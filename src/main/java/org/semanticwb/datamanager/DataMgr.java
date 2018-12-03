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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.servlet.ServletContext;

/**
 *
 * @author javiersolis
 */
public class DataMgr 
{
    private static final Logger logger = Logger.getLogger(DataMgr.class.getName());
    
    private static DataMgr instance=null;
    
    private static ScriptEngineManager factory = null;
    
    private static String applicationPath=null;
    
    private static ServletContext context=null;
    
    private String baseDatasource=null;
    
    private DataMgr(String applicationPath)
    {
        logger.log(Level.INFO,"Initializing DataMgr:"+applicationPath);        
        instance=this;
        
        factory = new ScriptEngineManager(); 
        
        DataMgr.applicationPath=applicationPath;
        
        //DataSource.init(applicationPath+"WEB-INF/datasources/");   
        RoutesMgr.startup();
    }
    
    /**
     *
     * @param session
     * @return
     */
    public static SWBScriptEngine initPlatform(javax.servlet.http.HttpSession session)
    {
        return DataMgr.getUserScriptEngine("[GLOBAL]", session, false);          
    }    
    
    /**
     *
     * @param path
     * @param session
     * @return
     */
    public static SWBScriptEngine initPlatform(String path, javax.servlet.http.HttpSession session)
    {
        return DataMgr.getUserScriptEngine(path, session, false);          
    }
    
    /**
     *
     * @param path
     * @param session
     * @param internal
     * @return
     */
    public static SWBScriptEngine initPlatform(String path, javax.servlet.http.HttpSession session, boolean internal)
    {
        return DataMgr.getUserScriptEngine(path, session, internal);          
    }
    
    /**
     *
     * @param baseDatasource
     */
    public void setBaseDatasourse(String baseDatasource)
    {
        this.baseDatasource=baseDatasource;
    }

    /**
     *
     * @return
     */
    public String getBaseDatasource() {
        return baseDatasource;
    }
    
    /**
     *
     * @return
     */
    public static DataMgr getBaseInstance()
    {
        return instance;
    }    
    
    /**
     *
     * @param applicationPath
     * @return
     */
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
    
    /**
     *
     * @param ctx
     * @return
     */
    public static DataMgr createInstance(ServletContext ctx)
    {
        context=ctx;
        return createInstance(ctx.getRealPath("/"));
    }

    /**
     *
     * @return
     */
    public static String getApplicationPath() {
        return applicationPath;
    }
/*    
    public static String readApplicationFile(String source)throws IOException
    {
        return readApplicationFile(source,"UTF8");
    }    
    
    public static String readApplicationFile(String source, String encode)throws IOException
    {
        logger.log(Level.FINE,"readApplicationFile:"+source);        
        File f=new File(instance.applicationPath+source);
        //System.out.println(f.getPath()+" "+f.exists());
        
        if(f.isFile())
        {
            //Carga Script de inicializacion
            FileInputStream in=new FileInputStream(f);
            if(encode!=null)
                return DataUtils.readInputStream(in, encode);
            else 
                return DataUtils.readInputStream(in);
        } 
        return null;        
    }
*/    
    
    /**
     *
     * @return
     */
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
     * @return  
     * @throws java.io.IOException  
     * @throws javax.script.ScriptException  
     */
    protected static ScriptEngine loadScript(String source, ScriptEngine engine) throws IOException, ScriptException
    {
        logger.log(Level.FINE,"loadScript:"+source);        
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
     * @return  
     * @throws java.io.IOException 
     * @throws javax.script.ScriptException 
     */
    protected static ScriptEngine loadLocalScript(String source, ScriptEngine engine) throws IOException, ScriptException
    {
        logger.log(Level.FINE,"loadLocalScript:"+source);  
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
     * @param session
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
