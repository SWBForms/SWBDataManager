/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.semanticwb.datamanager;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.Timer;
import java.util.TimerTask;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import org.semanticwb.datamanager.extractors.ScriptExtractor;
import org.semanticwb.datamanager.script.ScriptObject;

/**
 *
 * @author javiersolis
 */
public class DataExtractorBaseImp implements DataExtractorBase
{
    private String name=null;
    private SWBScriptEngine scriptEngine=null;
    private ScriptObject scriptObject=null;
    private SWBDataSource dataSource=null;
    private Timer timer=null;
    private DataExtractor extractor=null;

    protected DataExtractorBaseImp(String name, ScriptObject script, SWBScriptEngine engine)
    {
        this.name=name;
        this.scriptEngine=engine;
        this.scriptObject=script;        
        String dataSourceName=this.scriptObject.getString("dataSource");
        this.dataSource=engine.getDataSource(dataSourceName);        
        if(this.dataSource==null)throw new NoSuchFieldError("DataSource not found:"+dataSourceName);
        
        System.out.println("Loading DataExtractor:"+name);
        String dataClass=script.getString("class");
        if(dataClass!=null)
        {
            try
            {
                Class cls=Class.forName(dataClass);
                Constructor c=cls.getConstructor();
                extractor=(DataExtractor)c.newInstance();
            }catch(Exception e){e.printStackTrace();} 
        }else 
        {
            extractor=new ScriptExtractor();
        }
    }

    public void store(DataObject data) throws IOException
    {
        dataSource.add(data);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public SWBScriptEngine getScriptEngine() {
        return scriptEngine;
    }

    public void setScriptEngine(SWBScriptEngine scriptEngine) {
        this.scriptEngine = scriptEngine;
    }

    public ScriptObject getScriptObject() {
        return scriptObject;
    }

    public void setScriptObject(ScriptObject scriptObject) {
        this.scriptObject = scriptObject;
    }

    public SWBDataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(SWBDataSource dataSource) {
        this.dataSource = dataSource;
    }
    
    public void extract() throws IOException
    {
        extractor.extract(this);
    }
    
    public void start()
    {
        ScriptObject t=getScriptObject().get("timer");
        if(t!=null)
        {
            long time=t.getInt("time");
            String unit=t.getString("unit");
            if(unit!=null)
            {
                if(unit.equals("s"))time=time*1000;
                if(unit.equals("m"))time=time*1000*60;
                if(unit.equals("h"))time=time*1000*60*60;
                if(unit.equals("d"))time=time*1000*60*60*24;
            }
            
            final DataExtractorBase base=this;
            
            timer=new Timer();
            timer.schedule(new TimerTask()
            {
                @Override
                public void run() {
                    try
                    {
                        extractor.extract(base);
                    }catch(Exception e)
                    {
                        e.printStackTrace();
                    }
                }   
            },time,time);
        }
        extractor.start(this);
    }
    
    public void stop()
    {
        if(timer!=null)
        {
            timer.cancel();
        }
        extractor.stop(this);
    }    

    @Override
    public void store(ScriptObjectMirror data) throws IOException 
    {
        store(DataUtils.toDataObject(data));
    }
    
}
