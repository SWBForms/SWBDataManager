package org.semanticwb.datamanager.filestore;

/**
 *
 * @author serch
 */
public interface SWBFileSource {
    public SWBFileObject getFile(String name);
    public void storeFile(SWBFileObject file);
    public int getMaxSize();
}
