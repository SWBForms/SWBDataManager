/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.semanticwb.datamanager.datastore;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import org.semanticwb.datamanager.DataObjectIterator;
import org.semanticwb.datamanager.DataObject;

/**
 *
 * @author javiersolis
 */
public class DataObjectIteratorMongo extends DataObjectIterator
{
    private DBCursor cursor=null;
    private int total=0;

    public DataObjectIteratorMongo(DBCursor cursor, int total) 
    {
        this.cursor=cursor;
        this.total=total;
    }

    public int getTotal() {
        return total;
    }
    
    @Override
    public boolean hasNext() {
        return cursor.hasNext();
    }

    @Override
    public DataObject next() {
        return DataStoreMongo.toDataObject((BasicDBObject)cursor.next());
    }    

    @Override
    public int total() {
        return total;
    }

    @Override
    public int size() {
        return cursor.count();
    }
    
    public void close()
    {
        cursor.close();
    }
}
