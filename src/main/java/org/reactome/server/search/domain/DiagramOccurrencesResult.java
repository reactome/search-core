package org.reactome.server.search.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */
public class DiagramOccurrencesResult {

    // flag if the search term is present in the given diagram
    private String diagramEntity;
    private Boolean inDiagram;
    private final Set<String> occurrences = new HashSet<>();
    private final Set<String> interactsWith = new HashSet<>();

    public DiagramOccurrencesResult() {
    }

    public DiagramOccurrencesResult(String diagramEntity, Boolean inDiagram, Collection<String> occurrences, Collection<String> interactsWith) {
        this.diagramEntity = diagramEntity;
        this.inDiagram = inDiagram;
        addOccurrences(occurrences);
        addInteractsWith(interactsWith);
    }

    public DiagramOccurrencesResult(String diagramEntity, Collection<String> occurrences, Collection<String> interactsWith) {
        this.diagramEntity = diagramEntity;
        this.inDiagram = diagramEntity != null && !diagramEntity.isEmpty();
        addOccurrences(occurrences);
        addInteractsWith(interactsWith);
    }

    public DiagramOccurrencesResult(Boolean inDiagram, Collection<String> occurrences, Collection<String> interactsWith) {
        this.inDiagram = inDiagram;
        addOccurrences(occurrences);
        addInteractsWith(interactsWith);
    }

    public String getDiagramEntity() {
        return diagramEntity;
    }

    public Boolean getInDiagram() { return inDiagram;}

    public void addOccurrences(Collection<String> occurrences){
        if(occurrences!=null) this.occurrences.addAll(occurrences);
    }

    public Collection<String> getOccurrences() {
        return occurrences.isEmpty() ? null : occurrences;
    }

    public void addInteractsWith(Collection<String> interactsWith){
        if(interactsWith!=null) this.interactsWith.addAll(interactsWith);
    }

    public Collection<String> getInteractsWith() {
        return interactsWith.isEmpty() ? null : interactsWith;
    }

    @JsonIgnore
    public boolean isEmpty(){
        return occurrences.isEmpty() && interactsWith.isEmpty();
    }
}
