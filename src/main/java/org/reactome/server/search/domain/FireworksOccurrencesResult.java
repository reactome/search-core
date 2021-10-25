package org.reactome.server.search.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */
public class FireworksOccurrencesResult {

    private final Set<String> llps = new HashSet<>();
    private final Set<String> interactsWith = new HashSet<>();

    public FireworksOccurrencesResult() {
    }

    public void addLlps(Collection<String> llps){
        if(llps!=null) this.llps.addAll(llps);
    }

    public Collection<String> getLlps() {
        return llps.isEmpty() ? null : llps;
    }

    public void addInteractsWith(String interactsWith){
        if(interactsWith!=null) this.interactsWith.add(interactsWith);
    }

    public Collection<String> getInteractsWith() {
        return interactsWith.isEmpty() ? null : interactsWith;
    }

    @JsonIgnore
    public boolean isEmpty(){
        return llps.isEmpty() && interactsWith.isEmpty();
    }
}
