/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.semanticwb.datamanager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import org.semanticwb.datamanager.script.ScriptObject;

/**
 *
 * @author javier.solis.g
 */
public class DataUtils {

    /**
     * Lee el contenido del InputStream y lo convierte a un String
     *
     * @param inputStream
     * @return
     * @throws IOException
     */
    public static String readInputStream(InputStream inputStream) throws IOException {
        return new String(readFully(inputStream));
    }

    /**
     * Lee el contenido del InputStream y lo convierte a un String, con la
     * codificacion especificada
     *
     * @param inputStream
     * @param encoding
     * @return
     * @throws IOException
     */
    public static String readInputStream(InputStream inputStream, String encoding) throws IOException {
        return new String(readFully(inputStream), encoding);
    }

    /**
     * Lee el contenido del InputStream y lo convierte a un arreglo de bytes
     *
     * @param inputStream
     * @return
     * @throws IOException
     */
    public static byte[] readFully(InputStream inputStream) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length = 0;
        while ((length = inputStream.read(buffer)) != -1) {
            baos.write(buffer, 0, length);
        }
        return baos.toByteArray();
    }
    
    /**
     * Copies an input stream into an output stream using the buffer size
     * defined by {@code SWBUtils.bufferSize} in the reading/writing
     * operations.
     * <p>
     * Copia un flujo de entrada en uno de salida utilizando el
     * tama&ntilde;o de buffer definido por {@code SWBUtils.bufferSize} en
     * las operaciones de lectura/escritura.</p>
     *
     * @param in the input stream to read from
     * @param out the output stream to write to
     * @throws IOException if either the input or the output stream is
     * {@code null}.
     * <p>
     * Si el flujo de entrada o el de salida es {@code null}.</p>
     */
    public static void copyStream(InputStream in, OutputStream out) throws IOException
    {
        copyStream(in, out, 8192);
    }

    /**
     * Copies an input stream into an output stream using the specified
     * buffer size in the reading/writing operations.
     * <p>
     * Copia un flujo de entrada en uno de salida utilizando el
     * tama&ntilde;o de buffer especificado en las operaciones de
     * lectura/escritura.</p>
     *
     * @param in the input stream to read from
     * @param out the output stream to write to
     * @param bufferSize the number of bytes read/writen at the same time in
     * each I/O operation
     * @throws IOException if either the input or the output stream is
     * {@code null}.
     * <p>
     * Si el flujo de entrada o el de salida es {@code null}.</p>
     */
    public static void copyStream(InputStream in, OutputStream out, int bufferSize) throws IOException
    {
        if (in == null)
        {
            throw new IOException("Input Stream null");
        }
        if (out == null)
        {
            throw new IOException("Ouput Stream null");
        }
        byte[] bfile = new byte[bufferSize];
        int x;
        while ((x = in.read(bfile, 0, bufferSize)) > -1)
        {
            out.write(bfile, 0, x);
        }
        in.close();
        out.flush();
        out.close();
    }    
    
    

    public static ScriptObject getArrayNode(ScriptObject arr, String prop, String value) {
        if (arr != null) {
            Iterator<ScriptObject> it1 = arr.values().iterator();
            while (it1.hasNext()) {
                ScriptObject obj = it1.next();
                String val = obj.getString(prop);
                if (val != null && val.equals(value)) {
                    return obj;
                }
            }
        }
        return null;
    }

    public static String encodeSHA(String str) 
    {
        try
        {
            if(str!=null && !str.startsWith("[SHA-512]"))
            {
                MessageDigest md = MessageDigest.getInstance("SHA-512");
                md.update(str.getBytes());

                byte byteData[] = md.digest();

                //convert the byte to hex format method 1
                StringBuffer sb = new StringBuffer();
                sb.append("[SHA-512]");
                for (int i = 0; i < byteData.length; i++) {
                    sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
                }
                return sb.toString();
            }
        }catch(Exception e)
        {
            e.printStackTrace();
        }        
        return str;
    }
    
    public static Object toData(Object obj)
    {
        if(obj instanceof jdk.nashorn.internal.objects.NativeArray)
        {
            //System.out.print(value);
            jdk.nashorn.internal.objects.NativeArray narr=(jdk.nashorn.internal.objects.NativeArray)obj;
            Object arr[]=narr.asObjectArray();
            DataList list=new DataList();
            for(int x=0;x<arr.length;x++)
            {
                list.add(toData(arr[x]));
            }
            return list;
        }else if(obj instanceof ScriptObjectMirror && ((ScriptObjectMirror)obj).isArray())
        {
            return toDataList((ScriptObjectMirror)obj);
        }else if(obj instanceof ScriptObjectMirror)
        {
            return toDataObject((ScriptObjectMirror)obj);
        }
        return obj;  
    }      
    
    public static DataList toDataList(ScriptObjectMirror obj)
    {
        DataList ret=new DataList();
        Iterator it=obj.values().iterator();
        while (it.hasNext()) {
            ret.add(toData(it.next()));
        }
        return ret;        
    }
    
    public static DataObject toDataObject(ScriptObjectMirror obj)
    {
        if(obj==null)return null;
        DataObject ret=new DataObject();
        Iterator<Map.Entry<String,Object>> it=obj.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Object> entry = it.next();
            ret.put(entry.getKey(), toData(entry.getValue()));
        }
        return ret;
    }        
    
    public static class TEXT
    {    
        /**
         * Finds the substrings delimited by two given strings, inside another
         * string.
         * <p>
         * Encuentra las subcadenas delimitadas por dos objetos string dados,
         * dentro de otro objeto string.</p>
         *
         * @param str a string into which the substrings are going to be looked
         * for
         * @param pre a string that precedes the substring to extract from
         * {@code str}
         * @param pos pos a string that goes immediatly after the substring to
         * extract from {@code str}
         * @return an iterator with all the substrings found.
         *
         */
        public static Iterator<String> findInterStr(String str, String pre, String pos)
        {
            ArrayList<String> ret = new ArrayList();
            int y = 0;
            do
            {
                y = findInterStr(str, pre, pos, y, ret);
            }
            while (y > -1);
            return ret.iterator();
        }    
        
        /**
         * Finds a substring in {@code str} which position must be after
         * {@code index} and is delimited by {@code pre} and {@code pos}
         * strings. The substring found is then stored in {@code arr}.
         * <p>
         * Encuentra una subcadena en {@code str} cuya posici&oacute;n debe ser
         * posterior a {@code index} y es delimitada por las cadenas {@code pre}
         * y {@code pos}. La subcadena encontrada se almacena en
         * {@code arr}.</p>
         *
         * @param str a string from which a substring is going to be extracted
         * @param pre a string that precedes the substring to extract from
         * {@code str}
         * @param pos a string that goes immediatly after the substring to
         * extract from {@code str}
         * @param index the position in {@code str} from which {@code pre} is
         * looked for
         * @param arr the object in which the substring extracted is going to be
         * stored
         * @return the index in {@code str} immediatly after {@code pos}, or -1
         * if {@code pre} is not found in {@code str}. El &iacute;ndice en
         * {@code str} inmediatamente despu&eacute;s de {@code pos}, o -1 si
         * {@code pre} no es encontrado en {@code str}.</p>
         */
        private static int findInterStr(String str, String pre, String pos,
                int index, ArrayList arr)
        {

            int i = str.indexOf(pre, index);
            if (i > -1)
            {
                i = i + pre.length();
                int j = str.indexOf(pos, i);
                if (j > -1)
                {
                    arr.add(str.substring(i, j));
                    return j + pos.length();
                }
            }
            return -1;
        }        
    }

}
