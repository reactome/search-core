package org.reactome.server.search.domain;

import java.util.List;

/**
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */
public class DiagramOccurrencesResult {

    // flag if the search term is present in the given diagram
    private String inDiagram;
    private Boolean isInDiagram;
    private List<String> occurrences;
    private List<String> interactsWith;

    public DiagramOccurrencesResult(String inDiagram, List<String> occurrences, List<String> interactsWith) {
        this.inDiagram = inDiagram;
        this.isInDiagram = inDiagram != null && !inDiagram.isEmpty();
        this.occurrences = occurrences;
        this.interactsWith = interactsWith;
    }

    public DiagramOccurrencesResult(Boolean isInDiagram, List<String> occurrences, List<String> interactsWith) {
        this.isInDiagram = isInDiagram;
        this.occurrences = occurrences;
        this.interactsWith = interactsWith;
    }

    public String getInDiagram() {
        return inDiagram;
    }

    public Boolean isInDiagram() { return isInDiagram;}

    public List<String> getOccurrences() {
        return occurrences;
    }

    public List<String> getInteractsWith() {
        return interactsWith;
    }

}
