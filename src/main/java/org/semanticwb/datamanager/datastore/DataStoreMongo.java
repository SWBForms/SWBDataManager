/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.semanticwb.datamanager.datastore;

import com.mongodb.AggregationOutput;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.util.JSON;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.bson.types.ObjectId;
import org.semanticwb.datamanager.DataList;
import org.semanticwb.datamanager.DataObject;
import org.semanticwb.datamanager.DataObjectIterator;
import org.semanticwb.datamanager.SWBDataSource;
import org.semanticwb.datamanager.script.ScriptObject;

/**
 *
 * @author javiersolis
 */
public class DataStoreMongo implements SWBDataStore
{
    static private Logger log = Logger.getLogger(DataStoreMongo.class.getName());
    private MongoClient mongoClient=null;
    ScriptObject dataStore=null;
        
    /**
     *
     * @param dataStore
     */
    public DataStoreMongo(ScriptObject dataStore) 
    {
        //System.out.println("DataStoreMongo:"+dataStore);
        this.dataStore=dataStore;
        try
        {
            initDB();
        }catch(IOException e)
        {
            e.printStackTrace();
        }
    }
    
    private void initDB() throws IOException
    {
        if(mongoClient==null)
        {
            synchronized(DataStoreMongo.class)
            {
                if(mongoClient==null)
                {
                    //System.out.println("New DataStoreMongo...");
                    //TODO:configurar direccion de servidor
                    if ((null!=this.dataStore.getString("envhost") && null!=this.dataStore.getString("envport")))
                    {
                        String host = System.getenv(dataStore.getString("envhost"));
                        //log.info("Host:"+host);
                        int port = Integer.parseInt(System.getenv(dataStore.getString("envport")));
                        //log.info("Port:"+port);
                        log.fine("Connecting to: host:port -> "+host+":"+port);
                        mongoClient = new MongoClient(host, port);
                    } else
                    {
                        mongoClient = new MongoClient(dataStore.getString("host"), (Integer) dataStore.get("port").getValue());
                    }
                }                
            }
        }
    }
    
    /**
     *
     * @param dson
     * @param dataSource
     * @return
     * @throws IOException
     */
    public DataObjectIterator find(DataObject dson, SWBDataSource dataSource) throws IOException
    {
        BasicDBObject json=toBasicDBObject(dson);
        //System.out.println("fetch:"+dson);
//        MongoClient mongoClient = new MongoClient("localhost");
        try
        {
            String modelid=dataSource.getModelId();
            String scls=dataSource.getClassName();
            DB db = mongoClient.getDB(modelid);
            DBCollection coll = db.getCollection(scls);

            //BasicDBObject json=(BasicDBObject) JSON.parse(query);
            //String operationType = json.getString("operationType");
            int startRow = json.getInt("startRow",0);
            int endRow = json.getInt("endRow",0);
            //String dataSource = json.getString("dataSource");
            //String componentId = json.getString("componentId");
            String textMatchStyle = json.getString("textMatchStyle");
            BasicDBObject data = (BasicDBObject)json.get("data");
            //BasicDBObject oldValues = (BasicDBObject)json.get("oldValues");
            BasicDBList sortBy= (BasicDBList)json.get("sortBy");        

            //textMatchStyle
            //  exact
            //  substring
            // startsWith            
            if(data!=null && data.size()>0)
            {
                Iterator<String> it=data.keySet().iterator();
                while(it.hasNext())
                {
                    String key=it.next();
                    Object val=data.get(key);
                    
                    ScriptObject field=dataSource.getDataSourceScriptField(key);
                    String type=null;
                    if(field!=null)
                    {
                        field.getString("stype");    
                        if(type==null)type=field.getString("type");
                    }
                    //System.out.println("key:"+key+" type:"+type+" value:"+val);

                    if(val!=null && !key.startsWith("$"))
                    {
                        if(val instanceof String)
                        {
                            String value=(String)val;
                            if(key.equals("_id") || "select".equals(type) || value.startsWith("_suri:"))//is key
                            {
                                
                            }else if(textMatchStyle!=null)
                            {
                                if("substring".equals(textMatchStyle))
                                {
                                    data.put(key, new BasicDBObject().append("$regex",val));
                                }else if("startsWith".equals(textMatchStyle))
                                {
                                    data.put(key, new BasicDBObject().append("$regex","^"+val));
                                }
                            }
                        }else if(val instanceof BasicDBList)
                        {
                            BasicDBList value=(BasicDBList)val;
                            if(key.equals("_id"))
                            {
                                data.put(key, new BasicDBObject().append("$in",val));
                            }else
                            {
                                BasicDBObject all=new BasicDBObject();
                                all.put("$all", val);
                                data.put(key, all);
                            }
                        }
                    }
                }
            }

            //System.out.println("find:"+scls+" "+data);
            log.fine("find: "+scls+" "+data);
            DBCursor cur = coll.find(data);
            int total=cur.count();

            //Sort
            if(sortBy!=null)
            {
                BasicDBObject sort=new BasicDBObject();
                for(int x=0;x<sortBy.size();x++)
                {
                    String field=(String)sortBy.get(x);
                    if(field.startsWith("-"))
                    {
                        sort.append(field.substring(1), -1);
                    }else
                    {
                        sort.append(field, 1);
                    }
                }
                cur.sort(sort);
            }            
            
            if(startRow>0)cur.skip(startRow);
            if(endRow>0)cur.limit(endRow-startRow);
            
            return new DataObjectIteratorMongo(cur, total);
        }finally
        {
//            mongoClient.close();
        }
    }        
    
