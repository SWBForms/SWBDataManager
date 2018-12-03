/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.semanticwb.datamanager.script;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import org.semanticwb.datamanager.DataList;
import org.semanticwb.datamanager.DataMgr;
import org.semanticwb.datamanager.DataObject;
import org.semanticwb.datamanager.DataUtils;
import org.semanticwb.datamanager.SWBScriptEngine;


/**
 *
 * @author javiersolis
 */
public class ScriptObject 
{
    private Object _obj;
    
    /**
     *
     * @param obj
     */
    public ScriptObject(Object obj)
    {
        this._obj=obj;
    }
    
    /**
     *
     * @param key
     * @return
     */
    public ScriptObject get(String key)
    {
        Object o=((ScriptObjectMirror)_obj).get(key);
        if(o!=null)return new ScriptObject(o);
        return null;
    }
    
    /**
     *
     * @param key
     * @return
     */
    public String getString(String key)
    {
        Object o=((ScriptObjectMirror)_obj).get(key);
        if(o==null)return null;
        return o.toString();
    }
    
    /**
     *
     * @param key
     * @return
     */
    public int getInt(String key)
    {
        Object o=((ScriptObjectMirror)_obj).get(key);
        if(o==null)return 0;
        return (Integer)o;
    }    
    
    /**
     *
     * @param key
     * @return
     */
    public boolean containsKey(String key)
    {
        return ((ScriptObjectMirror)_obj).containsKey(key);
    }    
    
    /**
     *
     * @param value
     * @return
     */
    public boolean containsValue(Object value)
    {
        return ((ScriptObjectMirror)_obj).containsValue(value);
    }    
        
    /**
     *
     * @param key
     * @return
     */
    public boolean delete(String key)
    {
        return ((ScriptObjectMirror)_obj).delete(key);
    }     

    @Override
    public boolean equals(Object obj) 
    {
        if(obj instanceof ScriptObject)
        {
            return _obj.equals(((ScriptObject)obj).getValue());
        }
        return super.equals(obj); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int hashCode() {
        return _obj.hashCode();
    }
    
    /**
     *
     * @return
     */
    public Object getValue()
    {
        return _obj;
    }
    
    /**
     *
     * @return
     */
    public boolean isScriptObject()
    {
        if(_obj instanceof ScriptObjectMirror)
        {
            return true;
        }
        return false;
    }
    
    /**
     *
     * @return
     */
    public boolean isNativeObject()
    {
        return !isScriptObject();
    }

    /**
     *
     * @return
     */
    public boolean isArray()
    {
        return ((ScriptObjectMirror)_obj).isArray();
    }     
    
    /**
     *
     * @return
     */
    public boolean isEmpty()
    {
        return ((ScriptObjectMirror)_obj).isEmpty();
    }     
    
    /**
     *
     * @return
     */
    public boolean isFunction()
    {
        return ((ScriptObjectMirror)_obj).isFunction();
    }     
    
    /**
     *
     * @return
     */
    public Set<String> keySet()
    {
        return ((ScriptObjectMirror)_obj).keySet();
    }     
    
    /**
     *
     * @return
     */
    public int size()
    {
        return ((ScriptObjectMirror)_obj).size();
    }     
    
    /**
     *
     */
    public void clear()
    {
        ((ScriptObjectMirror)_obj).clear();
    }     
    
    /**
     *
     * @param key
     * @param obj
     */
    public void put(String key, Object obj)
    {
        if(obj instanceof ScriptObject)
        {
            ((ScriptObjectMirror)_obj).put(key, ((ScriptObject)obj).getValue());
        }else
        {
            ((ScriptObjectMirror)_obj).put(key, obj);
        }
    }     
        
    /**
     *
     * @return
     */
    public List<ScriptObject> values()
    {
        Iterator<Object> it=((ScriptObjectMirror)_obj).values().iterator();
        ArrayList<ScriptObject> arr=new ArrayList();
        while (it.hasNext()) {
            Object object = it.next();
            arr.add(new ScriptObject(object));
        }
        return arr;
    }      
    
    /**
     *
     * @param script
     * @return
     * @throws ScriptException
     */
    public static ScriptObject parse(String script) throws ScriptException
    {        
        ScriptEngine engine=DataMgr.getNativeScriptEngine();
        return new ScriptObject(engine.eval(script));
    }
    
    /**
     *
     * @param script
     * @param engine
     * @return
     * @throws ScriptException
     */
    public static ScriptObject parse(String script, ScriptEngine engine) throws ScriptException
    {        
        return new ScriptObject(engine.eval(script));
    }    
    
//    public ScriptObject invoke(Object... args)
//    {
//        return new ScriptObject(((ScriptObjectMirror)_obj).call(_obj, args));
//    }

    /**
     *
     * @param b
     * @param args
     * @return
     */
    
    public ScriptObject invoke(SWBScriptEngine b, Object... args)
    {
        return new ScriptObject(((ScriptObjectMirror)_obj).call(b, args));
    }
    
    /**
     *
     * @return
     */
    public DataObject toDataObject()
    {
        return DataUtils.toDataObject((ScriptObjectMirror)_obj);
    }
    
    /**
     *
     * @return
     */
    public DataList toDataList()
    {
        return DataUtils.toDataList((ScriptObjectMirror)_obj);
    }
    
    /**
     *
     * @return
     */
    public Object toData()
    {
        return DataUtils.toData(_obj);
    }
    
}
