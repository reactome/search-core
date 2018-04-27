package org.reactome.server.search.domain;

import java.util.List;
import java.util.Set;

/**
 * Internal Model for Reactome Entries
 * @author Florian Korninger (fkorn@ebi.ac.uk)
 * @version 1.0
 */
@SuppressWarnings("unused")
public class GroupedResult {

    private List<Result> results;
    private int rowCount;
    private int numberOfGroups;
    private int numberOfMatches;
    private Set<TargetResult> targetResults;


    public GroupedResult(List<Result> results, int rowCount, Integer numberOfGroups, int numberOfMatches) {
        this.results = results;
        this.numberOfGroups = numberOfGroups;
        this.numberOfMatches = numberOfMatches;
        this.rowCount = rowCount;
    }

    public int getNumberOfMatches() {
        return numberOfMatches;
    }

    public void setNumberOfMatches(int numberOfMatches) {
        this.numberOfMatches = numberOfMatches;
    }

    public int getRowCount() {
        return rowCount;
    }

    public void setRowCount(int rowCount) {
        this.rowCount = rowCount;
    }

    public int getNumberOfGroups() {
        return numberOfGroups;
    }

    public void setNumberOfGroups(int numberOfGroups) {
        this.numberOfGroups = numberOfGroups;
    }

    public List<Result> getResults() {
        return results;
    }

    public void setResults(List<Result> results) {
        this.results = results;
    }

    public Set<TargetResult> getTargetResults() {
        return targetResults;
    }

    public void setTargetResults(Set<TargetResult> targetResults) {
        this.targetResults = targetResults;
    }
}
