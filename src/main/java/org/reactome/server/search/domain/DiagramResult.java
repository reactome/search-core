package org.reactome.server.search.domain;

import java.util.List;

/**
 * @author Guilherme Viteri <gviteri@ebi.ac.uk>
 */
public class DiagramResult {

    private List<Entry> entries;
    private Long found;

    public DiagramResult(List<Entry> entries, Long found) {
        this.entries = entries;
        this.found = found;
    }

    public List<Entry> getEntries() {
        return entries;
    }

    public void setEntries(List<Entry> entries) {
        this.entries = entries;
    }

    public Long getFound() {
        return found;
    }

    public void setFound(Long found) {
        this.found = found;
    }
}
