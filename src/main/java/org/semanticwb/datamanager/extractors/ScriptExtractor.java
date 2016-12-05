/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.semanticwb.datamanager.extractors;

import java.io.IOException;
import org.semanticwb.datamanager.DataExtractor;
import org.semanticwb.datamanager.DataExtractorBase;
import org.semanticwb.datamanager.script.ScriptObject;

/**
 *
 * @author javiersolis
 */
public class ScriptExtractor implements DataExtractor
{

    @Override
    public void extract(DataExtractorBase base) throws IOException
    {
        ScriptObject extractor=base.getScriptObject().get("extractor");
        ScriptObject func=extractor.get("extract");
        //System.out.println("func:"+func);
        if(func!=null && func.isFunction())
        {
            try
            {
                ScriptObject r=func.invoke(base.getScriptEngine(),base);
            }catch(Throwable e)
            {
                e.printStackTrace();
            }
        }        
    }

    @Override
    public void start(DataExtractorBase base) {
        ScriptObject extractor=base.getScriptObject().get("extractor");
        ScriptObject func=extractor.get("start");        
        //System.out.println("func:"+func);
        if(func!=null && func.isFunction())
        {
            try
            {
                ScriptObject r=func.invoke(base.getScriptEngine(),base);
            }catch(Throwable e)
            {
                e.printStackTrace();
            }
        }else
        {
            try
            {
                extract(base);
            }catch(Exception e)
            {
                e.printStackTrace();
            }            
        }
    }

    @Override
    public void stop(DataExtractorBase base) {
        ScriptObject extractor=base.getScriptObject().get("extractor");
        ScriptObject func=extractor.get("stop");  
        //System.out.println("func:"+func);
        if(func!=null && func.isFunction())
        {
            try
            {
                ScriptObject r=func.invoke(base.getScriptEngine(),base);
            }catch(Throwable e)
            {
                e.printStackTrace();
            }
        }         
    }
}