    /**
     *
     * @param dson
     * @param dataSource
     * @return
     * @throws IOException
     */
    public DataObject fetch(DataObject dson, SWBDataSource dataSource) throws IOException
    {
        BasicDBObject json=toBasicDBObject(dson);
        DataObjectIterator it=find(dson,dataSource);
        try
        {
            int startRow = json.getInt("startRow",0);
            int endRow = json.getInt("endRow",0);

            DataObject ret=new DataObject();
            DataObject resp=new DataObject();
            DataList ndata=new DataList();
            ret.addParam("response", resp);
            resp.addParam("status", 0);
            resp.addParam("startRow", startRow);
            resp.addParam("data", ndata);

            int total=it.total();

            int endrow=startRow;
            try
            {
                while (it.hasNext())
                {
                    DataObject dbobj = it.next();
                    ndata.add(dbobj);
                    endrow++;
                    if(endrow==endRow)break;
                }
            } finally
            {
                it.close();
            }            
            resp.addParam("endRow", endrow);
            resp.addParam("totalRows", total);   
            //System.out.println("fetch ret:"+ret);
            
            //Sort based of ids
            try
            {
                DataObject data=dson.getDataObject("data");
                //System.out.println("data:"+data);
                if(data!=null)
                {
                    Object val=data.get("_id");
                    //System.out.println("val:"+val);
                    if(val!=null && val instanceof DataList)
                    {
                        DataList vals=(DataList)val;
                        //System.out.println("val:"+val);
                        for(int i=0;i<vals.size();i++)
                        {
                            String id=vals.getString(i);
                            //System.out.println("id:"+id);
                            for(int j=0;j<ndata.size();j++)
                            {
                                DataObject tmp=ndata.getDataObject(j);
                                //System.out.println("tmp:"+tmp);
                                if(id.equals(tmp.getId()))
                                {
                                    //System.out.println("remove:"+i);
                                    ndata.remove(j);
                                    ndata.add(tmp);
                                    break;
                                }
                            }                             
                        }                        
                    }            
                }    
            }catch(Exception e)
            {
                e.printStackTrace();
            } 
            return ret;     
        }finally
        {
//            mongoClient.close();
        }
    }    
    
