/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.semanticwb.datamanager;

import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.semanticwb.datamanager.datastore.DataStoreMongo;

/**
 *
 * @author javiersolis
 */
public class DataObject extends LinkedHashMap<String, Object> {

    @Override
    public Object put(String key, Object value) {
        return super.put(key, DataUtils.toData(value)); //To change body of generated methods, choose Tools | Templates.
    }

    public DataObject getDataObject(String key) {
        Object obj = get(key);
        if (obj instanceof DataObject) {
            return (DataObject) obj;
        }
        return null;
    }

    public DataList getDataList(String key) {
        Object obj = get(key);
        if (obj instanceof DataList) {
            return (DataList) obj;
        }
        return null;
    }

    public String getString(String key, String def) {
        String ret = getString(key);
        if (ret != null) {
            return ret;
        }
        return def;
    }

    public String getString(String key) {
        Object obj = get(key);
        if (obj == null) {
            return null;
        }
        return obj.toString();
    }

    public int getInt(String key, int def) {
        if (get(key) == null) {
            return def;
        }
        return getInt(key);
    }

    public int getInt(String key) {
        Object obj = get(key);
        if (obj instanceof Integer) {
            return (Integer) obj;
        }
        try {
            return Integer.parseInt(getString(key));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public long getLong(String key, long def) {
        if (get(key) == null) {
            return def;
        }
        return getLong(key);
    }

    public long getLong(String key) {
        Object obj = get(key);
        if (obj instanceof Long) {
            return (Long) obj;
        }
        try {
            return Long.parseLong(getString(key));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public boolean getBoolean(String key, boolean def) {
        if (get(key) == null) {
            return def;
        }
        return getBoolean(key);
    }

    public boolean getBoolean(String key) {
        Object obj = get(key);
        if (obj instanceof Boolean) {
            return (Boolean) obj;
        }
        try {
            return Boolean.parseBoolean(getString(key));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public double getDouble(String key, double def) {
        if (get(key) == null) {
            return def;
        }
        return getDouble(key);
    }

    public double getDouble(String key) {
        Object obj = get(key);
        if (obj instanceof Double) {
            return (Double) obj;
        }
        try {
            return Double.parseDouble(getString(key));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0.0;
    }
    
    public Date getDate(String key, Date def) {
        Date ret=getDate(key);
        if (ret == null) {
            return def;
        }
        return ret;
    }

    public Date getDate(String key) {
        Object obj = get(key);
        if (obj instanceof Date) {
            return (Date) obj;
        }
        return null;
    }    

    public static Object parseJSON(String json) {
        return DataStoreMongo.parseJSON(json);
    }
    
    public String toString() {
        return toString(false);
    }
    
    public String toString(boolean ident) {
        if(ident)return toString("");
        return toString(null);
    }    

    private String toString(String sep) {
        Iterator<Entry<String, Object>> i = entrySet().iterator();
        if (!i.hasNext()) {
            return "{}";
        }

        StringBuilder sb = new StringBuilder();
        sb.append('{');
        for (;;) {
            Entry<String, Object> e = i.next();
            String key = e.getKey();
            Object value = e.getValue();
            sb.append(encodeString(key,true));
            sb.append(':');
            if (value instanceof String) {
                sb.append(value == this ? "(this Map)" :  encodeString((String)value,true) );
            } else {
                sb.append(value == this ? "(this Map)" : value);
            }
            if (!i.hasNext()) {
                return sb.append('}').toString();
            }
            sb.append(',').append(' ');
        }
    }

    public String getId() {
        return getString("_id");
    }

    public String getNumId() {
        String id = getId();
        return id.substring(id.lastIndexOf(":") + 1);
    }

    public String getModelId() {
        String id = getId();
        int i1 = id.indexOf(":");
        if (i1 > -1) {
            int i2 = id.indexOf(":", i1 + 1);
            if (i2 > -1) {
                return id.substring(i1 + 1, i2);
            }
        }
        return null;
    }

    public String getClassName() {
        String id = getId();
        int i1 = id.indexOf(":");
        if (i1 > -1) {
            int i2 = id.indexOf(":", i1 + 1);
            if (i2 > -1) {
                int i3 = id.indexOf(":", i2 + 1);
                if (i3 > -1) {
                    return id.substring(i2 + 1, i3);
                }
            }
        }
        return null;
    }

    public DataObject addParam(String key, Object value) {
        put(key, value);
        return this;
    }

    public DataObject addSubObject(String key) {
        DataObject data = new DataObject();
        put(key, data);
        return data;
    }

    public DataList addSubList(String key) {
        DataList data = new DataList();
        put(key, data);
        return data;
    }   
    
    public void sort(Comparator<Map.Entry<String,Object>> comp)
    {
        entrySet().stream().sorted(comp).forEach((Map.Entry<String, Object> t) -> {
            remove(t.getKey());
            put(t.getKey(), t.getValue());
        });
    }
    
    protected static String encodeString(String value, boolean addDoubleQuotes)
    {
        if(value==null)return "null";

        int len = value.length();
        boolean needEncode = false;
        char c;
        for (int i = 0; i < len; i++)
        {
            c = value.charAt(i);

            if (c >= 0 && c <= 31 || c == 34 || c == 39 || c == 60 || c == 62 || c == 92)
            {
                needEncode = true;
                break;
            }
        }

        if (!needEncode)return addDoubleQuotes ? "\"" + value + "\"" : value;

        StringBuilder sb = new StringBuilder();
        if (addDoubleQuotes)
            sb.append('"');

        for (int i = 0; i < len; i++)
        {
            c = value.charAt(i);
            if (c >= 0 && c <= 7 || c == 11 || c >= 14 && c <= 31 || c == 39 || c == 60 || c == 62)
                sb.append("\\u"+String.format("%04d", (int)c));
            else switch ((int)c)
                {
                    case 8:
                        sb.append("\\b");
                        break;

                    case 9:
                        sb.append("\\t");
                        break;

                    case 10:
                        sb.append("\\n");
                        break;

                    case 12:
                        sb.append("\\f");
                        break;

                    case 13:
                        sb.append("\\r");
                        break;

                    case 34:
                        sb.append("\\\"");
                        break;

                    case 92:
                        sb.append("\\\\");
                        break;

                    default:
                        sb.append(c);
                        break;
                }
        }

        if (addDoubleQuotes)
            sb.append('"');

        return sb.toString();
    }    

}
