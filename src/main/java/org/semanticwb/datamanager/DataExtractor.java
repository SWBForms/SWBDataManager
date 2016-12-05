/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.semanticwb.datamanager;

import java.io.IOException;

/**
 *
 * @author javiersolis
 */
public interface DataExtractor 
{   
    public void start(DataExtractorBase base);
    
    public void extract(DataExtractorBase base) throws IOException;
    //void store(HashMap data);
    
    public void stop(DataExtractorBase base);
}
