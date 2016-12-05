/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.semanticwb.datamanager;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.semanticwb.datamanager.datastore.SWBDataStore;
import org.semanticwb.datamanager.script.ScriptObject;

/**
 *
 * @author javier.solis
 */
public class SWBDataSource 
{
    public static final String ACTION_FETCH="fetch";
    public static final String ACTION_AGGREGATE="aggregate";
    public static final String ACTION_UPDATE="update";
    public static final String ACTION_ADD="add";
    public static final String ACTION_REMOVE="remove";
    public static final String ACTION_VALIDATE="validate";
    public static final String ACTION_LOGIN="login";
    public static final String ACTION_LOGOUT="logout";
    public static final String ACTION_USER="user";
    public static final String ACTION_CONTEXTDATA="contextData";
    
    private String name=null;
    private String dataStoreName=null;
    private SWBScriptEngine engine=null;
    private ScriptObject script=null;
    private SWBDataStore db=null;
    private String modelid=null;
    
    private HashMap<String,DataObject> cache=new HashMap();    
    private HashMap<String,String> removeDependenceFields=null;
    

    protected SWBDataSource(String name, String modelid, ScriptObject script, SWBScriptEngine engine)
    {
        this.name=name;
        this.engine=engine;
        this.script=script;      
        this.modelid=modelid;
        dataStoreName=this.script.getString("dataStore");
        this.db=engine.getDataStore(dataStoreName);        
        if(this.db==null)throw new NoSuchFieldError("DataStore not found:"+dataStoreName);
    }

    /**
     * Regresa Nombre del DataSource
     * @return String
     */
    public String getName() {
        return name;
    }

    /**
     * Regresa el SWBScriptEngine que contiene a este DataSource
     * @return SWBScriptEngine
     */
    public SWBScriptEngine getScriptEngine() {
        return engine;
    }
    
    /**
     * Regresa ScriptObject con el script con la definición del datasource definida el el archivo js
     * @return ScriptObject
     */
    public ScriptObject getDataSourceScript()
    {
        return script;
    }      
    
    public DataObject fetch() throws IOException
    {
        return fetch(new DataObject());
    }    
    
//    public DataObject fetch(String query) throws IOException
//    {
//        return fetch((DataObject)JSON.parse(query));
//    }
    
    public DataObject fetch(DataObject json) throws IOException
    {
        DataObject req=engine.invokeDataProcessors(name, SWBDataSource.ACTION_FETCH, SWBDataProcessor.METHOD_REQUEST, json);
        DataObject res=db.fetch(req,this);
        res=engine.invokeDataProcessors(name, SWBDataSource.ACTION_FETCH, SWBDataProcessor.METHOD_RESPONSE, res);
        engine.invokeDataServices(name, SWBDataSource.ACTION_FETCH, req, res);
        return res;
    }
    
    public DataObject fetch(jdk.nashorn.api.scripting.ScriptObjectMirror json) throws IOException
    {        
        return fetch(DataUtils.toDataObject(json));
    }
    
    public DataObject aggregate(DataObject json) throws IOException
    {
        DataObject req=engine.invokeDataProcessors(name, SWBDataSource.ACTION_AGGREGATE, SWBDataProcessor.METHOD_REQUEST, json);
        DataObject res=db.aggregate(req,this);
        res=engine.invokeDataProcessors(name, SWBDataSource.ACTION_AGGREGATE, SWBDataProcessor.METHOD_RESPONSE, res);
        engine.invokeDataServices(name, SWBDataSource.ACTION_AGGREGATE, req, res);
        return res;
    }
    
    public DataObject aggregate(jdk.nashorn.api.scripting.ScriptObjectMirror json) throws IOException
    {        
        return aggregate(DataUtils.toDataObject(json));
    }    

    public DataObject addObj(DataObject obj) throws IOException
    {
        DataObject ret=null;
        DataObject req=new DataObject();
        req.put("data", obj);        
        ret=add(req);
        return ret;
    }  
    
    public DataObject addObj(jdk.nashorn.api.scripting.ScriptObjectMirror json) throws IOException
    {        
        return addObj(DataUtils.toDataObject(json));
    }
    
    public DataObject updateObj(DataObject obj) throws IOException
    {
        DataObject ret=null;
        DataObject req=new DataObject();
        req.put("data", obj);        
        ret=update(req);
        return ret;
    }
    
    public DataObject updateObj(jdk.nashorn.api.scripting.ScriptObjectMirror json) throws IOException
    {        
        return updateObj(DataUtils.toDataObject(json));
    }
    
