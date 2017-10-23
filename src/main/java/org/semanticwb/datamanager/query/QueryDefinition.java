/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.semanticwb.datamanager.query;

import java.util.HashMap;
import java.util.Iterator;

/**
 *
 * @author javiersolis
 */
public class QueryDefinition {
    private QueryObject root;
    private QueryObject act;
    private HashMap<String,QueryObject> objects=new HashMap();
    private HashMap<String,QueryProperty> properties=new HashMap();

    public QueryDefinition() {
    }
    
    public QueryDefinition addQueryObject(String dsname, String name)
    {
        QueryObject object=new QueryObject(dsname, name);
        objects.put(name, object);
        if(root==null)root=object;
        act=object;
        if(properties.containsKey(name))properties.get(name).setObject(object);
        return this;
    }
    
    public QueryDefinition addQueryProperty(String dsname, String name, String value)
    {
        QueryProperty property=new QueryProperty(dsname, name, value);
        act.addProperty(property);
        properties.put(name, property);
        return this;
    }

    @Override
    public String toString() {
        if(root==null)return "";
        return toString(root,"");
    }
    
    private String toString(QueryObject obj,String space)
    {
        StringBuilder ret=new StringBuilder();
        ret.append(space+obj.getDsName()+":"+obj.getName()+"\n");
        space+="--";
        Iterator<QueryProperty> it=obj.getProperties().iterator();
        while (it.hasNext()) {
            QueryProperty prop = it.next();
            ret.append(space+prop.getDsName()+":"+prop.getName()+":"+prop.getValue()+"\n");
            if(prop.getObject()!=null)
            {
                ret.append(toString(prop.getObject(), space+"--"));
            }
        }
        return ret.toString();
    }

    protected QueryObject getRoot() {
        return root;
    }

    protected HashMap<String, QueryObject> getObjects() {
        return objects;
    }

    protected HashMap<String, QueryProperty> getProperties() {
        return properties;
    }        
}
