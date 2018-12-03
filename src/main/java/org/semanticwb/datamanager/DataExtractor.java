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

    /**
     *
     * @param base
     */
    public void start(DataExtractorBase base);
    
    /**
     *
     * @param base
     * @throws IOException
     */
    public void extract(DataExtractorBase base) throws IOException;
    //void store(HashMap data);
    
    /**
     *
     * @param base
     */
    public void stop(DataExtractorBase base);
}
