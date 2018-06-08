package org.reactome.server.search.util;

/**
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */

public class ReportEntity {
    private String term;
    private String resource;

    public ReportEntity() {
    }

    public ReportEntity(String term, String resource) {
        this.term = term;
        this.resource = resource;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }
}
