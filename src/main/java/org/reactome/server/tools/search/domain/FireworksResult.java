package org.reactome.server.tools.search.domain;

import java.util.List;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
public class FireworksResult {

    List<Entry> entries;

    List<FacetContainer> facets;

    Long found;


    public FireworksResult(List<Entry> entries, List<FacetContainer> facets, Long found) {
        this.entries = entries;
        this.facets = facets;
        this.found = found;
    }

    public List<Entry> getEntries() {
        return entries;
    }

    public List<FacetContainer> getFacets() {
        return facets;
    }

    public Long getFound() {
        return found;
    }

}