    public DataObject removeObj(DataObject obj) throws IOException
    {
        DataObject ret=null;
        DataObject req=new DataObject();
        req.put("data", obj);        
        ret=remove(req);
        return ret;
    }
    
    public DataObject removeObj(jdk.nashorn.api.scripting.ScriptObjectMirror json) throws IOException
    {        
        return removeObj(DataUtils.toDataObject(json));
    }    
    

    public DataObject fetchObjByNumId(String id) throws IOException
    {
        return fetchObjById(getBaseUri()+id);
    }    
    
    public DataObject fetchObjById(String id) throws IOException
    {
        DataObject ret=null;
        DataObject req=new DataObject();
        DataObject data=new DataObject();
        data.put("_id", id);
        req.put("data", data);

        DataObject r=(DataObject)fetch(req);
        if(r!=null)
        {
            DataObject res=(DataObject)r.get("response");       
            if(res!=null)
            {
                DataList rdata=(DataList)res.get("data");
                if(rdata!=null && rdata.size()>0)
                {
                    ret=(DataObject)rdata.get(0);
                }
            }            
        }
        return ret;
    }
    
    /**
     * Regresa Objecto de cache NumID y si no lo tiene lo carga, de lo contrario regresa null
     * @param id
     * @return 
     */
    public DataObject getObjectByNumId(String id)
    {
        return getObjectById(getBaseUri()+id);
    }    
    
