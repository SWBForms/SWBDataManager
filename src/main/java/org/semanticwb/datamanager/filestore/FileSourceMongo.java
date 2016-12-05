package org.semanticwb.datamanager.filestore;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSInputFile;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.logging.Logger;
import org.semanticwb.datamanager.exceptions.SWBDataManagerException;
import org.semanticwb.datamanager.script.ScriptObject;

/**
 *
 * @author serch
 */
public class FileSourceMongo implements SWBFileSource {

    static private Logger log = Logger.getLogger("o.s.d.f.FileSourceMongo");
    private final String collection;
    private final MongoClient mongoClient;
    private final DB mongoDB;
    private final int maxSize;
    private final int cachableSize;

    public FileSourceMongo(ScriptObject fileSource, ScriptObject ds) throws UnknownHostException {
        collection = fileSource.getString("scls");
        maxSize = fileSource.getInt("maxSize");
        cachableSize = fileSource.getInt("cachableSize");
        //TODO:configurar direccion de servidor
        if ((null != ds.getString("envhost") && null != ds.getString("envport"))) {
            String host = System.getenv(ds.getString("envhost"));
            //log.info("Host:"+host);
            int port = Integer.parseInt(System.getenv(ds.getString("envport")));
            //log.info("Port:"+port);
            log.fine("Connecting to: host:port -> " + host + ":" + port);
            mongoClient = new MongoClient(host, port);
        } else {
            mongoClient = new MongoClient(ds.getString("host"), (Integer) ds.get("port").getValue());
        }
        mongoDB = mongoClient.getDB(fileSource.getString("modelid"));
    }

    @Override
    public SWBFileObject getFile(String name) {
        SWBFileObject ret=null;
        GridFS fs = new GridFS(mongoDB, collection);
        GridFSDBFile fileForOutput = fs.findOne(name);
        if (null != fileForOutput) {
            if (cachableSize > fileForOutput.getLength()) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream(cachableSize);
                try {
                    fileForOutput.writeTo(baos);
                } catch (IOException ioe) {
                    throw new SWBDataManagerException(ioe);
                }
                ret = new SWBFileObject(fileForOutput.getFilename(), 
                        fileForOutput.getContentType(), 
                        baos.toByteArray());
            } else {
                ret = new SWBFileObject(fileForOutput.getFilename(),
                        fileForOutput.getContentType(),
                        fileForOutput.getInputStream());
            }
        }
        return ret;
    }

    @Override
    public void storeFile(SWBFileObject file) {
        GridFS fs = new GridFS(mongoDB, collection);
        fs.remove(file.getName());
        GridFSInputFile inputFile = fs.createFile(file.getContentAsByteArray());
        inputFile.setFilename(file.getName());
        inputFile.setContentType(file.getContentType());
        inputFile.save();
    }

    @Override
    public int getMaxSize() {
        return maxSize;
    }

    
}
