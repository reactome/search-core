package org.reactome.server.search.exception;

/**
 * TODO IMPLEMENT
 *
 * @author Florian Korninger (fkorn@ebi.ac.uk)
 * @version 1.0
 */
@SuppressWarnings("unused")
public class SolrSearcherException extends Exception {

    public SolrSearcherException(String message, Throwable cause) {
        super(message, cause);
    }

    public SolrSearcherException(String message) { super(message); }
}
