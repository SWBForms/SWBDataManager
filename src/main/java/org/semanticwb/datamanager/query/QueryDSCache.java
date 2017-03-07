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
import org.semanticwb.datamanager.DataObject;
import org.semanticwb.datamanager.SWBDataSource;

/**
 *
 * @author javiersolis
 */
public class QueryDSCache {
    private static int CACHE_SIZE=1000;
    private SWBDataSource ds;
    private HashMap<String,DataObject> data=new HashMap();
    private int totalRows;

    public QueryDSCache(SWBDataSource ds) throws IOException
    {
        this.ds = ds;
        
        if(ds==null)return;
        DataObject q=new DataObject().addParam("startRow", 0).addParam("endRow", CACHE_SIZE);
        q.addSubObject("data");
        DataObject ret=ds.fetch(q);
        DataObject response=ret.getDataObject("response");
        
        totalRows=response.getInt("totalRows");
        DataList list=response.getDataList("data");
        if(list!=null)
        {
            Iterator<DataObject> it=list.iterator();
            while (it.hasNext()) {
                DataObject obj = it.next();
                data.put(obj.getString("_id"),obj);
            }
        }
    }
    
    public DataObject getRecord(String id) throws IOException
    {
        DataObject obj=data.get(id);
        if(obj==null && size()!=totalRows)
        {
            return ds.fetchObjById(id);                            
        }
        return obj;
    }
    
    public int size()
    {
        return data.size();
    }

    public int getTotalRows() {
        return totalRows;
    }
        
}
