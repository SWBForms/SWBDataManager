/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.semanticwb.datamanager;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import org.semanticwb.datamanager.script.ScriptObject;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

/**
 *
 * @author javiersolis
 */
public class SWBScriptUtils {

    SWBScriptEngine engine;

    private static final ExecutorService proccessor = Executors.newSingleThreadExecutor();

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                proccessor.shutdown();
                proccessor.awaitTermination(1, TimeUnit.MINUTES);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
        }));
    }

    /**
     *
     * @param engine
     */
    public SWBScriptUtils(SWBScriptEngine engine) {
        this.engine = engine;
    }

    /**
     *
     * @param str
     * @return
     */
    public String encodeSHA(String str) {
        try {
            if (str != null && !str.startsWith("[SHA-512]")) {
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
        } catch (Exception e) {
            e.printStackTrace();
        }
        return str;
    }

    private Session getSession() {
        Properties props = new Properties();
        ScriptObject config = engine.getScriptObject().get("config");
        if (config != null) {
            ScriptObject smail = config.get("mail");
            if (smail != null) {
                String from = smail.getString("from");
                String fromName = smail.getString("fromName");
                String host = smail.getString("host");
                String user = smail.getString("user");
                String passwd = smail.getString("passwd");
                int port = (Integer) smail.get("port").getValue();
                String transport = smail.getString("transport");
                String ssltrust = smail.getString("ssltrust");
                String starttls = smail.getString("starttls");

                if (ssltrust == null) {
                    ssltrust = "*";
                }
                if (starttls == null) {
                    starttls = "true";
                }

                props.put("mail.smtp.host", host);
                props.put("mail.smtp.port", port);
                props.put("mail.smtps.ssl.trust", ssltrust);
                props.put("mail.smtp.starttls.enable", starttls);
            }
        }
        return Session.getInstance(props);
    }

    /**
     *
     * @param to
     * @param subject
     * @param msg
     * @return
     */
    public boolean sendMail(String to, String subject, String msg) {
        return sendMail(to, null,subject, msg, "text/plain", null);
    }
    
    /**
     *
     * @param to
     * @param subject
     * @param msg
     * @return
     */
    public boolean sendHtmlMail(String to, String subject, String msg) {
        return sendMail(to, null,subject, "<html><body>" + msg + "</body></html>", "text/html", null);    
    }    
    
    /**
     *
     * @param to
     * @param toName
     * @param subject
     * @param message
     * @param contentType
     * @param callback
     * @return
     */
    public boolean sendMail(String to, String toName, String subject, String message, String contentType, Consumer callback) {
        return sendMail(to, toName, subject, message, contentType, null, callback);
    }    
    
    /**
     *
     * @param to
     * @param toName
     * @param subject
     * @param message
     * @param contentType
     * @param callback
     * @return
     */
    public boolean sendMail(String to, String toName, String subject, String message, String contentType, File attaches[], Consumer callback) {
        Properties props = new Properties();
        ScriptObject config = engine.getScriptObject().get("config");
        if (config != null) {
            ScriptObject smail = config.get("mail");
            if (smail != null) {
                String from = smail.getString("from");
                String fromName = smail.getString("fromName");
                String host = smail.getString("host");
                String user = smail.getString("user");
                String passwd = smail.getString("passwd");
                int port = (Integer) smail.get("port").getValue();
                String transport = smail.getString("transport")==null?"smtps":smail.getString("transport");
                String ssltrust = smail.getString("ssltrust");
                String starttls = smail.getString("starttls");

                if (ssltrust == null) {
                    ssltrust = "*";
                }
                if (starttls == null) {
                    starttls = "true";
                }

                props.put("mail.smtp.host", host);
                props.put("mail.smtp.port", port);
                props.put("mail.smtps.ssl.trust", ssltrust);
                props.put("mail.smtp.starttls.enable", starttls);

                try {
                    Session session = Session.getInstance(props);
                    InternetAddress userAddrs[] = InternetAddress.parse(to);
                                        
                    Message msg = new MimeMessage(session);
                    Address[] addrs = new Address[]{new InternetAddress(from, fromName)};
                    msg.addFrom(addrs);
                    msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
                    msg.setSubject(subject);
                    
                    //msg.setDataHandler(new DataHandler(message, contentType));
                    
                    // Create a multipar message
                    Multipart multipart = new MimeMultipart();                    
                    
                    // Create the message part
                    BodyPart htmlBodyPart = new MimeBodyPart();
                    htmlBodyPart.setContent(message , contentType);
                    multipart.addBodyPart(htmlBodyPart);

                    if(attaches!=null)
                    {
                        for(int x=0;x<attaches.length;x++)
                        {
                            // Part two is attachment
                            BodyPart fileBodyPart = new MimeBodyPart();
                            DataSource source = new FileDataSource(attaches[x]);
                            fileBodyPart.setDataHandler(new DataHandler(source));
                            fileBodyPart.setFileName(attaches[x].getName());
                            multipart.addBodyPart(fileBodyPart);
                        }
                    }

                    // Send the complete message parts
                    msg.setContent(multipart);
                    
                    proccessor.submit(() -> 
                    {
                        try {
                            Transport t = session.getTransport(transport);
                            t.connect(host, port, user, passwd);
                            t.sendMessage(msg, userAddrs);
                            t.close();
                            if(callback!=null)callback.accept(null);
                        } catch (MessagingException uex) {
                            uex.printStackTrace();
                        }
                    });
                    return true;
                } catch (IOException | MessagingException uex) {
                    uex.printStackTrace();
                }
            }
        }
        return false;
    }

}
