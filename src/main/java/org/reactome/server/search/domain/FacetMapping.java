package org.reactome.server.search.domain;

/**
 * Internal Model for Reactome Entries
 * @author Florian Korninger (fkorn@ebi.ac.uk)
 * @version 1.0
 */
@SuppressWarnings("unused")
public class FacetMapping {

    private long totalNumFount;
    private FacetList speciesFacet;
    private FacetList typeFacet;
    private FacetList keywordFacet;
    private FacetList compartmentFacet;
    private FacetList iconCategoriesFacet;

    public long getTotalNumFount() {
        return totalNumFount;
    }

    public void setTotalNumFount(long totalNumFount) {
        this.totalNumFount = totalNumFount;
    }

    public FacetList getSpeciesFacet() {
        return speciesFacet;
    }

    public void setSpeciesFacet(FacetList speciesFacet) {
        this.speciesFacet = speciesFacet;
    }

    public FacetList getTypeFacet() {
        return typeFacet;
    }

    public void setTypeFacet(FacetList typeFacet) {
        this.typeFacet = typeFacet;
    }

    public FacetList getKeywordFacet() {
        return keywordFacet;
    }

    public void setKeywordFacet(FacetList keywordFacet) {
        this.keywordFacet = keywordFacet;
    }

    public FacetList getCompartmentFacet() {
        return compartmentFacet;
    }

    public void setCompartmentFacet(FacetList compartmentFacet) {
        this.compartmentFacet = compartmentFacet;
    }

    public FacetList getIconCategoriesFacet() {
        return iconCategoriesFacet;
    }

    public void setIconCategoriesFacet(FacetList iconCategoriesFacet) {
        this.iconCategoriesFacet = iconCategoriesFacet;
    }
}
