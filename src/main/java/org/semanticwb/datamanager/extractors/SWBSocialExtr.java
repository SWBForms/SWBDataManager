/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.semanticwb.datamanager.extractors;

import java.io.IOException;
import java.util.Date;
import org.semanticwb.datamanager.DataExtractor;
import org.semanticwb.datamanager.DataExtractorBase;
import org.semanticwb.datamanager.DataObject;

/**
 *
 * @author javiersolis
 */
public class SWBSocialExtr implements DataExtractor
{

    @Override
    public void extract(DataExtractorBase base) throws IOException
    {
        System.out.println("extract:"+base.getName());
        String url=base.getScriptObject().getString("url");
        String brand=base.getScriptObject().getString("brand");
        String stream=base.getScriptObject().getString("stream");
        
        DataObject req=new DataObject();
        DataObject data=new DataObject();
        req.put("data", data);
        data.put("brand", brand);
        data.put("stream", stream);
        data.put("date", new Date());
        data.put("precio", 500);
       
        base.store(req);
    }

    @Override
    public void start(DataExtractorBase base) {
        System.out.println("Start:"+base.getName());
        try
        {
            extract(base);
        }catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void stop(DataExtractorBase base) {
        System.out.println("Stop:"+base.getName());        
    }
    
}
