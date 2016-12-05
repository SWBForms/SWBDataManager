/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.semanticwb.datamanager;

import java.io.File;
import java.io.Reader;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import javax.script.SimpleBindings;
import org.semanticwb.datamanager.datastore.SWBDataStore;
import org.semanticwb.datamanager.filestore.SWBFileSource;
import org.semanticwb.datamanager.script.ScriptObject;

/**
 *
 * @author javiersolis
 */
public class SWBBaseScriptEngine implements SWBScriptEngine 
{
    private static final ConcurrentHashMap<String,SWBBaseScriptEngine> engines=new ConcurrentHashMap();
    
//    private final HashMap<SWBUser, Bindings> users=new HashMap();    
    private HashMap<String,ScriptObject> dataSources=null;
    private HashMap<String,SWBDataStore> dataStores=null;
    
    private HashMap<String, SWBFileSource> fileSources=null;
    
    private HashMap<String,DataExtractorBase> dataExtractors=null;
        
    private HashMap<String,List<SWBDataService>> dataServices=null;
    private HashMap<String,List<SWBDataProcessor>> dataProcessors=null;
    
    private ScriptEngine sengine=null;
    private ScriptObject sobject=null;
    private String source=null;    
    private File file=null;    
    private transient long updated;
    private transient long lastCheck;
    
    private static final List _emptyList_=new ArrayList();
    
    private boolean closed=false;
    private boolean internalSource=false;
    
    private SWBScriptUtils utils;

    private SWBBaseScriptEngine(String source)
    {        
        this.source=source;
    }    
    
    private SWBBaseScriptEngine(String source, boolean internalSource)
    {
        this.source=source;
        this.internalSource=internalSource;
    }
    
