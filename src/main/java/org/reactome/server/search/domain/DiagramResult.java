package org.reactome.server.search.domain;

import java.util.List;

/**
 * @author Guilherme Viteri (gviteri@ebi.ac.uk)
 */
public class DiagramResult {

    private List<Entry> entries;
    private List<FacetContainer> facets;
    private Long found;

    public DiagramResult(List<Entry> entries, List<FacetContainer> facets, Long found) {
        this.entries = entries;
        this.facets = facets;
        this.found = found;
    }

    public List<Entry> getEntries() {
        return entries;
    }

    public void setEntries(List<Entry> entries) {
        this.entries = entries;
    }

    public List<FacetContainer> getFacets() {
        return facets;
    }

    public void setFacets(List<FacetContainer> facets) {
        this.facets = facets;
    }

    public Long getFound() {
        return found;
    }

    public void setFound(Long found) {
        this.found = found;
    }
}
