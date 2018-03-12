package org.reactome.server.search.domain;

import java.util.List;

/**
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */
public class DiagramOccurrencesResult {

    // flag if the search term is present in the given diagram
    private Boolean isInDiagram;
    private List<String> occurrences;

    public DiagramOccurrencesResult(Boolean isInDiagram, List<String> occurrences) {
        this.isInDiagram = isInDiagram;
        this.occurrences = occurrences;
    }

    public Boolean getInDiagram() {
        return isInDiagram;
    }

    public void setInDiagram(Boolean inDiagram) {
        isInDiagram = inDiagram;
    }

    public List<String> getOccurrences() {
        return occurrences;
    }

    public void setOccurrences(List<String> occurrences) {
        this.occurrences = occurrences;
    }
}