    private void init()
    {
        System.out.println("initializing DataManager Engine...");
        try
        {
            utils=new SWBScriptUtils(this);
            lastCheck=System.currentTimeMillis();        
            if(!source.equals("[GLOBAL]"))
            {
                file=new File(DataMgr.getApplicationPath()+source);
                updated=file.lastModified();
            }else
            {
                updated=System.currentTimeMillis();;
            }
            
            ScriptEngine engine=DataMgr.getNativeScriptEngine();     
            //engine.put("_swbf_sengine", this);
            
            engine=DataMgr.loadLocalScript("/global.js", engine);
            
            String baseDS=DataMgr.getBaseInstance().getBaseDatasource();
            if(baseDS!=null)
            {
                engine=DataMgr.loadScript(baseDS, engine);
            }            
            
            if(!source.equals("[GLOBAL]"))
            {
                if(internalSource)
                    engine=DataMgr.loadLocalScript(source, engine); 
                else
                    engine=DataMgr.loadScript(source, engine);  
            }
            
            ScriptObject eng=new ScriptObject(engine.get("eng"));
            sobject=eng;
            
            //Load Routes
            ScriptObject ros = eng.get("routes");
            RoutesMgr.parseRouter(ros);
              
            //Load DataStores
            {
                HashMap<String,SWBDataStore> dataStores=new HashMap();
                this.sengine=engine;
                this.dataStores=dataStores;            
                ScriptObject dss=eng.get("dataStores");   
                Iterator<String> it=dss.keySet().iterator();
                while (it.hasNext()) {
                    String dsname = it.next();
                    ScriptObject dataStore=dss.get(dsname);
                    String dataStoreClass=dataStore.getString("class");
                    try
                    {
                        Class cls=Class.forName(dataStoreClass);
                        Constructor c=cls.getConstructor(ScriptObject.class);
                        System.out.println("Loading DataStore:"+dsname);
                        dataStores.put(dsname,(SWBDataStore)c.newInstance(dataStore));
                    }catch(Exception e){e.printStackTrace();}        
                }
            }            
            
            //Load DataSources
            {
                HashMap<String,ScriptObject> dataSources=new HashMap();
                this.sengine=engine;
                this.dataSources=dataSources;            
                ScriptObject dss=eng.get("dataSources");   
                Iterator<String> it=dss.keySet().iterator();
                while (it.hasNext()) {
                    String dsname = it.next();
                    System.out.println("Loading DataSource:"+dsname);                    
                    ScriptObject so=dss.get(dsname);
                    dataSources.put(dsname, so);
                }
            }
            
            //Load DataExtractors
            {
                dataExtractorsStop();  
            
                HashMap<String,DataExtractorBase> dataExtractors=new HashMap();
                this.dataExtractors=dataExtractors;
                ScriptObject ext=eng.get("dataExtractors");   
                System.out.println("Loading Extractors");
                Iterator<String> it=ext.keySet().iterator();
                while(it.hasNext())
                {
                    String key=it.next();
                    ScriptObject data=ext.get(key);
                    try
                    {
                        DataExtractorBase dext=new DataExtractorBaseImp(key,data,this);
                        dataExtractors.put(key,dext);
                    }catch(Exception e){e.printStackTrace();} 
                }
            }     
            
            //Load DataServices
            {
                HashMap<String,List<SWBDataService>> dataServices=new HashMap();
                this.dataServices=dataServices;            
                ScriptObject dss=eng.get("dataServices");   
                Iterator<String> it=dss.keySet().iterator();
                while(it.hasNext())
                {
                    String key=it.next();
                    ScriptObject data=dss.get(key);
                    System.out.println("Loading DataService:"+key);
                    SWBDataService dataService=new SWBDataService(key,data);
                    
                    Iterator<ScriptObject> dsit=data.get("dataSources").values().iterator();
                    while (dsit.hasNext()) 
                    {
                        ScriptObject dsname = dsit.next();
                        Iterator<ScriptObject> acit=data.get("actions").values().iterator();
                        while (acit.hasNext()) 
                        {
                            String action = acit.next().getValue().toString();
                            String name=dsname.getValue().toString();
                            
                            if(name.equals("*"))
                            {
                                Iterator<String> itds=dataSources.keySet().iterator();
                                while (itds.hasNext()) 
                                {
                                    name = itds.next();
                                    String k=name+"-"+action;
                                    List<SWBDataService> arr=dataServices.get(k);
                                    if(arr==null)
                                    {
                                        arr=new ArrayList();
                                        dataServices.put(k, arr);
                                    }
                                    arr.add(dataService);
                                }
                                
                            }else
                            {
                                String k=name+"-"+action;
                                List<SWBDataService> arr=dataServices.get(k);
                                if(arr==null)
                                {
                                    arr=new ArrayList();
                                    dataServices.put(k, arr);
                                }
                                arr.add(dataService);
                            }
                        }
                    }
                }
            }
            
            //Load DataProcessors
            {
                HashMap<String,List<SWBDataProcessor>> dataProcessors=new HashMap();
                this.dataProcessors=dataProcessors;            
                ScriptObject dss=eng.get("dataProcessors");   
                Iterator<String> it=dss.keySet().iterator();
                while(it.hasNext())
                {
                    String key=it.next();
                    ScriptObject data=dss.get(key);
                    System.out.println("Loading DataProcessor:"+key);
                    SWBDataProcessor dataProcessor=new SWBDataProcessor(key,data);
                    
                    Iterator<ScriptObject> dsit=data.get("dataSources").values().iterator();
                    while (dsit.hasNext()) 
                    {
                        ScriptObject dsname = dsit.next();
                        Iterator<ScriptObject> acit=data.get("actions").values().iterator();
                        while (acit.hasNext()) 
                        {
                            String action = acit.next().getValue().toString();
                            String name=dsname.getValue().toString();
                            
                            if(name.equals("*"))
                            {
                                Iterator<String> itds=dataSources.keySet().iterator();
                                while (itds.hasNext()) 
                                {
                                    name = itds.next();
                                    String k=name+"-"+action;
                                    List<SWBDataProcessor> arr=dataProcessors.get(k);
                                    if(arr==null)
                                    {
                                        arr=new ArrayList();
                                        dataProcessors.put(k, arr);
                                    }
                                    arr.add(dataProcessor);
                                }
                                
                            }else
                            {
                                String k=name+"-"+action;
                                List<SWBDataProcessor> arr=dataProcessors.get(k);
                                if(arr==null)
                                {
                                    arr=new ArrayList();
                                    dataProcessors.put(k, arr);
                                }
                                arr.add(dataProcessor);
                            }                            

                        }
                    }
                }
            }                                       
            
//            //Load UserRepository
//            {
//                ScriptObject ur=eng.get("userRepository");   
//                System.out.println("Loading UserRepository");
//                userRep=new SWBUserRepository(ur, this);
//            } 
            {
                this.fileSources = new HashMap<>();
                ScriptObject dss=eng.get("fileSources");
                Iterator<String> it=dss.keySet().iterator();
                while (it.hasNext()) {
                    String dsname = it.next();
                    ScriptObject fileSource=dss.get(dsname); 
                    String fileSourceClass=fileSource.getString("class");
                    String dataStore=fileSource.getString("dataStore");
                    ScriptObject ds = eng.get("dataStores");
                    if (null!=ds){
                        ds = ds.get(dataStore);
                    } else {
                        ds = null;
                    }
                    try
                    {
                        Class cls=Class.forName(fileSourceClass);
                        Constructor c=cls.getConstructor(ScriptObject.class, ScriptObject.class);
                        System.out.println("Loading FileSource:"+dsname); 
                        fileSources.put(dsname,(SWBFileSource)c.newInstance(fileSource, ds));
                    }catch(Exception e){e.printStackTrace();}        
                }
            }
            
            dataExtractorsStart();         
            
        }catch(Throwable e)
        {
            e.printStackTrace();
        }
    }
    
