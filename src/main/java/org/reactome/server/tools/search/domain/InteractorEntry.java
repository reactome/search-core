package org.reactome.server.tools.search.domain;

import java.util.List;

/**
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */

public class InteractorEntry {

    private String accession;
    private String name;
    private String url;

    private List<Interactor> interactions;

    public String getAccession() {
        return accession;
    }

    public void setAccession(String accession) {
        this.accession = accession;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Interactor> getInteractions() {
        return interactions;
    }

    public void setInteractions(List<Interactor> interactions) {
        this.interactions = interactions;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
