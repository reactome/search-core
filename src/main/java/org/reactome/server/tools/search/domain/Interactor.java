package org.reactome.server.tools.search.domain;

import java.util.List;

/**
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */

public class Interactor implements Comparable<Interactor> {

    private List<InteractorReactomeEntry> interactorReactomeEntries;
    private Double score;
    private List<String> interactionEvidences;
    private String accession;
    private String accessionURL;
    private String evidencesURL;

    public List<InteractorReactomeEntry> getInteractorReactomeEntries() {
        return interactorReactomeEntries;
    }

    public void setInteractorReactomeEntries(List<InteractorReactomeEntry> interactorReactomeEntries) {
        this.interactorReactomeEntries = interactorReactomeEntries;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public List<String> getInteractionEvidences() {
        return interactionEvidences;
    }

    public void setInteractionEvidences(List<String> interactionEvidences) {
        this.interactionEvidences = interactionEvidences;
    }

    public String getAccession() {
        return accession;
    }

    public void setAccession(String accession) {
        this.accession = accession;
    }

    public String getAccessionURL() {
        return accessionURL;
    }

    public void setAccessionURL(String accessionURL) {
        this.accessionURL = accessionURL;
    }

    public String getEvidencesURL() {
        return evidencesURL;
    }

    public void setEvidencesURL(String evidencesURL) {
        this.evidencesURL = evidencesURL;
    }

    @Override
    public int compareTo(Interactor otherInteraction) {
        return this.score.compareTo(otherInteraction.getScore());
    }
}
