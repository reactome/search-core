package org.reactome.server.search.domain;

/**
 * POJO to be used by the search in the diagram.
 * It provides general information such as number of entries found in the current diagram and faceting
 * and number of entries found in other diagrams (all) and faceting.
 *
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */
public class DiagramSearchSummary {
    private DiagramResult diagramResult;
    private FireworksResult fireworksResult;

    public DiagramSearchSummary(DiagramResult diagramResult, FireworksResult fireworksResult) {
        this.diagramResult = diagramResult;
        this.fireworksResult = fireworksResult;
    }

    public DiagramResult getDiagramResult() {
        return diagramResult;
    }

    public void setDiagramResult(DiagramResult diagramResult) {
        this.diagramResult = diagramResult;
    }

    public FireworksResult getFireworksResult() {
        return fireworksResult;
    }

    public void setFireworksResult(FireworksResult fireworksResult) {
        this.fireworksResult = fireworksResult;
    }
}
