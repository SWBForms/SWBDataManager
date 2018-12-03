/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.semanticwb.datamanager;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;

/**
 *
 * @author javiersolis
 * @param <E>
 */
public class DataList<E> extends ArrayList<E>
{
    
    /**
     * Add obj if is diferent to null, else add def
     * @param index
     * @param obj
     * @param def
     * @return 
     */
    public boolean add(int index, E obj, E def)
    {
        if(obj==null)
        {
            add(index, def);
            return false;
        }else
        {
            add(index, obj);
            return true;
        }
    }
    
    /**
     * Add obj if is diferent to null, else add def
     * @param obj
     * @param def
     * @return 
     */
    public boolean add(E obj, E def)
    {
        if(obj==null)
        {
            add(def);
            return false;
        }else
        {
            add(obj);
            return true;
        }
    }
    
    /**
     *
     * @param index
     * @return
     */
    public String getString(int index) {
        Object obj=get(index);
        if(obj==null)return null;
        return obj.toString();
    }
    
    /**
     *
     * @param index
     * @return
     */
    public DataObject getDataObject(int index)
    {
        Object obj=get(index);
        if(obj instanceof DataObject)return (DataObject)obj;
        return null;
    }
    
    /**
     *
     * @param property
     * @param value
     * @return
     */
    public DataObject findDataObject(String property, Object value)
    {
        for(int x=0;x<size();x++)
        {
            Object val=getDataObject(x).get(property);
            if(val!=null && val.equals(value))return getDataObject(x);
        }
        return null;
    }
    
    /**
     *
     * @param index
     * @return
     */
    public DataList getDataList(int index)
    {
        Object obj=get(index);
        if(obj instanceof DataList)return (DataList)obj;
        return null;
    }    
    
    /**
     *
     * @param index
     * @return
     */
    public int getInt(int index)
    {
        Object obj=get(index);
        if(obj instanceof Integer)return (Integer)obj;
        try
        {
            return Integer.parseInt(getString(index));
        }catch(Exception e)
        {
            e.printStackTrace();
        }
        return 0;
    }
    
    /**
     *
     * @param index
     * @return
     */
    public long getLong(int index)
    {
        Object obj=get(index);
        if(obj instanceof Long)return (Long)obj;
        try
        {
            return Long.parseLong(getString(index));
        }catch(Exception e)
        {
            e.printStackTrace();
        }
        return 0;
    }   
    
    /**
     *
     * @param index
     * @return
     */
    public boolean getBoolean(int index)
    {
        Object obj=get(index);
        if(obj instanceof Boolean)return (Boolean)obj;
        try
        {
            return Boolean.parseBoolean(getString(index));
        }catch(Exception e)
        {
            e.printStackTrace();
        }
        return false;
    }          
    
    public String toString() 
    {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        Iterator it=iterator();
        while (it.hasNext()) {
            Object object = it.next();
            if(object instanceof String)
            {
                sb.append(DataObject.encodeString((String)object,true));
            }else
            {
                sb.append(object);
            }
            if(it.hasNext())sb.append(", ");
        }
        sb.append("]");
        return sb.toString();
    }     
    
}
