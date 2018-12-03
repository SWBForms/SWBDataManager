package org.semanticwb.datamanager.exceptions;

/**
 *
 * @author serch
 */
public class AlreadyDeliveredException extends RuntimeException {

    /**
     *
     */
    public AlreadyDeliveredException() {
    }

    /**
     *
     * @param message
     */
    public AlreadyDeliveredException(String message) {
        super(message);
    }

    /**
     *
     * @param message
     * @param cause
     */
    public AlreadyDeliveredException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     *
     * @param cause
     */
    public AlreadyDeliveredException(Throwable cause) {
        super(cause);
    }
    
}
