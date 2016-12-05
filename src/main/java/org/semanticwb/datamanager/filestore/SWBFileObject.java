package org.semanticwb.datamanager.filestore;

import java.io.ByteArrayInputStream;
import org.semanticwb.datamanager.exceptions.AlreadyDeliveredException;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.semanticwb.datamanager.exceptions.SWBDataManagerException;

/**
 *
 * @author serch
 */
public class SWBFileObject {
    public enum Type {
        BYTE, FILE, STREAM
    }
    
    private final Type type;
    private final String name;
    private final String contentType;
    private final byte[] contentByte;
    private final File contentFile;
    private final InputStream contentStream;
    private boolean alreadyGiven = false;

    public SWBFileObject(String name, String contenType, byte[] content) {
        this.type = Type.BYTE;
        this.name = name;
        this.contentType = contenType;
        this.contentByte = content;
        this.contentFile = null;
        this.contentStream = null;
    }
    
    public SWBFileObject(String name, String contenType, File content) {
        this.type = Type.FILE;
        this.name = name;
        this.contentType = contenType;
        this.contentByte = null;
        this.contentFile = content;
        this.contentStream = null;
    }
    
    public SWBFileObject(String name, String contenType, InputStream content) {
        this.type = Type.STREAM;
        this.name = name;
        this.contentType = contenType;
        this.contentByte = null;
        this.contentFile = null;
        this.contentStream = content;
    }

    public Type getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getContentType() {
        return contentType;
    }
    
    public byte[] getContentAsByteArray() {
        if (type == Type.BYTE) return contentByte;
        try {
            return toByteArray();
        } catch (IOException ioe){
            throw new SWBDataManagerException("can't generate a byte[]", ioe);
        }
    }
    
    public File getContentAsFile() {
        if (type == Type.FILE) return contentFile;
        else throw new SWBDataManagerException("This content can't be transformed to a File");
    }
    
    public InputStream getContentAsInputStream(){
        try {
            return toInputStream();
        } catch (IOException ioe){
            throw new SWBDataManagerException("can't generate an InputStream", ioe);
        }
    }
    
    private byte[] toByteArray() throws IOException {
        ByteArrayOutputStream bof = new ByteArrayOutputStream();
        byte[] buff = new byte[8192];
        int c = -1;
        InputStream is = toInputStream();
        while((c = is.read(buff))>-1){
            bof.write(buff, 0, c);
        }
        return bof.toByteArray();
    }
    
    private InputStream toInputStream() throws IOException{
        InputStream is = null;
        if (type==Type.BYTE){
            is = new ByteArrayInputStream(contentByte);
        }
        if (type==Type.FILE){
                is = new FileInputStream(contentFile);
        }
        if (type==Type.STREAM){
            if (alreadyGiven) throw new AlreadyDeliveredException("The stream was already used");
            alreadyGiven = true;
            is = contentStream;
        }
        return is;
    }
    
}

