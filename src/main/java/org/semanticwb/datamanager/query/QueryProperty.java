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
    
    /**
     *
     * @param dsname
     * @param name
     * @param value
     */
    protected QueryProperty(String dsname, String name, String value) {
        this.dsname = dsname;
        this.name = name;
        this.value = value;
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
     * @return
     */
    public String getValue() {
        return value;
    }

    /**
     *
     * @param value
     */
    public void setValue(String value) {
        this.value = value;
    }     

    /**
     *
     * @param dsname
     */
    public void setDsName(String dsname) {
        this.dsname = dsname;
    }

    /**
     *
     * @return
     */
    public String getDsName() {
        return dsname;
    }
    
    /**
     *
     * @return
     */
    public QueryObject getObject() {
        return object;
    }

    /**
     *
     * @param object
     */
    public void setObject(QueryObject object) {
        this.object = object;
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