    /**
     *
     * @param dson
     * @param dataSource
     * @return
     * @throws IOException
     */
    public DataObject aggregate(DataObject dson, SWBDataSource dataSource) throws IOException
    {
        BasicDBObject json=toBasicDBObject(dson);
        
//        MongoClient mongoClient = new MongoClient("localhost");
        try
        {
            String modelid=dataSource.getModelId();
            String scls=dataSource.getClassName();
            DB db = mongoClient.getDB(modelid);
            DBCollection coll = db.getCollection(scls);

            int startRow = json.getInt("startRow",0);
            int endRow = json.getInt("endRow",0);
            
            List data=null;
            Object d=json.get("data");
            if(d instanceof BasicDBList)
            {
                data=(BasicDBList)d;
            }else if(d instanceof BasicDBObject)
            {
                data=new BasicDBList();
                data.add(d);
            }            

            BasicDBObject ret=new BasicDBObject();
            BasicDBObject resp=new BasicDBObject();
            BasicDBList ndata=new BasicDBList();
            ret.append("response", resp);
            resp.append("status", 0);
            resp.append("startRow", startRow);
            resp.append("data", ndata);

            //System.out.println("find:"+scls+" "+data);
            log.fine("agregate: "+scls+" "+data);
            AggregationOutput aggrout = coll.aggregate(data);
            
            int total=0;
            
            Iterator<DBObject> it= aggrout.results().iterator();
            while(it.hasNext())
            {
                DBObject obj=it.next();
                if(total>=startRow)
                {
                    ndata.add(obj);
                }
                total++;
                if(total==endRow)break;                
            }            

            resp.append("endRow", endRow);
            resp.append("totalRows", total);   
            //System.out.println("fetach:"+ret);
            return toDataObject(ret);        
        }finally
        {
//            mongoClient.close();
        }
    }        
    
    /**
     *
     * @param dson
     * @param dataSource
     * @return
     * @throws IOException
     */
    public DataObject add(DataObject dson, SWBDataSource dataSource) throws IOException
    {
        BasicDBObject json=toBasicDBObject(dson);
        log.finest("Adding: "+json.toString());
//        MongoClient mongoClient = new MongoClient("localhost");
        try
        {        
            initDB();
            String modelid=dataSource.getModelId();
            String scls=dataSource.getClassName();
            DB db = mongoClient.getDB(modelid);
            DBCollection coll = db.getCollection(scls);

            BasicDBObject data = (BasicDBObject)json.get("data");

            BasicDBObject obj=data;//copyJSONObject(data);
            if(obj.getString("_id")==null)
            {
                ObjectId id = new ObjectId();
                obj.append("_id", dataSource.getBaseUri()+id.toString());
                //obj.append("_id", id);
            }
            coll.insert(obj);            

            BasicDBObject ret=new BasicDBObject();
            BasicDBObject resp=new BasicDBObject();
            ret.append("response", resp);
            resp.append("status", 0);
            resp.append("data", obj);
            return toDataObject(ret);   
        }finally
        {
//            mongoClient.close();
        }
    }
    
    /**
     *
     * @param dson
     * @param dataSource
     * @return
     * @throws IOException
     */
    public DataObject remove(DataObject dson, SWBDataSource dataSource) throws IOException
    {
        BasicDBObject json=toBasicDBObject(dson);
//        MongoClient mongoClient = new MongoClient("localhost");
        try
        {
            initDB();
            String modelid=dataSource.getModelId();
            String scls=dataSource.getClassName();
            DB db = mongoClient.getDB(modelid);
            DBCollection coll = db.getCollection(scls);

            BasicDBObject data = (BasicDBObject)json.get("data");
            
            boolean removeByID=json.getBoolean("removeByID",true);

            DBObject base=null;
            //ObjectId id=getObjectId(data);
            if(removeByID)
            {
                String id=data.getString("_id");
                BasicDBObject search=new BasicDBObject().append("_id", id);
                base=coll.findAndRemove(search);
            }else
            {
                coll.remove(data);                
            }

            BasicDBObject ret=new BasicDBObject();
            BasicDBObject resp=new BasicDBObject();
            ret.append("response", resp);
            resp.append("status", 0);

            return toDataObject(ret);   
        }finally
        {
//            mongoClient.close();
        }
    }    
    
