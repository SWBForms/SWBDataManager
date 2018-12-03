/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.semanticwb.datamanager;

import java.util.Iterator;

/**
 *
 * @author javiersolis
 */
public class DataObjectIterator implements Iterator        
{
    /**
     * The total size of the collection, not limited
     * @return 
     */
    public int total()
    {
        return 0;
    }
    
    /**
     * The size of the Iterator limited
     * @return 
     */
    public int size()
    {
        return 0;
    }

    @Override
    public boolean hasNext()
    {
        return false;
    }

    @Override
    public DataObject next()
    {
        return null;
    }
    
    /**
     *
     */
    public void close()
    {        
    }
}