    private void dataExtractorsStart()
    {
        if(dataExtractors!=null)
        {
            Iterator<DataExtractorBase> it=dataExtractors.values().iterator();
            while (it.hasNext()) {
                DataExtractorBaseImp dataExtractor = (DataExtractorBaseImp)it.next();
                dataExtractor.start();
            }
        }           
    }
    
    private void dataExtractorsStop()
    {
        if(dataExtractors!=null)
        {
            Iterator<DataExtractorBase> it=dataExtractors.values().iterator();
            while (it.hasNext()) {
                DataExtractorBaseImp dataExtractor = (DataExtractorBaseImp)it.next();
                dataExtractor.stop();
            }
        }           
    }    
    
    public ScriptObject getDataSourceScript(String name)
    {
        return dataSources.get(name);
    }
    
    public ScriptObject getScriptObject()
    {
        return sobject;
    }
    
    public Set<String> getDataSourceNames()
    {
        return dataSources.keySet();   
    }
    
    @Override
    public SWBDataSource getDataSource(String name)
    {
        ScriptObject so=getDataSourceScript(name);
        if(so!=null)
        {
            return new SWBDataSource(name,null,so,this);
        }
        return null;
    }
    
    @Override
    public SWBDataSource getDataSource(String name, String modelid)
    {
        ScriptObject so=getDataSourceScript(name);
        if(so!=null)
        {
            return new SWBDataSource(name,modelid,so,this);
        }
        return null;
    }    
    
    @Override
    public SWBDataStore getDataStore(String name)
    {
        return dataStores.get(name);
    }    

    /**
     * Busca los objetos SWBDataService relacionados a un especifico DataSource y una accion 
     * @param dataSource
     * @param action
     * @return Lista de SWBDataService o null si no hay SWBDataService relacionados
     */
    @Override
    public List<SWBDataService> findDataServices(String dataSource, String action)
    {
        return dataServices.get(dataSource+"-"+action);
    }
    
    @Override
    public void invokeDataServices(String dataSource, String action, DataObject request, DataObject response)
    {
        invokeDataServices(this, dataSource, action, request, response);
    }
       
    protected void invokeDataServices(SWBScriptEngine userengine, String dataSource, String action, DataObject request, DataObject response)
    {
        List<SWBDataService> list=findDataServices(dataSource, action);
        if(list!=null)
        {
            Iterator<SWBDataService> dsit=list.iterator();
            while(dsit.hasNext())
            {
                SWBDataService dsrv=dsit.next();
                ScriptObject func=dsrv.getDataServiceScript().get(SWBDataService.METHOD_SERVICE);
                if(func!=null && func.isFunction())
                {
                    try
                    {
                        func.invoke(userengine,request,response.get("response"),dataSource,action);
                    }catch(Throwable e)
                    {
                        e.printStackTrace();
                    }
                }
            }            
        }       
    }
    
//    @Override
//    public SWBUserRepository getUserRepository()
//    {
//        return userRep;
//    }
    
    /**
     * Busca los objetos SWBDataProcessor relacionados a un especifico DataSource y una accion 
     * @param dataSource
     * @param action
     * @return Lista de SWBDataProcessor o null si no hay SWBDataService relacionados
     */
    
    @Override
    public List<SWBDataProcessor> findDataProcessors(String dataSource, String action)
    {
        return dataProcessors.get(dataSource+"-"+action);
    }   

