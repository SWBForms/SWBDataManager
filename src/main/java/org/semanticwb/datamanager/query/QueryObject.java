/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.semanticwb.datamanager.query;

import java.util.ArrayList;

/**
 *
 * @author javiersolis
 */
public class QueryObject {
    private String dsName;
    private String name;
    private String displayName;
    private ArrayList<QueryProperty> properties=new ArrayList();

    /**
     *
     * @param dsName
     * @param name
     */
    protected QueryObject(String dsName, String name) {
        this.dsName = dsName;
        this.name = name;
    }        
    
    /**
     *
     * @param property
     */
    public void addProperty(QueryProperty property)
    {
        properties.add(property);
    }

    /**
     *
     * @return
     */
    public ArrayList<QueryProperty> getProperties() {
        return properties;
    }        

    /**
     *
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     *
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     *
     * @param dsName
     */
    public void setDsName(String dsName) {
        this.dsName = dsName;
    }

    /**
     *
     * @return
     */
    public String getDsName() {
        return dsName;
    }

    /**
     *
     * @return
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     *
     * @param displayName
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
    
}
