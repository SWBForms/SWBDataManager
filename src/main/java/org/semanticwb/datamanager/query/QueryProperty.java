/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.semanticwb.datamanager.query;

/**
 *
 * @author javiersolis
 */
public class QueryProperty {
    private String dsname;
    private String name;
    private String value;
    private String displayName;
    private QueryObject object;
    
    protected QueryProperty(String dsname, String name, String value) {
        this.dsname = dsname;
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }     

    public void setDsname(String dsname) {
        this.dsname = dsname;
    }

    public String getDsname() {
        return dsname;
    }
    
    public QueryObject getObject() {
        return object;
    }

    public void setObject(QueryObject object) {
        this.object = object;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
    
}
