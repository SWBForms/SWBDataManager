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
import org.semanticwb.datamanager.DataMgr;
import org.semanticwb.datamanager.SWBScriptEngine;


/**
 *
 * @author javiersolis
 */
public class ScriptObject 
{
    private Object _obj;
    
    public ScriptObject(Object obj)
    {
        this._obj=obj;
    }
    
    public ScriptObject get(String key)
    {
        Object o=((ScriptObjectMirror)_obj).get(key);
        if(o!=null)return new ScriptObject(o);
        return null;
    }
    
    public String getString(String key)
    {
        Object o=((ScriptObjectMirror)_obj).get(key);
        if(o==null)return null;
        return o.toString();
    }
    
    public int getInt(String key)
    {
        Object o=((ScriptObjectMirror)_obj).get(key);
        if(o==null)return 0;
        return (Integer)o;
    }    
    
    public boolean containsKey(String key)
    {
        return ((ScriptObjectMirror)_obj).containsKey(key);
    }    
    
    public boolean containsValue(Object value)
    {
        return ((ScriptObjectMirror)_obj).containsValue(value);
    }    
        
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
    
    public Object getValue()
    {
        return _obj;
    }
    
    public boolean isScriptObject()
    {
        if(_obj instanceof ScriptObjectMirror)
        {
            return true;
        }
        return false;
    }
    
    public boolean isNativeObject()
    {
        return !isScriptObject();
    }

    public boolean isArray()
    {
        return ((ScriptObjectMirror)_obj).isArray();
    }     
    
    public boolean isEmpty()
    {
        return ((ScriptObjectMirror)_obj).isEmpty();
    }     
    
    public boolean isFunction()
    {
        return ((ScriptObjectMirror)_obj).isFunction();
    }     
    
    public Set<String> keySet()
    {
        return ((ScriptObjectMirror)_obj).keySet();
    }     
    
    public int size()
    {
        return ((ScriptObjectMirror)_obj).size();
    }     
    
    public void clear()
    {
        ((ScriptObjectMirror)_obj).clear();
    }     
    
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
    
    public static ScriptObject parse(String script) throws ScriptException
    {        
        ScriptEngine engine=DataMgr.getNativeScriptEngine();
        return new ScriptObject(engine.eval(script));
    }
    
    public static ScriptObject parse(String script, ScriptEngine engine) throws ScriptException
    {        
        return new ScriptObject(engine.eval(script));
    }    
    
//    public ScriptObject invoke(Object... args)
//    {
//        return new ScriptObject(((ScriptObjectMirror)_obj).call(_obj, args));
//    }
    
    public ScriptObject invoke(SWBScriptEngine b, Object... args)
    {
        return new ScriptObject(((ScriptObjectMirror)_obj).call(b, args));
    }
}
