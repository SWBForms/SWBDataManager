/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.semanticwb.datamanager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 *
 * @author javiersolis
 */
public class DataMgr 
{
    private static DataMgr instance=null;
    
    private static ScriptEngineManager factory = null;
    
    private static String applicationPath=null;
    
    private String baseDatasource=null;
    
    private DataMgr(String applicationPath)
    {
        System.out.println("Initializing DataMgr:"+applicationPath);        
        instance=this;
        
        factory = new ScriptEngineManager(); 
        
        DataMgr.applicationPath=applicationPath;
        
        //DataSource.init(applicationPath+"WEB-INF/datasources/");   
        RoutesMgr.startup();
    }
    
    public static SWBScriptEngine initPlatform(javax.servlet.http.HttpSession session)
    {
        return DataMgr.getUserScriptEngine("[GLOBAL]", session, false);          
    }    
    
    public static SWBScriptEngine initPlatform(String path, javax.servlet.http.HttpSession session)
    {
        return DataMgr.getUserScriptEngine(path, session, false);          
    }
    
    public void setBaseDatasourse(String baseDatasource)
    {
        this.baseDatasource=baseDatasource;
    }

    public String getBaseDatasource() {
        return baseDatasource;
    }
    
    public static DataMgr getBaseInstance()
    {
        return instance;
    }    
    
    public static DataMgr createInstance(String applicationPath)
    {
        if(instance==null)
        {
            synchronized(DataMgr.class)
            {
                if(instance==null)
                {
                    new DataMgr(applicationPath);
                }
            }
        }
        return instance;
    }

    public static String getApplicationPath() {
        return applicationPath;
    }
    
    
    public static ScriptEngine getNativeScriptEngine()
    {
        // create a JavaScript engine         
        //System.out.println(new NashornScriptEngineFactory().getParameter("THREADING"));
        return factory.getEngineByName("JavaScript");           
    }
    
    /**
     * Relaltive to Application path
     * @param source
     * @param engine 
     */
    protected static ScriptEngine loadScript(String source, ScriptEngine engine) throws IOException, ScriptException
    {
        System.out.println("loadScript:"+source);        
        File f=new File(instance.applicationPath+source);
        //System.out.println(f.getPath()+" "+f.exists());
        
        if(f.isFile())
        {
            //Carga Script de inicializacion
            FileInputStream in=new FileInputStream(f);
            InputStreamReader r=new InputStreamReader(in,"UTF8");            
            engine.eval(r);                                
        } 
        return engine;
    }
    
    /**
     * Relaltive to Class path
     * @param source
     * @param engine 
     */
    protected static ScriptEngine loadLocalScript(String source, ScriptEngine engine) throws IOException, ScriptException
    {
        System.out.println("loadLocalScript:"+source);  
        InputStream r=DataMgr.class.getResourceAsStream(source);
        //System.out.println("r:"+r);
        if(r!=null)
        {
            engine.eval(new InputStreamReader(r,"UTF8"));                                
        } 
        return engine;
    }   
/*    
    /**
     * Regresa ScriptEngine asociado al archivo js de datasources relativo al classpath
     * @param source ruta del archivo js de datasources relativo al classpath
     * @return SWBScriptEngine
     * /
    public static SWBScriptEngine getScriptEngine(String source)
    {
        return getScriptEngine(source, true);
    }   
    
    
    /**
     * Regresa ScriptEngine asociado al archivo js de datasources 
     * @param source ruta del archivo js de datasources
     * @param internal, si es true la ruta es relativa al classpath, de lo contrario es relativa al workpath
     * @return SWBScriptEngine
     * /
    public static SWBScriptEngine getScriptEngine(String source, boolean internal)
    {
        SWBBaseScriptEngine engine=SWBBaseScriptEngine.getScriptEngine(source,internal);
        return engine;
    }
*/    
    
    /**
     * Regresa ScriptEngine asociado al archivo js de datasources relativo al classpath
     * @param source ruta del archivo js de datasources
     * @param user datos del usuario
     * @return SWBScriptEngine
     */
    public static SWBScriptEngine getUserScriptEngine(String source, DataObject user)
    {
        return getUserScriptEngine(source, user, true);
    }    
    
    /**
     * Regresa ScriptEngine asociado al archivo js de datasources 
     * @param source ruta del archivo js de datasources
     * @param user datos del usuario
     * @param internal si es true la ruta es relativa al classpath, de lo contrario es relativa al workpath
     * @return SWBScriptEngine
     */
    public static SWBScriptEngine getUserScriptEngine(String source, DataObject user, boolean internal)
    {
        SWBBaseScriptEngine engine=SWBBaseScriptEngine.getScriptEngine(source,internal);
        if(engine!=null)return new SWBUserScriptEngine(engine,user);
        return null;
    }  
    
    /**
     * Regresa ScriptEngine asociado al archivo js de datasources 
     * @param source ruta del archivo js de datasources
     * @param user datos del usuario
     * @param internal si es true la ruta es relativa al classpath, de lo contrario es relativa al workpath
     * @return SWBScriptEngine
     */
    public static SWBScriptEngine getUserScriptEngine(String source, javax.servlet.http.HttpSession session, boolean internal)
    {
        SWBBaseScriptEngine engine=SWBBaseScriptEngine.getScriptEngine(source,internal);
        if(engine!=null)return new SWBUserScriptEngine(engine,session);
        return null;
    }      
    
    
    
}
