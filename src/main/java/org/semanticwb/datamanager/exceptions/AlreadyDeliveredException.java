package org.semanticwb.datamanager.exceptions;

/**
 *
 * @author serch
 */
public class AlreadyDeliveredException extends RuntimeException {

    public AlreadyDeliveredException() {
    }

    public AlreadyDeliveredException(String message) {
        super(message);
    }

    public AlreadyDeliveredException(String message, Throwable cause) {
        super(message, cause);
    }

    public AlreadyDeliveredException(Throwable cause) {
        super(cause);
    }
    
}
