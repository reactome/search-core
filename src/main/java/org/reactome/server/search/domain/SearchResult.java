package org.reactome.server.search.domain;

/**
 * Created by:
 *
 * @author Florian Korninger (florian.korninger@ebi.ac.uk)
 * @since 01.04.16.
 */
@SuppressWarnings("unused")
public class SearchResult {

    private FacetMapping facetMapping;
    private GroupedResult groupedResult;
    private double resultCount;
    private int rows;

    public SearchResult(FacetMapping facetMapping, GroupedResult groupedResult, double resultCount, int rows) {
        this.facetMapping = facetMapping;
        this.groupedResult = groupedResult;
        this.resultCount = resultCount;
        this.rows = rows;
    }

    public FacetMapping getFacetMapping() {
        return facetMapping;
    }

    public void setFacetMapping(FacetMapping facetMapping) {
        this.facetMapping = facetMapping;
    }

    public GroupedResult getGroupedResult() {
        return groupedResult;
    }

    public void setGroupedResult(GroupedResult groupedResult) {
        this.groupedResult = groupedResult;
    }

    public double getResultCount() {
        return resultCount;
    }

    public void setResultCount(double resultCount) {
        this.resultCount = resultCount;
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }
}