    @Override
    public DataObject invokeDataProcessors(String dataSource, String action, String method, DataObject obj)
    {
        return invokeDataProcessors(this, dataSource, action, method, obj);
    }
    
    
    protected DataObject invokeDataProcessors(SWBScriptEngine userengine, String dataSource, String action, String method, DataObject obj)
    {
        List<SWBDataProcessor> list=findDataProcessors(dataSource, action);
        if(list!=null)
        {
            Iterator<SWBDataProcessor> dsit=list.iterator();
            while(dsit.hasNext())
            {
                SWBDataProcessor dsrv=dsit.next();
                ScriptObject func=dsrv.getDataProcessorScript().get(method);
                //System.out.println("func:"+func);
                if(func!=null && func.isFunction())
                {
                    try
                    {
                        ScriptObject r=func.invoke(userengine,obj,dataSource,action);
                        if(r!=null && r.getValue() instanceof DataObject)
                        {
                            obj=(DataObject)r.getValue();
                        }
                    }catch(jdk.nashorn.internal.runtime.ECMAException ecma)
                    {
                        throw ecma;
                    }catch(Throwable e)
                    {
                        e.printStackTrace();
                    }
                }
            }            
        }   
        return obj;
    }
    
    
    @Override
    public void reloadScriptEngine()
    {
        try
        {
            init();
        }catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    
    @Override
    public ScriptEngine getNativeScriptEngine()
    {
        return sengine;
    }
    
    @Override
    public Object eval(String script) throws ScriptException
    {
        return sengine.eval(script);
    }
    
    protected Object eval(String script, Bindings bind) throws ScriptException
    {
        return sengine.eval(script, bind);
    }
    
    @Override
    public Object eval(Reader script) throws ScriptException
    {
        return sengine.eval(script);
    }    
    
    protected Object eval(Reader script, Bindings bind) throws ScriptException
    {
        return sengine.eval(script,bind);
    }        
    
    @Override
    public void chechUpdates()
    {
        long time=System.currentTimeMillis();
        if((time-lastCheck)>10000)
        {
            lastCheck=time;
            //System.out.println("time:"+(time-lastCheck)+" updated:"+updated+" source.lastModified():"+source.lastModified());
            if(file!=null && updated!=file.lastModified())
            {
                synchronized(this)
                {
                    if(updated!=file.lastModified())
                    {
                        System.out.println("Update ScriptEngine");
                        reloadScriptEngine();
                    }
                }
            }
        }
    }
    
   
    public Bindings getUserBindings(SWBUserScriptEngine engine)
    {
        Bindings b=null;
//        if(user==null)return null;
//        Bindings b = users.get(user);
        System.out.println("getUserBindings:"+engine);
//        if(b==null)
//        {
//            synchronized(users)
//            {           
//                b = users.get(user);
//                if(b==null)
//                {
                    b = new SimpleBindings();     
                    Bindings enginescope=sengine.getBindings(ScriptContext.ENGINE_SCOPE);            
                    Iterator<Map.Entry<String,Object>> set=enginescope.entrySet().iterator();
                    while (set.hasNext()) {
                        Map.Entry<String, Object> entry = set.next();
                        b.put(entry.getKey(), entry.getValue());
//                        System.out.println(engine.getUser()+" prop:"+entry.getKey()+" "+entry.getValue().hashCode());
                    }
                    //b.put("_swbf_user", user);  
                    b.put("sengine", engine);
//                    users.put(user, b);
//                }
//            }
//        }
        return b;        
    }

    public Bindings getUserBindings() {
        return null;
    }   

    @Override
    public SWBScriptUtils getUtils() {
        return utils;
    }
    
    @Override
    public void close() {
        if(!closed)
        {
            synchronized(this)
            {
                if(!closed)
                {
                    closed=true;
                    dataExtractorsStop();
                    System.out.println("Closed DataManager Engine...");
                }
            }
        }
    }

    @Override
    public boolean isClosed() {
        return closed;
    }

    @Override
    public DataObject getUser() {
        return null;
    }    
    
//******************************** static *****************************************************//    
    
    protected static SWBBaseScriptEngine getScriptEngine(String source, boolean internal)
    {
        SWBBaseScriptEngine engine=engines.get(source);        
        if(engine==null)
        {
            synchronized(engines)
            {
                engine=engines.get(source);
                if(engine==null)
                {
                    try
                    {
                        engine=new SWBBaseScriptEngine(source,internal);
                        engine.init();
                        engines.put(source, engine);
                    }catch(Throwable e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }else
        {
            engine.chechUpdates();
        }
        return engine;
    }

    @Override
    public SWBFileSource getFileSource(String name) {
        return fileSources.get(name);
    }

    @Override
    public Object getContextData(String key) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object setContextData(String key, Object data) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
