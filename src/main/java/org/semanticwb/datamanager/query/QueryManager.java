/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.semanticwb.datamanager.query;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import org.semanticwb.datamanager.DataList;
import org.semanticwb.datamanager.DataMgr;
import org.semanticwb.datamanager.DataObject;
import org.semanticwb.datamanager.SWBDataSource;
import org.semanticwb.datamanager.SWBScriptEngine;

/**
 *
 * @author javiersolis
 */
public class QueryManager {
    private String datasources;   
    HashMap<String,QueryDSCache> dsCache=new HashMap();
    QueryDefinition definition;
    DataObject user;

    /**
     * 
     * @param datasources 
     * @param user
     * @param def 
     */
    public QueryManager(String datasources, DataObject user, QueryDefinition def) {
        this.datasources=datasources;
        this.definition=def;
        this.user=user;
    }        
    
    /**
     *
     * @throws IOException
     */
    public void preload() throws IOException
    {
        SWBScriptEngine engine = DataMgr.getUserScriptEngine(datasources, user,false);
        Iterator<QueryObject> it=definition.getObjects().values().iterator();
        while (it.hasNext()) {
            QueryObject obj = it.next();
            SWBDataSource ds=engine.getDataSource(obj.getDsName());  
            obj.setDisplayName(ds.getName());
            if(obj!=definition.getRoot())
            {
                dsCache.put(obj.getDsName(),new QueryDSCache(ds));
            }
            
            Iterator<QueryProperty> itp=obj.getProperties().iterator();
            while (itp.hasNext()) {
                QueryProperty prop = itp.next();
                prop.setDisplayName(ds.getDataSourceScriptField(prop.getDsName()).getString("title"));
            }                        
        }
    }

    private void processProps(QueryObject qobj, DataList props) throws IOException
    {
        Iterator<QueryProperty> it=qobj.getProperties().iterator();
        while (it.hasNext()) {
            QueryProperty prop = it.next();
            if(prop.getObject()==null)
            {
                props.add(new DataObject()
                        .addParam("name",prop.getName())
                        .addParam("value",prop.getValue())
                        .addParam("dsname",prop.getDsName())
                        .addParam("title", prop.getDisplayName())
                        .addParam("ds",qobj.getDsName())
                );
            }else
            {
                QueryObject nqobj=prop.getObject();
                processProps(nqobj, props);
            }
        }
    }
    
