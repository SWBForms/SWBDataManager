/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.semanticwb.datamanager.datastore;

import java.io.IOException;
import org.semanticwb.datamanager.DataObject;
import org.semanticwb.datamanager.SWBDataSource;

/**
 *
 * @author javiersolis
 */
public interface SWBDataStore 
{
    public DataObject fetch(DataObject json, SWBDataSource dataSource) throws IOException;

    public DataObject aggregate(DataObject json, SWBDataSource dataSource) throws IOException;
    
    public DataObject add(DataObject json, SWBDataSource dataSource) throws IOException;

    public DataObject remove(DataObject json, SWBDataSource dataSource) throws IOException;

    public DataObject update(DataObject json, SWBDataSource dataSource) throws IOException;

    public void close();
}
