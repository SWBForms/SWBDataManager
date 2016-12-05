package org.semanticwb.datamanager.exceptions;

/**
 *
 * @author serch
 */
public class SWBDataManagerException extends RuntimeException {

    public SWBDataManagerException() {
    }

    public SWBDataManagerException(String message) {
        super(message);
    }

    public SWBDataManagerException(String message, Throwable cause) {
        super(message, cause);
    }

    public SWBDataManagerException(Throwable cause) {
        super(cause);
    }
    
}
