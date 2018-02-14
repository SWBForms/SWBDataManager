/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.semanticwb.datamanager;

import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.Set;
import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import org.semanticwb.datamanager.datastore.SWBDataStore;
import org.semanticwb.datamanager.filestore.SWBFileSource;
import org.semanticwb.datamanager.script.ScriptObject;

/**
 *
 * @author javiersolis
 */
public class SWBUserScriptEngine implements SWBScriptEngine
{
    SWBBaseScriptEngine engine=null;
    DataObject user=null;
    javax.servlet.http.HttpSession session=null;
    
    public SWBUserScriptEngine(SWBBaseScriptEngine engine, javax.servlet.http.HttpSession session)
    {
        this.engine=engine;
        this.session=session;
        if(session!=null)
        {
            user = (DataObject) session.getAttribute("_USER_");
        }        
    }
    
    public SWBUserScriptEngine(SWBBaseScriptEngine engine, DataObject user)
    {
        this.engine=engine;
        this.user=user;
    }

    @Override
    public void chechUpdates() {
        engine.chechUpdates();
    }

    @Override
    public Object eval(String script) throws ScriptException {
        return engine.eval(script,engine.getUserBindings(this));
    }

    @Override
    public Object eval(Reader script) throws ScriptException {
        return engine.eval(script,engine.getUserBindings(this));        
    }

    @Override
    public List<SWBDataProcessor> findDataProcessors(String dataSource, String action) {
        return engine.findDataProcessors(dataSource, action);
    }

    @Override
    public List<SWBDataService> findDataServices(String dataSource, String action) {
        return engine.findDataServices(dataSource, action);
    }

    @Override
    public SWBDataSource getDataSource(String name)
    {
        ScriptObject so=engine.getDataSourceScript(name);
        if(so!=null)
        {
            return new SWBDataSource(name,null,so,this);
        }
        return null;
    }
    
    @Override
    public SWBDataSource getDataSource(String name, String modelid)
    {
        ScriptObject so=engine.getDataSourceScript(name);
        if(so!=null)
        {
            return new SWBDataSource(name,modelid,so,this);
        }
        return null;
    }    

    @Override
    public SWBDataStore getDataStore(String name) {
        return engine.getDataStore(name);
    }

    @Override
    public ScriptEngine getNativeScriptEngine() {
        return engine.getNativeScriptEngine();
    }

//    public SWBUserRepository getUserRepository() {
//        return engine.getUserRepository();
//    }

    @Override
    public DataObject invokeDataProcessors(String dataSource, String action, String method, DataObject obj) {
        return engine.invokeDataProcessors(this,dataSource, action, method, obj);
    }

    @Override
    public void invokeDataServices(String dataSource, String action, DataObject request, DataObject response) {
        engine.invokeDataServices(this, dataSource, action, request, response);
    }

    @Override
    public void reloadScriptEngine() {
        engine.reloadScriptEngine();
    }

    @Override
    public DataObject getUser() {
        return user;
    }

    @Override
    public Bindings getUserBindings() {
        return engine.getUserBindings(this);
    }
    
    @Override
    public SWBScriptUtils getUtils() {
        return engine.getUtils();
    } 
    
    @Override
    public ScriptObject getScriptObject() {
        return engine.getScriptObject();
    }

    @Override
    public void close() {
        engine.close();
    }

    @Override
    public boolean isClosed() {
        return engine.isClosed();
    }

    @Override
    public SWBFileSource getFileSource(String name) {
        return engine.getFileSource(name);
    }

    @Override
    public Set<String> getDataSourceNames() {
        return engine.getDataSourceNames();
    }
    
    public Object getContextData(String key)
    {
        if(session!=null)
        {
            return session.getAttribute("ctx_"+key);
        }return null;
    }
    
    public Object setContextData(String key, Object data)
    {
        Object ret=null;
        if(session!=null)
        {
            ret=session.getAttribute("ctx_"+key);
            session.setAttribute("ctx_"+key,data);
        }
        return ret;
    }     

    @Override
    public boolean hasUserRole(String role) {
        DataObject user=getUser();
        if(user!=null)
        {
            DataList roles=user.getDataList("roles");
            if(roles!=null)
            {
                return roles.contains(role);
            }
        }
        return false;
    }
    
    @Override
    public boolean hasUserAnyRole(String... roles) {
        if(roles!=null)
        {
            for(int x=0;x<roles.length;x++)
            {
                if(hasUserRole(roles[x]))return true;
            }
        }
        return false;
    }
    
    @Override
    public boolean hasUserAnyRole(List<String> roles) {
        if(roles!=null)
        {
            for(int x=0;x<roles.size();x++)
            {
                if(hasUserRole(roles.get(x)))return true;
            }
        }
        return false;
    }    

    @Override
    public boolean hasUserGroup(String group) {
        DataObject user=getUser();
        if(user!=null)
        {
            DataList groups=user.getDataList("groups");
            if(groups!=null)
            {
                return groups.contains(group);
            }            
        }
        return false;        
    }
    
    @Override
    public void removeUserPermissionCache()
    {
        if(session!=null)
        {
            session.removeAttribute("_PERMISSIONS_"); 
        }
    }    
    
    protected DataObject getUserPermissions() throws IOException
    {        
        DataObject permissions=null;
        if(session!=null)
        {
            permissions=(DataObject)session.getAttribute("_PERMISSIONS_");
            if(permissions==null)
            {
                synchronized(this)
                {
                    permissions=(DataObject)session.getAttribute("_PERMISSIONS_");
                    if(permissions==null)
                    {        
                        permissions=DataUtils.mapDataSourceByFields(getDataSource("Permission"), "name","roles");        
                        session.setAttribute("_PERMISSIONS_",permissions);        
                        System.out.println("permissions session:"+permissions);
                    }
                }
            }
        }else
        {
            permissions=DataUtils.mapDataSourceByFields(getDataSource("Permission"), "name","roles");    
            System.out.println("permissions:"+permissions);
        }
        return permissions;
    }
    
    @Override
    public boolean hasUserPermission(String permission)
    {        
        DataObject permissions = null;
        try 
        {
            permissions = getUserPermissions();
        } catch (Exception e) 
        {
            e.printStackTrace();
        }
        if (permissions != null) 
        {
            return hasUserAnyRole(permissions.getDataList(permission));
        } else 
        {
            return false;
        }
    }   

    @Override
    public String getAppName() {
        return engine.getAppName();
    }
    
    
}
