/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.semanticwb.datamanager;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TimeZone;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import org.semanticwb.datamanager.script.ScriptObject;
import org.semanticwb.datamanager.utils.SimpleDateFormatTS;

/**
 *
 * @author javier.solis.g
 */
public class DataUtils {
    
    /**
     * Create a new and unique String Id 
     * @return String with ID
     */
    public static String createId()
    {
        return new org.bson.types.ObjectId().toString();
    }
    
    /**
     *
     * @param txt
     * @param def
     * @return
     */
    public static String validate(String txt, String def)
    {
        if(txt!=null) return txt;
        return def;
    }
        
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
    
    /**
     *
     * @param arr
     * @param prop
     * @param value
     * @return
     */
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
    
    /**
     *
     * @param arr
     * @param prop
     * @param value
     * @return
     */
    public static ArrayList<ScriptObject> getArrayNodes(ScriptObject arr, String prop, String value) {
        ArrayList<ScriptObject> ret=new ArrayList();
        if (arr != null) {
            Iterator<ScriptObject> it1 = arr.values().iterator();
            while (it1.hasNext()) {
                ScriptObject obj = it1.next();
                String val = obj.getString(prop);
                if (val != null && val.equals(value)) {
                    ret.add(obj);
                }
            }
        }
        return ret;
    }    

    /**
     *
     * @param str
     * @return
     */
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
    
    /**
     *
     * @param obj
     * @return
     */
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
    
    /**
     *
     * @param obj
     * @return
     */
    public static DataList toDataList(ScriptObjectMirror obj)
    {
        DataList ret=new DataList();
        Iterator it=obj.values().iterator();
        while (it.hasNext()) {
            ret.add(toData(it.next()));
        }
        return ret;        
    }
    
    /**
     *
     * @param obj
     * @return
     */
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

    /**
     * Map data source with keyField like a key and _id like a value
     * @param ds
     * @param keyField
     * @return
     * @throws IOException 
     */
    public static DataObject mapDataSourceByField(SWBDataSource ds, String keyField) throws IOException
    {
        int block=1000;
        DataObject map=new DataObject();
        int startRow=0;
        int endRow=block;
        int totalRows=0;
        do
        {
            DataObject res=ds.fetch(new DataObject().addParam("startRow", startRow).addParam("endRow", endRow)).getDataObject("response");
            DataList<DataObject>data=res.getDataList("data");
            for(DataObject obj:data)
            {
                map.put(obj.get(keyField).toString(), obj.getId());
            }
            startRow=res.getInt("startRow");
            endRow=res.getInt("endRow");
            totalRows=res.getInt("totalRows"); 
            if(endRow==totalRows)break;
            startRow+=block;
            endRow+=block;
        }while(true);
        
        return map;
    }    
    
    /**
     * Map data source with keyField like a key and valueField like a value
     * @param ds
     * @param keyField
     * @param valueField
     * @return
     * @throws IOException 
     */
    public static DataObject mapDataSourceByFields(SWBDataSource ds, String keyField, String valueField) throws IOException
    {
        int block=1000;
        DataObject map=new DataObject();
        int startRow=0;
        int endRow=block;
        int totalRows=0;
        do
        {
            DataObject res=ds.fetch(new DataObject().addParam("startRow", startRow).addParam("endRow", endRow)).getDataObject("response");
            DataList<DataObject>data=res.getDataList("data");
            for(DataObject obj:data)
            {
                String key=obj.getString(keyField);
                if(key!=null)
                {
                    map.put(key, obj.get(valueField));
                }
            }
            startRow=res.getInt("startRow");
            endRow=res.getInt("endRow");
            totalRows=res.getInt("totalRows"); 
            if(endRow==totalRows)break;
            startRow+=block;
            endRow+=block;
        }while(true);
        
        return map;
    }   
    
    /**
     *
     */
    public static class HTTP
    {          
        /**
         *
         * @param url
         * @return
         * @throws IOException
         */
        public static String httpGet(String url) throws IOException
        {
            return httpGet(url, 0);
        }

