package org.semanticwb.datamanager.filestore;

/**
 *
 * @author serch
 */
public interface SWBFileSource {

    /**
     *
     * @param name
     * @return
     */
    public SWBFileObject getFile(String name);

    /**
     *
     * @param file
     */
    public void storeFile(SWBFileObject file);

    /**
     *
     * @return
     */
    public int getMaxSize();
}
