/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.semanticwb.datamanager.utils;

import java.text.ParseException;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 *
 * @author javier.solis.g
 */
public class SimpleDateFormatTS
{
    private final ThreadLocal<java.text.SimpleDateFormat> sd;
    
    public SimpleDateFormatTS(final String patern, Locale loc, TimeZone ts)
    {
        sd = new ThreadLocal<java.text.SimpleDateFormat>()
        {
            @Override
            protected java.text.SimpleDateFormat initialValue()
            {
                java.text.SimpleDateFormat df=null;
                if(loc!=null)df=new java.text.SimpleDateFormat(patern,loc);
                else df=new java.text.SimpleDateFormat(patern);
                df.setTimeZone(ts);
                return df;
            }
        };            
    }    
    
    public Date parse(String txt) throws ParseException
    {
        return sd.get().parse(txt);
    }
    
    public String format(Date date)
    {
        return sd.get().format(date);
    }
}
