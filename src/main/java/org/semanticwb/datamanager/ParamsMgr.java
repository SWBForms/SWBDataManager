package org.semanticwb.datamanager;

import java.util.HashMap;
import javax.servlet.http.HttpSession;
import org.semanticwb.datamanager.utils.TokenGenerator;

/**
 *
 * @author javiersolis
 */
public class ParamsMgr 
{
    private static final String SESSION_TAG="_PARAMSMGR_";
    private final HttpSession session;
    private HashMap<String,HashMap<String,String>> params;

    public ParamsMgr(HttpSession session) {
        this.session=session;
        params=(HashMap)session.getAttribute(SESSION_TAG);
        if(params==null)
        {
            params=new HashMap();
            session.setAttribute(SESSION_TAG, params);
        }
    }
    
    public String setDataMap(HashMap<String,String> map)
    {
        String key=TokenGenerator.nextShortToken();
        params.put(key, map);
        return key;
    }
    
    /**
     * Parametros llave,valor
     * @param data
     * @return 
     */
    public String setDataValues(String ...data)
    {
        String key=TokenGenerator.nextShortToken();
        HashMap map=new HashMap();
        if(data.length%2==0)
        {
            for(int x=0;x<data.length;x+=2)
            {
                map.put(data[x], data[x+1]);
            }
            params.put(key, map);
        }else
        {
            return null;
        }
        return key;
    }
    
    public String setDataValue(String value)
    {
        String key=TokenGenerator.nextShortToken();
        HashMap<String,String> map=new HashMap();
        map.put("_DEF_", value);
        params.put(key, map);
        return key;
    }    
    
    public HashMap<String,String> getDataMap(String key)
    {
        return params.get(key);
    }
    
    public String getDataValue(String key)
    {
        if(params.containsKey(key))
        {
            return params.get(key).get("_DEF_");
        }
        return null;        
    }    
    
    public String getDataValue(String key, String param)
    {
        if(params.containsKey(key))
        {
            return params.get(key).get(param);
        }
        return null;
    }
}