    /**
     * Regresa Objecto de cache por ID y si no lo tiene lo carga, de lo contrario regresa null
     * @param id
     * @return 
     */
    public DataObject getObjectById(String id)
    {
        DataObject obj=cache.get(id);
        if(obj==null)
        {
            synchronized(cache)
            {
                obj=cache.get(id);
                if(obj==null)
                {
                    try
                    {
                        obj=fetchObjById(id);
                        cache.put(id, obj);
                    }catch(IOException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }
        return obj;
    }
    
    public DataObject removeObjByNumId(String id) throws IOException
    {
        return removeObjById(getBaseUri()+id);
    }    
    
    public DataObject removeObjById(String id) throws IOException
    {
        DataObject ret=null;
        DataObject req=new DataObject();
        DataObject data=new DataObject();
        data.put("_id", id);
        req.put("data", data);

        DataObject r=(DataObject)remove(req);
        if(r!=null)
        {
            ret=(DataObject)r.get("response");       
        }
        
        cache.remove(id);
        
        return ret;
    }   
    
//    public DataObject update(String query) throws IOException
//    {
//        return update((DataObject)JSON.parse(query));
//    }
    
    public DataObject update(DataObject json) throws IOException
    {
        DataObject req=engine.invokeDataProcessors(name, SWBDataSource.ACTION_UPDATE, SWBDataProcessor.METHOD_REQUEST, json);
        DataObject res=db.update(req,this);
        res=engine.invokeDataProcessors(name, SWBDataSource.ACTION_UPDATE, SWBDataProcessor.METHOD_RESPONSE, res);
        engine.invokeDataServices(name, SWBDataSource.ACTION_UPDATE, req, res);
        
        if(req!=null)
        {
            DataObject data=req.getDataObject("data");
            if(data!=null)
            {
                String id=data.getString("_id");
                cache.remove(id);
            }
        }
        
        return res;
    }   
    
    public DataObject update(jdk.nashorn.api.scripting.ScriptObjectMirror json) throws IOException
    {        
        return update(DataUtils.toDataObject(json));
    }
    
//    public DataObject add(String query) throws IOException
//    {
//        return add((DataObject)JSON.parse(query));
//    }
    
    public DataObject add(DataObject json) throws IOException
    {
        DataObject req=engine.invokeDataProcessors(name, SWBDataSource.ACTION_ADD, SWBDataProcessor.METHOD_REQUEST, json);
        DataObject res=db.add(req,this);
        res=engine.invokeDataProcessors(name, SWBDataSource.ACTION_ADD, SWBDataProcessor.METHOD_RESPONSE, res);
        engine.invokeDataServices(name, SWBDataSource.ACTION_ADD, req, res);
        return res;
    }  
    
    public DataObject add(jdk.nashorn.api.scripting.ScriptObjectMirror json) throws IOException
    {        
        return add(DataUtils.toDataObject(json));
    }
    
//    public DataObject remove(String query) throws IOException
//    {
//        return remove((DataObject)JSON.parse(query));
//    }
    
    public HashMap getRemoveDependenceFields()
    {
        if(removeDependenceFields==null)
        {
            synchronized(this)
            {
                if(removeDependenceFields==null)
                {
                    System.out.println("Loading removeDependence "+getName());
                    removeDependenceFields=new HashMap();
                    ScriptObject fields=script.get("fields");
                    if(fields!=null)
                    {
                        Iterator<ScriptObject> it = fields.values().iterator();
                        while (it.hasNext()) {
                            ScriptObject obj = it.next();
                            String val = obj.getString("removeDependence");
                            if(val==null && "grid".equals(obj.getString("stype")))val="true"; //TODO: crear deficionion configurable de stipos contra propiedades
                            if(val!=null && val.equals("true"))
                            {
                                String name=obj.getString("name");
                                String dss=obj.getString("dataSource");
                                removeDependenceFields.put(name,dss);
                            }
                        }   
                    }
                    ScriptObject links=script.get("links");
                    if(links!=null)
                    {
                        Iterator<ScriptObject>it = links.values().iterator();
                        while (it.hasNext()) {
                            ScriptObject obj = it.next();
                            String val = obj.getString("removeDependence");
                            if(val==null || val.equals("true"))
                            {
                                String name=obj.getString("name");
                                String dss=obj.getString("dataSource");
                                removeDependenceFields.put(name,dss);
                            }
                        }     
                    }
                }
            }            
        }
        return removeDependenceFields;
    }
    
    private void removeDependence(String id) throws IOException
    {
        //System.out.println("removeDependence:"+id);
        if(id==null)return;
        DataObject obj=fetchObjById(id);
        //System.out.println("obj:"+obj);
        HashMap map=getRemoveDependenceFields();
        Iterator<String> it=map.keySet().iterator();
        while (it.hasNext()) {
            String name = it.next();
            String dss=(String)map.get(name);
            Object o=obj.get(name);
            //System.out.println("prop:"+name+":"+o);
            if(o instanceof DataList)
            {
                DataList list=(DataList)o;
                Iterator<String> it2=list.iterator();
                while (it2.hasNext()) {
                    String str = it2.next();
                    //System.out.println("remove m:"+str+":"+dss);
                    SWBDataSource ds=engine.getDataSource(dss);
                    ds.removeObjById(str);
                }
            }else if(o instanceof String)
            {
                //System.out.println("remove s:"+o+":"+dss);
                SWBDataSource ds=engine.getDataSource(dss);
                ds.removeObjById(o.toString());
            }
            
        }
    }
    
    private void checkRemoveDependence(DataObject json) throws IOException
    {        
        HashMap map=getRemoveDependenceFields();
        if(!map.isEmpty())
        {
            DataObject data = json.getDataObject("data");
            boolean removeByID=json.getBoolean("removeByID",true);
            if(removeByID)
            {
                String id=data.getString("_id");
                removeDependence(id);
            }else
            {
                DataObject r=fetch(json);
                if(r!=null)
                {
                    DataObject res=(DataObject)r.get("response");       
                    if(res!=null)
                    {
                        DataList rdata=(DataList)res.get("data");
                        Iterator<DataObject> it=rdata.iterator();
                        while (it.hasNext()) {
                            DataObject obj = it.next();
                            removeDependence(obj.getId());
                        }
                    }   
                }
            }
        }
    }
    
    public DataObject remove(DataObject json) throws IOException
    {
        DataObject req=engine.invokeDataProcessors(name, SWBDataSource.ACTION_REMOVE, SWBDataProcessor.METHOD_REQUEST, json);
        checkRemoveDependence(json);
        DataObject res=db.remove(req,this);
        res=engine.invokeDataProcessors(name, SWBDataSource.ACTION_REMOVE, SWBDataProcessor.METHOD_RESPONSE, res);
        engine.invokeDataServices(name, SWBDataSource.ACTION_REMOVE, req, res);
        cache.clear();        
        return res;
    }   
    
    public DataObject remove(jdk.nashorn.api.scripting.ScriptObjectMirror json) throws IOException
    {        
        return remove(DataUtils.toDataObject(json));
    }
    
//    public DataObject validate(String query) throws IOException
//    {
//        return validate((DataObject)JSON.parse(query));
//    }
    
    public DataObject validate(DataObject json) throws IOException
    {
//        String modelid=dataSource.getModelId();
//        String scls=dataSource.getClassName();
        DataObject ret=new DataObject();
        DataObject resp=new DataObject();
        DataObject errors=new DataObject();
        ret.put("response", resp);

        boolean hasErrors=false;
        

        DataObject data=(DataObject)json.get("data");
        if(data!=null)
        {
            Iterator<Map.Entry<String,Object>> it=data.entrySet().iterator();
            while(it.hasNext())
            {
                Map.Entry<String,Object> entry=it.next(); 

                String key=entry.getKey();
                Object value=entry.getValue();
                ScriptObject field=getDataSourceScriptField(key);
                if(field!=null)
                {
                    ScriptObject validators=field.get("validators");
                    if(validators!=null)
                    {
                        Iterator<ScriptObject> it2=validators.values().iterator();
                        while (it2.hasNext()) 
                        {
                            ScriptObject validator = it2.next();
                            String type=validator.getString("type");

                            if("serverCustom".equals(type))
                            {
                                ScriptObject func=validator.get("serverCondition");
                                if(func!=null)
                                {
                                    //System.out.println(key+"-->"+value+"-->"+func);
                                    ScriptObject r=func.invoke(engine,key,value,json);
                                    //System.out.println("r:"+r.getValue());
                                    if(r!=null && r.getValue().equals(false))
                                    {
                                        //System.out.println("Error...");
                                        hasErrors=true;
                                        String errmsg=validator.getString("errorMessage");
                                        if(errmsg==null)errmsg="Error..";
                                        errors.put(key, errmsg);
                                    }
                                }
                            }else if("isUnique".equals(type))
                            {
                                String id=(String)data.get("_id");
                                DataObject req=new DataObject();
                                DataObject query=new DataObject();
                                req.put("data", query);
                                query.put(key, value);
                                DataList rdata=(DataList)((DataObject)fetch(req).get("response")).get("data");                                  
                                if(rdata!=null && rdata.size()>0)
                                {
                                    if(rdata.size()>1 || id==null || !((DataObject)rdata.get(0)).get("_id").toString().equals(id))
                                    {
                                        hasErrors=true;
                                        String errmsg=validator.getString("errorMessage");
                                        //TODO:Internacionalizar...
                                        if(errmsg==null)errmsg="El valor debe de ser único..";
                                        errors.put(key, errmsg);
                                    }
                                }                                
                                //System.out.println("isUnique:"+key+"->"+value+" "+id+" "+r);
                            }
                        }
                    }
                }
            }        
        }
        
        if(hasErrors)
        {
            resp.put("status", -4);
            resp.put("errors", errors);
        }else
        {
            resp.put("status", 0);
        }
        return ret;                
    } 
    
    public DataObject validate(jdk.nashorn.api.scripting.ScriptObjectMirror json) throws IOException
    {        
        return validate(DataUtils.toDataObject(json));
    }
    
    public ScriptObject getDataSourceScriptField(String name)
    {
        ScriptObject fields=script.get("fields");
        ScriptObject ret=DataUtils.getArrayNode(fields, "name", name);
        if(ret==null)
        {
            fields=script.get("links");
            ret=DataUtils.getArrayNode(fields, "name", name);
        }
        return ret;
    }
    
    public String getModelId()
    {
        if(modelid!=null)return modelid;
        String modelid=getDataSourceScript().getString("modelid");
        //System.out.println("getModelId 1:"+modelid);
        
        Iterator it=DataUtils.TEXT.findInterStr(modelid, "{contextData.", "}");
        while(it.hasNext())
        {
            String s=(String)it.next();
            String o=(String)engine.getContextData(s);
            if(o!=null)
            {
                modelid=modelid.replace("{contextData."+s+"}", o);
            }
        }
        //System.out.println("getModelId 2:"+modelid);        
        return modelid;
    }
    
    public String getClassName()
    {
        return getDataSourceScript().getString("scls");
    }    
    
    public String getBaseUri()
    {
        String modelid=getModelId();
        String scls=getClassName();
        //TODO:get NS
        return "_suri:"+modelid+":"+scls+":";
        //return "_suri:http://swb.org/"+dataStoreName+"/"+modelid+"/"+scls+":";
    }
            
//******************************************* static *******************************/            
    
    public static DataObject getError(int x)
    {
        DataObject ret=new DataObject();
        DataObject resp=new DataObject();
        ret.put("response", resp);
        resp.put("status", x);
        //resp.put("data", obj);
        return ret;
    }
    
//    private static ScriptObject getServerValidator(ScriptObject field, String type)
//    {
//        ScriptObject validators=field.get("validators");
//        return SWBFormsUtils.getArrayNode(validators, "type", type);
//    }    
    
}
