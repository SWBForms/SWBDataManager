package org.semanticwb.datamanager.exceptions;

/**
 *
 * @author serch
 */
public class SWBDataManagerException extends RuntimeException {

    /**
     *
     */
    public SWBDataManagerException() {
    }

    /**
     *
     * @param message
     */
    public SWBDataManagerException(String message) {
        super(message);
    }

    /**
     *
     * @param message
     * @param cause
     */
    public SWBDataManagerException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     *
     * @param cause
     */
    public SWBDataManagerException(Throwable cause) {
        super(cause);
    }
    
}