    private boolean addObject(DataObject in, DataObject out, QueryObject qobj, DataList data) throws IOException
    {
        boolean ret=processObject(in, out, qobj, data);
        if(ret)
        {
            if(out.size()>0)
            {
                data.add(out);
            }           
        }
        return ret;
    }
    
    
    private boolean processObject(DataObject in, DataObject out, QueryObject qobj, DataList data) throws IOException
    {
        if(in==null)return false;
        
        Iterator<QueryProperty> it=qobj.getProperties().iterator();
        while (it.hasNext()) {
            QueryProperty prop = it.next();
            if(prop.getObject()==null)
            {
                boolean add=false;
                String val=prop.getValue();
                Object orig=in.get(prop.getDsName());
                
                //if(orig!=null)System.out.println(val+"="+orig+" "+orig.getClass());
                
                if(val.equals("?"))
                {
                    add=true;
                }else if(val.startsWith("\\"))
                {
                    val=val.substring(1);
                    if(orig!=null && val.equals(orig.toString()))
                    {
                        add=true;
                    }
                }else if(val.startsWith(">="))
                {
                    val=val.substring(2);
                    if(orig instanceof Integer)
                    {
                        try
                        {
                            int v=Integer.parseInt(val);
                            if((Integer)orig>=v)add=true;
                        }catch(NumberFormatException noe){}
                    }else if(orig instanceof Double)
                    {
                        try
                        {
                            double v=Double.parseDouble(val);
                            if((Double)orig>=v)add=true;
                        }catch(NumberFormatException noe){}
                    }else 
                    {
                        if(orig.toString().compareTo(val)>-1)add=true;
                    }
                }else if(val.startsWith("<="))
                {
                    val=val.substring(2);
                    if(orig instanceof Integer)
                    {
                        try
                        {
                            int v=Integer.parseInt(val);
                            if((Integer)orig<=v)add=true;
                        }catch(NumberFormatException noe){}
                    }else if(orig instanceof Double)
                    {
                        try
                        {
                            double v=Double.parseDouble(val);
                            if((Double)orig<=v)add=true;
                        }catch(NumberFormatException noe){}
                    }else 
                    {
                        if(orig.toString().compareTo(val)<1)add=true;
                    }
                }else if(val.startsWith(">"))
                {
                    val=val.substring(1);
                    //System.out.println(">");
                    if(orig instanceof Integer)
                    {
                        //System.out.println(val+">"+orig);
                        try
                        {
                            int v=Integer.parseInt(val);
                            if((Integer)orig>v)add=true;
                        }catch(NumberFormatException noe){}
                        //System.out.println(val+">"+orig+" add:"+add);
                    }else if(orig instanceof Double)
                    {
                        try
                        {
                            double v=Double.parseDouble(val);
                            if((Double)orig>v)add=true;
                        }catch(NumberFormatException noe){}
                    }else 
                    {
                        if(orig.toString().compareTo(val)>0)add=true;
                    }
                }else if(val.startsWith("<"))
                {
                    val=val.substring(1);
                    if(orig instanceof Integer)
                    {
                        try
                        {
                            int v=Integer.parseInt(val);
                            if((Integer)orig<v)add=true;
                        }catch(NumberFormatException noe){}
                    }else if(orig instanceof Double)
                    {
                        try
                        {
                            double v=Double.parseDouble(val);
                            if((Double)orig<v)add=true;
                        }catch(NumberFormatException noe){}
                    }else 
                    {
                        if(orig.toString().compareTo(val)<0)add=true;
                    }
                }else if(orig!=null && val.equals(orig.toString()))
                {
                    add=true;
                }
                if(add)out.put(prop.getName(), orig);
                else return false;
            }else
            {
                QueryObject nqobj=prop.getObject();
                Object oid=in.get(prop.getDsName());
                if(oid!=null)
                {
                    if(oid instanceof DataList)
                    {
                        DataList ids=(DataList)oid;
                        for(int x=0;x<ids.size();x++)
                        {
                            String id=ids.getString(x);
                            System.out.println("nqobj:"+nqobj+" "+nqobj.getDsName()+" "+dsCache.get(nqobj.getDsName()));
                            DataObject nin=dsCache.get(nqobj.getDsName()).getRecord(id);
                            if(x==0)
                            {
                                if(!processObject(nin, out, nqobj, data))
                                {
                                    return false;
                                }                                                          
                            }else
                            {
                                DataObject nout=(DataObject)out.clone();
                                if(!addObject(nin, nout, nqobj, data))
                                {
                                    return false;
                                }                                                                                      
                            }
                        }                    
                    }else
                    {
                        String id=oid.toString();
                        DataObject nin=dsCache.get(nqobj.getDsName()).getRecord(id);

                        if(!processObject(nin, out, nqobj,data))
                        {
                            return false;
                        }                
                    }
                }else
                {
                    System.out.println("Obj:"+prop.getObject()+" DSName:"+prop.getDsName());
                }
            }
        }
        return true;
    }
    
    /**
     *
     * @return
     * @throws IOException
     */
    public DataObject execute() throws IOException
    {
        DataObject ret=new DataObject();
        QueryObject root=definition.getRoot();
        SWBScriptEngine engine = DataMgr.getUserScriptEngine(datasources, user,false);
        DataObject res=engine.getDataSource(root.getDsName()).fetch();
        DataObject response=res.getDataObject("response");
        int totalRows=response.getInt("totalRows");
        DataList list=response.getDataList("data");
        
        DataList props=ret.addSubList("props");  
        processProps(root,props);
        
        DataList data=ret.addSubList("data");
        Iterator<DataObject> it=list.iterator();
        while (it.hasNext()) {
            DataObject in = it.next();
            DataObject out=new DataObject();
            addObject(in, out, root,data);
        }
        return ret;
    }
    
    /**
     *
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException
    {
        QueryDefinition def=new QueryDefinition();
        def.addQueryObject("Facturas", "?Facturas1").addQueryProperty("numero", "?numero1", "?");
        
        QueryManager mgr=new QueryManager("/test/datasources.js",null,def);  
        //DataObject ret=mgr.execute();
        //System.out.println("ret:"+ret);
    }
}
