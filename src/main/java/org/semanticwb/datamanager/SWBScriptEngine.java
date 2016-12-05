/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.semanticwb.datamanager;

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

    public void chechUpdates();

    public Object eval(String script) throws ScriptException;

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

    public SWBDataSource getDataSource(String name);
    
    public SWBDataSource getDataSource(String name, String modelid);    
    
    public Set<String> getDataSourceNames();

    public SWBDataStore getDataStore(String name);
    
    public SWBFileSource getFileSource(String name);

    public ScriptEngine getNativeScriptEngine();
    
    public ScriptObject getScriptObject();

    public DataObject invokeDataProcessors(String dataSource, String action, String method, DataObject obj);

    public void invokeDataServices(String dataSource, String action, DataObject request, DataObject response);

    public void reloadScriptEngine();
    
    public Bindings getUserBindings();
    
    public DataObject getUser();
    
    public Object getContextData(String key);
    
    public Object setContextData(String key, Object data); 
    
    public SWBScriptUtils getUtils();
    
    public void close();
    
    public boolean isClosed();
}