        /**
         * 
         * @param url
         * @param timeout in miliseconds
         * @return
         * @throws IOException 
         */
        public static String httpGet(String url, int timeout) throws IOException
        {
            StringBuilder ret=new StringBuilder();
            URL url1=new URL(url);
            URLConnection con=url1.openConnection();
            if(timeout>0)
            {
                con.setConnectTimeout(timeout);
                con.setReadTimeout(timeout);
            }
            con.setRequestProperty("User-Agent", "Mozilla/5.0");
            try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()))) 
            {
                String inputLine;
                while ((inputLine = br.readLine()) != null) {
                    ret.append(inputLine);
                }
            }

            return ret.toString();            
        }
    }
    
    /**
     *
     */
    public static class TEXT
    {    
        private static SimpleDateFormatTS iso8601dateFormat = new SimpleDateFormatTS("yyyy-MM-dd'T'HH:mm:ss'.'SSS", null, TimeZone.getTimeZone("UTC"));
        
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
            
        

        /**
         * Gets the difference in time between one date given and the system
         * date. This difference is expressed in the biggest unit of time
         * possible. These units of time being: seconds, minutes, hours, days,
         * months and years.
         * <p>
         * Obtiene la diferencia en tiempo entre una fecha dada y la fecha del
         * sistema. Esta diferencia se expresa en la unidad de tiempo m&aacute;s
         * grande posible. Las unidades de tiempo manejadas son: segundos,
         * minutos, horas, d&iacute;s, meses y a&ntilde;os.</p>
         *
         * @param CreationDate the date to compare
         * @param lang a string indicating the language in which the date is
         * going to be presented
         * @return a string representing the difference between the date given
         * and the system's date, expressed in the biggest unit of time
         * possible. un objeto string que representa la diferencia entre la
         * fecha dada y la fecha del sistema, expresada en la unidad de tiempo
         * m&aacute;s grande posible.
         */
        public static String getTimeAgo(Date CreationDate, String lang)
        {
            return getTimeAgo(new Date(), CreationDate, lang);
        }

        /**
         * Gets the difference in time between two dates given. This difference
         * is expressed in the biggest unit of time possible. These units of
         * time being: seconds, minutes, hours, days, months and years.
         * <p>
         * Obtiene la diferencia en tiempo entre dos fechas dadas. Esta
         * diferencia se expresa en la unidad de tiempo m&aacute;s grande
         * posible. Las unidades de tiempo manejadas son: segundos, minutos,
         * horas, d&iacute;s, meses y a&ntilde;os.</p>
         *
         * @param CurrentDate the most recent date to compare
         * @param CreationDate the oldest date to compare
         * @param lang a string indicating the language in which the date is
         * going to be presented
         * @return a string representing the difference between the two dates
         * given, expressed in the biggest unit of time possible. un objeto
         * string que representa la diferencia entre dos fechas dadas, expresada
         * en la unidad de tiempo m&aacute;s grande posible.
         */
        public static String getTimeAgo(Date CurrentDate, Date CreationDate, String lang)
        {
            String ret = "";
            int second;
            int secondCurrent;
            int secondCreation;
            int minute;
            int minuteCurrent;
            int minuteCreation;
            int hour;
            int hourCurrent;
            int hourCreation;
            int day;
            int dayCurrent;
            int dayCreation;
            int month;
            int monthCurrent;
            int monthCreation;
            int year;
            int yearCurrent;
            int yearCreation;
            int dayMonth;

            secondCurrent = CurrentDate.getSeconds();
            secondCreation = CreationDate.getSeconds();
            minuteCurrent = CurrentDate.getMinutes();
            minuteCreation = CreationDate.getMinutes();
            hourCurrent = CurrentDate.getHours();
            hourCreation = CreationDate.getHours();
            dayCurrent = CurrentDate.getDate();
            dayCreation = CreationDate.getDate();
            monthCurrent = CurrentDate.getMonth();
            monthCreation = CreationDate.getMonth();
            yearCurrent = CurrentDate.getYear();
            yearCreation = CreationDate.getYear();

            boolean leapYear = false;
            if (monthCurrent > 1 || (dayCreation == 29 && monthCreation == 1))
            {
                leapYear = (yearCreation % 4 == 0) && (yearCreation % 100 != 0 || yearCreation % 400 == 0);
            }
            dayMonth = 0;
            day = 0;
            switch (monthCreation)
            {
                case 0:
                    dayMonth = 31;
                    break;
                case 1:
                    if (leapYear)
                    {
                        dayMonth = 29;
                    }
                    else
                    {
                        dayMonth = 28;
                    }
                    break;
                case 2:
                    dayMonth = 31;
                    break;
                case 3:
                    dayMonth = 30;
                    break;
                case 4:
                    dayMonth = 31;
                    break;
                case 5:
                    dayMonth = 30;
                    break;
                case 6:
                    dayMonth = 31;
                    break;
                case 7:
                    dayMonth = 31;
                    break;
                case 8:
                    dayMonth = 30;
                    break;
                case 9:
                    dayMonth = 31;
                    break;
                case 10:
                    dayMonth = 30;
                    break;
                case 11:
                    dayMonth = 31;
                    break;
            }
            if (secondCurrent >= secondCreation)
            {
                second = secondCurrent - secondCreation;
            }
            else
            {
                second = (60 - secondCreation) + secondCurrent;
                minuteCurrent = minuteCurrent - 1;
            }
            if (minuteCurrent >= minuteCreation)
            {
                minute = minuteCurrent - minuteCreation;
            }
            else
            {
                minute = (60 - minuteCreation) + minuteCurrent;
                hourCurrent = hourCurrent - 1;
            }
            if (hourCurrent >= hourCreation)
            {
                hour = hourCurrent - hourCreation;
            }
            else
            {
                hour = (24 - hourCreation) + hourCurrent;
                dayCurrent = dayCurrent - 1;
            }
            if (dayCurrent >= dayCreation)
            {
                day = day + (dayCurrent - dayCreation);
            }
            else
            {
                day = day + ((dayMonth - dayCreation) + dayCurrent);
                monthCurrent = monthCurrent - 1;
            }
            if (monthCurrent >= monthCreation)
            {
                month = monthCurrent - monthCreation;
            }
            else
            {
                month = (12 - monthCreation) + monthCurrent;
                yearCurrent = yearCurrent - 1;
            }

            year = yearCurrent - yearCreation;
            if ("en".equals(lang))
            {
                if (year > 0)
                {
                    ret = (year + " years ago");
                }
                else if (month > 0)
                {
                    ret = (month + " month ago");
                }
                else if (day > 0)
                {
                    ret = (day + " days ago");
                }
                else if (hour > 0)
                {
                    ret = (hour + " hours ago");
                }
                else if (minute > 0)
                {
                    ret = (minute + " minutes ago");
                }
                else
                {
                    ret = (second + " second ago");
                }
            }
            else
            {
                if (year > 0)
                {
                    ret = (year + " año(s) atrás");
                }
                else if (month > 0)
                {
                    ret = (month + " mes(es) atrás");
                }
                else if (day > 0)
                {
                    ret = (day + " día(s) atrás");
                }
                else if (hour > 0)
                {
                    ret = (hour + " hora(s) atrás");
                }
                else if (minute > 0)
                {
                    ret = (minute + " minuto(s) atrás");
                }
                else
                {
                    ret = (second + " segundo(s) atrás");
                }
            }
            return ret;
        }        
        
        /**
         * Replaces some special characters in a XML-formatted string by their
         * entity names. The characters to replace are:
         * {@literal \t, &, <, y >}.
         * <p>
         * Reemplaza algunos de los caracteres especiales presentes en una
         * cadena con formato XML por su equivalente en nombre de entidad. Los
         * caracteres a reemplazar son: {@literal \t, &, <, y >.}</p>
         *
         * @param str an XML-formatted string with some replaceable characters
         * @return a string representing the same content that {@code str}, but
         * with some characters replaced according to the following relations:
         * {@literal \t} replaced by 4 blank spaces {@literal &} replaced by {@literal &amp;}
         * {@literal <} replaced by {@literal &lt;}
         * } replaced by {@literal &gt;} un objeto string que representa el
         * mismo contenido que {@code str}, pero con algunos caracteres
         * reemplazados de acuerdo a las relaciones anteriormente mostradas.
         */
        static public String replaceXMLChars(String str)
        {
            if (str == null)
            {
                return null;
            }
            StringBuffer ret = new StringBuffer(500);

            // split tokens
            StringTokenizer tokenizer = new StringTokenizer(str, " \t@%^&()-+=|\\{}[].;\"<>", true);
            while (tokenizer.hasMoreTokens())
            {
                // next token
                String token = tokenizer.nextToken();

                // replace '\t' by the content of "tabulation"
                if (token.startsWith("\t"))
                {
                    ret.append("    ");
                    continue;
                }

                // replace '<' by '&lt;'
                if (token.startsWith("<"))
                {
                    ret.append("&lt;");
                    continue;
                }

                // replace '>' by '&gt;'
                if (token.startsWith(">"))
                {
                    ret.append("&gt;");
                    continue;
                }

                // replace '&' by '&amp;'
                if (token.startsWith("&"))
                {
                    ret.append("&amp;");
                    continue;
                }
                ret.append(token);
            }
            return ret.toString();

        }

        /**
         * Replaces the entity name of some special characters by their
         * representation in HTML code. The entity names to replace are:
         * {@literal &lt;, &gt;, y &amp;}.
         * <p>
         * Reemplaza el nombre de entidad de algunos caracteres especiales, por
         * su equivalente en HTML. Los nombres de entidad a reemplazar son:
         *
         * @param txt a string containing the text to replace
         * @return a string with the entity names of some special characters
         * replaced by their representation in HTML code. The entity names to
         * look for are: {@literal &amp;} replaced by {@literal &}
         * {@literal &lt;} replaced by {@literal <}
         * }
         * un objeto string con los nombres de entidad de algunos caracteres
         * especiales reemplazados por su representaci&oacute;n en c&oacute;digo
         * HTML, arriba se mencionan los reemplazos realizados.
         * {@literal &lt;, &gt;, y &amp;}.</p>
         */
        static public String replaceXMLTags(String txt)
        {

            if (txt == null)
            {
                return null;
            }
            StringBuffer str = new StringBuffer(txt);
            for (int x = 0; x < str.length(); x++)
            {
                char ch = str.charAt(x);
                if (ch == '&')
                {
                    if (str.substring(x, x + 4).equals("&lt;"))
                    {
                        str.replace(x, x + 4, "<");
                    }
                    else if (str.substring(x, x + 4).equals("&gt;"))
                    {
                        str.replace(x, x + 4, ">");
                    }
                    else if (str.substring(x, x + 5).equals("&amp;"))
                    {
                        str.replace(x, x + 5, "&");
                    }
                }
            }
            return str.toString();
        }   
        
        
        /**
         * Converts a date into a string with the format
         * {@literal yyyy-MM-dd'T'HH:mm:ss'.'SSS}.
         * <p>
         * Convierte un objeto date a uno string con el formato
         * {@literal yyyy-MM-dd'T'HH:mm:ss'.'SSS}.</p>
         *
         * @param date a date to convert
         * @return a string representing the date received with the format
         * {@literal yyyy-MM-dd'T'HH:mm:ss'.'SSS}. un objeto string que
         * representa al date recibido, con el formato
         * {@literal yyyy-MM-dd'T'HH:mm:ss'.'SSS}.
         */
        public static String iso8601DateFormat(Date date)
        {
            //SimpleDateFormat iso8601dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'.'SSS");
            return iso8601dateFormat.format(date)+"Z";
        }        
        
        
    }

}
