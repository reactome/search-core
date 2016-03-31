package org.reactome.server.tools.search.exception;

/**
 * TODO IMPLEMENT
 *
 * @author Florian Korninger (fkorn@ebi.ac.uk)
 * @version 1.0
 */
@SuppressWarnings("unused")
public class EnricherException extends Exception{

    protected EnricherException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public EnricherException() {
        super();
    }

    public EnricherException(String message) {
        super(message);
    }

    public EnricherException(String message, Throwable cause) {
        super(message, cause);
    }

    public EnricherException(Throwable cause) {
        super(cause);
    }
}
