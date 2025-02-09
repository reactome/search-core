package org.reactome.server.search.domain;

import java.util.List;
import java.util.Set;

/**
 * @author Antonio Fabregat (fabregat@ebi.ac.uk)
 */
public class FireworksResult {
    List<Entry> entries;
    List<FacetContainer> facets;
    Long found;
    Set<TargetResult> targetResults;

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

    public Set<TargetResult> getTargetResults() {
        return targetResults;
    }

    public void setTargetResults(Set<TargetResult> targetResults) {
        this.targetResults = targetResults;
    }
}
