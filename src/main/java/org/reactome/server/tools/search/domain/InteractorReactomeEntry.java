package org.reactome.server.tools.search.domain;

/**
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */
@SuppressWarnings("unused")
public class InteractorReactomeEntry {

    private String reactomeId;
    private String reactomeName;

    public InteractorReactomeEntry(String reactomeId, String reactomeName) {
        this.reactomeId = reactomeId;
        this.reactomeName = reactomeName;
    }

    public String getReactomeId() {
        return reactomeId;
    }

    public void setReactomeId(String reactomeId) {
        this.reactomeId = reactomeId;
    }

    public String getReactomeName() {
        return reactomeName;
    }

    public void setReactomeName(String reactomeName) {
        this.reactomeName = reactomeName;
    }
}