    /**
     *
     * @param dson
     * @param dataSource
     * @return
     * @throws IOException
     */
    public DataObject update(DataObject dson, SWBDataSource dataSource) throws IOException
    {
        BasicDBObject json=toBasicDBObject(dson);
//        MongoClient mongoClient = new MongoClient("localhost");
        try
        {        
            initDB();
            String modelid=dataSource.getModelId();
            String scls=dataSource.getClassName();
            DB db = mongoClient.getDB(modelid);
            DBCollection coll = db.getCollection(scls);

            BasicDBObject data = (BasicDBObject)json.get("data");
            BasicDBObject oldValues = (BasicDBObject)json.get("oldValues");

            String id=data.getString("_id");
            BasicDBObject search=new BasicDBObject().append("_id", id);

            DBObject base=coll.findOne(search);
            //System.out.println("base"+base);

            data.remove("_id");
            DBObject obj=copyDBObjectOldValues(base,data,oldValues);
            //data.markAsPartialObject();
            //DBObject obj=coll.findAndModify(search, data);
            coll.save(obj);

            BasicDBObject ret=new BasicDBObject();
            BasicDBObject resp=new BasicDBObject();
            ret.append("response", resp);
            resp.append("status", 0);
            resp.append("data", obj);

            return toDataObject(ret);   
        }finally
        {
//            mongoClient.close();
        }
    }            
    
    private DBObject copyDBObject(DBObject base, DBObject jobj)
    {
        Iterator<String> it = jobj.keySet().iterator();
        while (it.hasNext())
        {
            String key = it.next();
            base.put(key, jobj.get(key));
        }
        return base;
    } 

    private DBObject copyDBObjectOldValues(DBObject base, DBObject jobj, DBObject oobj)
    {
        Iterator<String> it = jobj.keySet().iterator();
        while (it.hasNext())
        {
            String key = it.next();
            Object val=jobj.get(key);
            //System.out.println("key:"+key);
            //System.out.println("val:"+val);
            //System.out.println("oobj:"+oobj);
            if(oobj!=null && ((val!=null && val.equals(oobj.get(key))) || (val==null && oobj.get(key)==null)))continue;
            base.put(key, jobj.get(key));
        }
        return base;
    }    
    
    /**
     *
     */
    public void close()
    { 
        //System.out.println("Close DataStoreMongo...");
        if(mongoClient!=null)
        {            
            mongoClient.close();
            mongoClient=null;
        }
    }
    
    /**
     *
     * @param obj
     * @return
     */
    public Object toBasicDB(Object obj)
    {
        if(obj instanceof DataObject)
        {
            return toBasicDBObject((DataObject)obj);
        }else if(obj instanceof DataList)
        {
            return toBasicDBList((DataList)obj);
        }
        return obj;  
    }    
    
    /**
     *
     * @param obj
     * @return
     */
    public BasicDBList toBasicDBList(DataList obj)
    {
        BasicDBList ret=new BasicDBList();
        Iterator it=obj.iterator();
        while (it.hasNext()) {
            ret.add(toBasicDB(it.next()));
        }
        return ret;
    }    

    /**
     *
     * @param obj
     * @return
     */
    public BasicDBObject toBasicDBObject(DataObject obj)
    {
        BasicDBObject ret=new BasicDBObject();
        Iterator<Map.Entry<String,Object>> it=obj.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Object> entry = it.next();
            //if(!entry.getKey().startsWith("$"))
            //{
                ret.put(entry.getKey(), toBasicDB(entry.getValue()));
            //}
        }
        return ret;
    }
    
    /**
     *
     * @param obj
     * @return
     */
    public static Object toData(Object obj)
    {
        if(obj instanceof BasicDBObject)
        {
            return toDataObject((BasicDBObject)obj);
        }else if(obj instanceof BasicDBList)
        {
            return toDataList((BasicDBList)obj);
        }
        return obj;  
    }    
    
    /**
     *
     * @param obj
     * @return
     */
    public static DataList toDataList(BasicDBList obj)
    {
        DataList ret=new DataList();
        Iterator it=obj.iterator();
        while (it.hasNext()) {
            ret.add(toData(it.next()));
        }
        return ret;
    }    

    /**
     *
     * @param obj
     * @return
     */
    public static DataObject toDataObject(BasicDBObject obj)
    {
        DataObject ret=new DataObject();
        Iterator<Map.Entry<String,Object>> it=obj.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Object> entry = it.next();
            ret.put(entry.getKey(), toData(entry.getValue()));
        }
        return ret;
    }    
    
    /**
     *
     * @param json
     * @return
     */
    public static Object parseJSON(String json)
    {
        return toData(JSON.parse(json));
    }

    /**
     *
     * @return
     */
    public MongoClient getMongoClient() {
        return mongoClient;
    }        
}
