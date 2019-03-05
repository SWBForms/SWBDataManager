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
public interface SWBScriptEngine 
{

    /**
     *
     * @return
     */
    public String getAppName();
    
    /**
     * Regresa configuracion de cache
     * @return boolean
     */
    public boolean getDSCache();

    /**
     *
     */
    public void chechUpdates();

    /**
     *
     * @param script
     * @return
     * @throws ScriptException
     */
    public Object eval(String script) throws ScriptException;

    /**
     *
     * @param script
     * @return
     * @throws ScriptException
     */
    public Object eval(Reader script) throws ScriptException;

    /**
     * Busca los objetos SWBDataProcessor relacionados a un especifico DataSource y una accion
     * @param dataSource
     * @param action
     * @return Lista de SWBDataProcessor o null si no hay SWBDataService relacionados
     */
    public List<SWBDataProcessor> findDataProcessors(String dataSource, String action);

    /**
     * Busca los objetos SWBDataService relacionados a un especifico DataSource y una accion
     * @param dataSource
     * @param action
     * @return Lista de SWBDataService o null si no hay SWBDataService relacionados
     */
    public List<SWBDataService> findDataServices(String dataSource, String action);

    /**
     *
     * @param name
     * @return
     */
    public SWBDataSource getDataSource(String name);
    
    /**
     *
     * @param name
     * @param modelid
     * @return
     */
    public SWBDataSource getDataSource(String name, String modelid);    
    
    /**
     *
     * @return
     */
    public Set<String> getDataSourceNames();

    /**
     *
     * @param name
     * @return
     */
    public SWBDataStore getDataStore(String name);
    
    /**
     *
     * @param name
     * @return
     */
    public SWBFileSource getFileSource(String name);

    /**
     *
     * @return
     */
    public ScriptEngine getNativeScriptEngine();
    
    /**
     *
     * @return
     */
    public ScriptObject getScriptObject();

    /**
     *
     * @param dataSource
     * @param action
     * @param method
     * @param obj
     * @return
     */
    public DataObject invokeDataProcessors(String dataSource, String action, String method, DataObject obj);

    /**
     *
     * @param dataSource
     * @param action
     * @param request
     * @param response
     */
    public void invokeDataServices(String dataSource, String action, DataObject request, DataObject response);

    /**
     *
     */
    public void reloadScriptEngine();
    
    /**
     *
     */
    public void reloadAllScriptEngines();
    
    /**
     *
     */
    public void needsReloadAllScriptEngines();
    
    /**
     *
     */
    public void needsReloadScriptEngine();
    
    /**
     *
     * @return
     */
    public boolean isNeedsReloadScriptEngine();
    
    /**
     *
     * @return
     */
    public Bindings getUserBindings();
    
    /**
     *
     * @return
     */
    public DataObject getUser();
    
    /**
     *
     * @param role
     * @return
     */
    public boolean hasUserRole(String role);
    
    /**
     *
     * @param roles
     * @return
     */
    public boolean hasUserAnyRole(String... roles);
    
    /**
     *
     * @param roles
     * @return
     */
    public boolean hasUserAnyRole(List<String> roles);
    
    /**
     *
     * @param group
     * @return
     */
    public boolean hasUserGroup(String group);
    
    /**
     *
     */
    public void removeUserPermissionCache();
    
    /**
     *
     * @param permission
     * @return
     */
    public boolean hasUserPermission(String permission);
    
    /**
     *
     * @param key
     * @return
     */
    public Object getContextData(String key);
    
    /**
     *
     * @param key
     * @param data
     * @return
     */
    public Object setContextData(String key, Object data); 
    
    /**
     *
     * @return
     */
    public SWBScriptUtils getUtils();
    
    /**
     *
     */
    public void close();
    
    /**
     *
     * @return
     */
    public boolean isClosed();
    
    /**
     *
     * @return
     */
    public long getId();
    
    /**
     * return DataObject fetch Object based on Id (Auto discover DataSource)
     * @param id
     * @return 
     * @throws java.io.IOException 
     */
    public DataObject fetchObjectById(String id) throws IOException;
    
    /**
     * return DataObject get Object from cache based on Id (Auto discover DataSource)
     * @param id
     * @return 
     */    
    public DataObject getObjectById(String id);
    
    /**
     * Disable DataProcessors, DataServices and DataTransformations
     * @param disabledDataTransforms boolean
     */
    public void setDisabledDataTransforms(boolean disabledDataTransforms);

    /**
     * return true if is disable DataProcessors, DataServices and DataTransformations
     * @return boolean
     */
    public boolean isDisabledDataTransforms();

}
