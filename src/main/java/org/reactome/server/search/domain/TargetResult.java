package org.reactome.server.search.domain;

import java.util.Objects;

/**
 *
 * @author Guilherme S Viteri (gviteri@ebi.ac.uk)
 */
public class TargetResult {
    private String term;
    private String resource;
    private boolean isTarget;

    public TargetResult(String term, String resource, boolean isTarget) {
        this.term = term;
        this.resource = resource;
        this.isTarget = isTarget;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public boolean isTarget() {
        return isTarget;
    }

    public void setTarget(boolean target) {
        isTarget = target;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TargetResult that = (TargetResult) o;
        return Objects.equals(term, that.term);
    }

    @Override
    public int hashCode() {
        return Objects.hash(term);
    }
}
