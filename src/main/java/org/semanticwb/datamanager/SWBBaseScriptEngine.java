/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.semanticwb.datamanager;

import com.mongodb.util.JSON;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
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
    private static final Logger logger = Logger.getLogger(SWBBaseScriptEngine.class.getName());
    
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
    private transient long id;
    private transient long lastCheck;
    
    private ArrayList<SWBScriptFile> files=new ArrayList();
    private boolean needsReload=false;
    //private File file=null;    
    //private transient long updated;
        
    private boolean closed=false;
    private boolean internalSource=false;
    
    private boolean disabledDataTransforms=false;
    
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
        logger.log(Level.INFO,"Initializing ScriptEngine: "+source);
        try
        {
            utils=new SWBScriptUtils(this);
            lastCheck=System.currentTimeMillis();  
            id=lastCheck;
            
            ArrayList<String> sources=new ArrayList();
            if(!source.equals("[GLOBAL]"))
            {
                int i=source.indexOf("[");
                if(i>-1)
                {
                    String base=source.substring(0,i);
                    String arr=source.substring(i);
                    List<String> o=(List)JSON.parse(arr);
                    //System.out.println(o);
                    o.forEach(s->{
                        if(s.startsWith("/"))
                        {
                            sources.add(s);
                        }else
                        {
                            sources.add(base+s);
                        }
                    });
                }else
                {
                    sources.add(source);
                }
            }            
            
            files.clear();
            if(!sources.isEmpty())
            {                
                sources.forEach(s->{
                    File f=new File(DataMgr.getApplicationPath()+s); 
                    files.add(new SWBScriptFile(f));
                });
            }
            
            ScriptEngine engine=DataMgr.getNativeScriptEngine();     
            //engine.put("_swbf_sengine", this);
            
            engine=DataMgr.loadLocalScript("/global.js", engine);
            
            String baseDS=DataMgr.getBaseInstance().getBaseDatasource();
            if(baseDS!=null)
            {
                engine=DataMgr.loadScript(baseDS, engine);
            }      
                
            Iterator<String> it2=sources.iterator();
            while (it2.hasNext()) {
                String f = it2.next();
                if(internalSource)
                    engine=DataMgr.loadLocalScript(f, engine); 
                else
                    engine=DataMgr.loadScript(f, engine);                 
            }
            
//            if(!source.equals("[GLOBAL]"))
//            {
//                if(internalSource)
//                    engine=DataMgr.loadLocalScript(source, engine); 
//                else
//                    engine=DataMgr.loadScript(source, engine);  
//            }
            
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
                        logger.log(Level.INFO,"Loading DataStore:"+dsname);
                        dataStores.put(dsname,(SWBDataStore)c.newInstance(dataStore));
                    }catch(Exception e){e.printStackTrace();}        
                }
            }            
            
            //Load DataSources
            {
                HashMap<String,ScriptObject> dataSources=new HashMap();
                this.dataSources=dataSources;            
                ScriptObject dss=eng.get("dataSources");   
                Iterator<String> it=dss.keySet().iterator();
                while (it.hasNext()) {
                    String dsname = it.next();
                    logger.log(Level.INFO,"Loading DataSource:"+dsname);                    
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
                logger.log(Level.INFO,"Loading Extractors");
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
                    logger.log(Level.INFO,"Loading DataService:"+key);
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
                    logger.log(Level.INFO,"Loading DataProcessor:"+key);
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
                        logger.log(Level.INFO,"Loading FileSource:"+dsname); 
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
    
    /**
     *
     * @param name
     * @return
     */
    public ScriptObject getDataSourceScript(String name)
    {
        return dataSources.get(name);
    }
    
    /**
     *
     * @return
     */
    public ScriptObject getScriptObject()
    {
        return sobject;
    }
    
    /**
     *
     * @return
     */
    public Set<String> getDataSourceNames()
    {
        return dataSources.keySet();   
    }
    
    /**
     *
     * @param name
     * @return
     */
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
    
    /**
     *
     * @param name
     * @param modelid
     * @return
     */
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
    
    /**
     *
     * @param name
     * @return
     */
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
    
    /**
     *
     * @param dataSource
     * @param action
     * @param request
     * @param response
     */
    @Override
    public void invokeDataServices(String dataSource, String action, DataObject request, DataObject response)
    {
        invokeDataServices(this, dataSource, action, request, response);
    }
       
    /**
     *
     * @param userengine
     * @param dataSource
     * @param action
     * @param request
     * @param response
     */
    protected void invokeDataServices(SWBScriptEngine userengine, String dataSource, String action, DataObject request, DataObject response)
    {
        if(disabledDataTransforms)return;
        
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

    /**
     *
     * @param dataSource
     * @param action
     * @param method
     * @param obj
     * @return
     */
    @Override
    public DataObject invokeDataProcessors(String dataSource, String action, String method, DataObject obj)
    {
        return invokeDataProcessors(this, dataSource, action, method, obj);
    }
    
    /**
     *
     * @param userengine
     * @param dataSource
     * @param action
     * @param method
     * @param obj
     * @return
     */
    protected DataObject invokeDataProcessors(SWBScriptEngine userengine, String dataSource, String action, String method, DataObject obj)
    {
        if(disabledDataTransforms)return obj;
        
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
    
    /**
     *
     */
    @Override
    public void reloadScriptEngine()
    {
        try
        {
            close();
            
            dataSources.clear();
            fileSources.clear();
            dataExtractors.clear();
            dataServices.clear();
            dataProcessors.clear();
            
            sengine=null;
            sobject=null;
    
            files.clear();
            needsReload=false;
            closed=false;
            utils=null;        
            
            init();
        }catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    
    /**
     *
     */
    public void needsReloadAllScriptEngines()
    {
        Iterator<SWBBaseScriptEngine> it=engines.values().iterator();
        while (it.hasNext()) {
            SWBBaseScriptEngine eng = it.next();
            eng.needsReloadScriptEngine();
        }        
    }
    
    /**
     *
     */
    public void reloadAllScriptEngines()
    {
        Iterator<SWBBaseScriptEngine> it=engines.values().iterator();
        while (it.hasNext()) {
            SWBBaseScriptEngine eng = it.next();
            eng.reloadScriptEngine();
        }        
    }        
    
    /**
     * Mark script engine as changed for reload
     */
    public void needsReloadScriptEngine()
    {
        this.needsReload=true;
    }
    
    /**
     *
     * @return
     */
    public boolean isNeedsReloadScriptEngine()
    {
        return needsReload;
    }
    
    /**
     *
     * @return
     */
    @Override
    public ScriptEngine getNativeScriptEngine()
    {
        return sengine;
    }
    
    /**
     *
     * @param script
     * @return
     * @throws ScriptException
     */
    @Override
    public Object eval(String script) throws ScriptException
    {
        return sengine.eval(script);
    }
    
    /**
     *
     * @param script
     * @param bind
     * @return
     * @throws ScriptException
     */
    protected Object eval(String script, Bindings bind) throws ScriptException
    {
        return sengine.eval(script, bind);
    }
    
    /**
     *
     * @param script
     * @return
     * @throws ScriptException
     */
    @Override
    public Object eval(Reader script) throws ScriptException
    {
        return sengine.eval(script);
    }    
    
    /**
     *
     * @param script
     * @param bind
     * @return
     * @throws ScriptException
     */
    protected Object eval(Reader script, Bindings bind) throws ScriptException
    {
        return sengine.eval(script,bind);
    }        
    
    /**
     *
     */
    @Override
    public void chechUpdates()
    {        
        {
            long time=System.currentTimeMillis();
            if((time-lastCheck)>10000)
            {
                lastCheck=time;
                if(needsReload)
                {
                    synchronized(this)
                    {
                        if(needsReload)
                        {
                            needsReload=false;
                            logger.log(Level.INFO,"remove ScriptEngine:"+source);
                            reloadScriptEngine();
                        }
                    }
                }                
                
                Iterator<SWBScriptFile> it=files.iterator();
                while (it.hasNext()) {
                    SWBScriptFile f = it.next();
                    if(f.chechUpdates())break;
                }
            }
        }
    }
    
    /**
     *
     * @param engine
     * @return
     */
    public Bindings getUserBindings(SWBUserScriptEngine engine)
    {
        Bindings b=null;
//        if(user==null)return null;
//        Bindings b = users.get(user);
//        System.out.println("getUserBindings:"+engine);
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

    /**
     *
     * @return
     */
    public Bindings getUserBindings() {
        return null;
    }   

    /**
     *
     * @return
     */
    @Override
    public SWBScriptUtils getUtils() {
        return utils;
    }
    
    /**
     *
     */
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
                    
                    //DataStores..
                    Iterator<SWBDataStore> it=dataStores.values().iterator();
                    while (it.hasNext()) {
                        SWBDataStore next = it.next();
                        next.close();
                    }
                    
                    logger.log(Level.INFO,"Closed ScriptEngine: "+source);
                }
            }
        }
    }

    /**
     *
     * @return
     */
    @Override
    public boolean isClosed() {
        return closed;
    }

    /**
     *
     * @return
     */
    @Override
    public DataObject getUser() {
        return null;
    }    
    
//******************************** static *****************************************************//    

    /**
     *
     * @param source
     * @param internal
     * @return
     */
    
    public static SWBBaseScriptEngine getScriptEngine(String source, boolean internal)
    {
        //System.out.println("getScriptEngine:"+source);
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

    /**
     *
     * @param name
     * @return
     */
    @Override
    public SWBFileSource getFileSource(String name) {
        return fileSources.get(name);
    }

    /**
     *
     * @param key
     * @return
     */
    @Override
    public Object getContextData(String key) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     *
     * @param key
     * @param data
     * @return
     */
    @Override
    public Object setContextData(String key, Object data) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     *
     * @param role
     * @return
     */
    @Override
    public boolean hasUserRole(String role) {
        return false;
    }
    
    /**
     *
     * @param roles
     * @return
     */
    @Override
    public boolean hasUserAnyRole(String... roles)
    {
        return false;
    }
    
    /**
     *
     * @param roles
     * @return
     */
    @Override
    public boolean hasUserAnyRole(List<String> roles)
    {
        return false;
    }

    /**
     *
     * @param group
     * @return
     */
    @Override
    public boolean hasUserGroup(String group) {
        return false;
    }

    /**
     *
     */
    @Override
    public void removeUserPermissionCache() {        
    }

    /**
     *
     * @param permission
     * @return
     */
    @Override
    public boolean hasUserPermission(String permission) {
        return false;
    }

    /**
     *
     * @return
     */
    @Override
    public String getAppName() {
        ScriptObject config = getScriptObject().get("config");
        if (config != null) {
            return config.getString("appName");
        }
        return null;
    }
    
    /**
     *
     * @return
     */
    @Override
    public boolean getDSCache() {
        ScriptObject config = getScriptObject().get("config");
        if (config != null) {
            return Boolean.parseBoolean(config.getString("dsCache"));
        }
        return false;
    }    

    /**
     *
     * @return
     */
    @Override
    public long getId() {
        return id;
    }

    @Override
    public DataObject fetchObjectById(String id) throws IOException
    {
        //"_suri:"+modelid+":"+scls+":";
        String ids[]=id.split(":");
        if(ids.length==4)return getDataSource(ids[2], ids[1]).fetchObjById(id);
        return null;
    }

    @Override
    public DataObject getObjectById(String id) {
        //"_suri:"+modelid+":"+scls+":";
        String ids[]=id.split(":");
        if(ids.length==4)return getDataSource(ids[2], ids[1]).getObjectById(id);
        return null;        
    }

    public void setDisabledDataTransforms(boolean disabledDataTransforms) {
        this.disabledDataTransforms = disabledDataTransforms;
    }

    public boolean isDisabledDataTransforms() {
        return disabledDataTransforms;
    }
    
    /**
     *
     */
    public class SWBScriptFile
    {
        private File file=null;      
        private transient long updated;    

        /**
         *
         * @param file
         */
        public SWBScriptFile(File file) {
            this.file=file;
            this.updated=file.lastModified();
        }
        
        /**
         *
         * @return
         */
        public boolean chechUpdates()
        {
            if(file!=null && updated!=file.lastModified())
            {
                synchronized(this)
                {
                    if(updated!=file.lastModified())
                    {
                        logger.log(Level.INFO,"Update ScriptEngine:"+source);
                        reloadScriptEngine();
                        return true;
                    }
                }
            }
            return false;
        }        
                
    }
    
}
