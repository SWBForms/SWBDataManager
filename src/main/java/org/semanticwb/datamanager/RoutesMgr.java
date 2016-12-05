/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.semanticwb.datamanager;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import org.semanticwb.datamanager.script.ScriptObject;

/**
 *
 * @author javiersolis
 */
public class RoutesMgr {
    private static RoutesMgr instance=null;
    private static final Logger logger = Logger.getLogger("o.s.d.RoutesMgr");
    private final Map<String, RouteData> map = new ConcurrentHashMap<String, RouteData>();
    private String loginRoute;   

    private RoutesMgr() {
    
    }

    public static String getLoginRoute() {
        return instance.loginRoute;
    }
    
    protected static void startup()
    {
        instance = new RoutesMgr();
    }
    
    public static RouteData getRouteData(String path)
    {
        return instance.map.get(path);
    }
    
    public static synchronized void parseRouter(ScriptObject routes) {
        Iterator<String> it=routes.keySet().iterator();
        while (it.hasNext()) {
            String dsname = it.next();
            System.out.println("Loading Routes:"+dsname); 
            ScriptObject route=routes.get(dsname);
            String lfb=route.getString("loginFallback");
            if(lfb!=null)instance.loginRoute=lfb;
            ScriptObject routeList=route.get("routeList");
            Iterator<ScriptObject> it2=routeList.values().iterator();
            while (it2.hasNext()) {
                ScriptObject obj = it2.next();
                String key=obj.getString("routePath");
                RouteData _obj=instance.map.get(key);
                if(_obj!=null)
                {
                    int _index=_obj.getScriptObject().getInt("zindex");
                    int index=obj.getInt("zindex");
                    if(index>=_index)
                    {
                        instance.map.put(key, new RouteData(obj, null));
                    }
                }else
                {
                    instance.map.put(key, new RouteData(obj, null));
                }
            }
            System.out.println("Routes:"+instance.map); 
        }
    }
}