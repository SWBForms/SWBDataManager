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

    protected QueryObject(String dsName, String name) {
        this.dsName = dsName;
        this.name = name;
    }        
    
    public void addProperty(QueryProperty property)
    {
        properties.add(property);
    }

    public ArrayList<QueryProperty> getProperties() {
        return properties;
    }        

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDsName(String dsName) {
        this.dsName = dsName;
    }

    public String getDsName() {
        return dsName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
    
}
