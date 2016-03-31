package org.reactome.server.tools.search.domain;

/**
 * Internal Model for Reactome Entries
 * @author Florian Korninger (fkorn@ebi.ac.uk)
 * @version 1.0
 */
@SuppressWarnings("unused")
public class ModifiedResidue {

    private String name;

    private Integer coordinate;
    private PsiMod psiMod;
    private EntityReference modification;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getCoordinate() {
        return coordinate;
    }

    public void setCoordinate(Integer coordinate) {
        this.coordinate = coordinate;
    }

    public PsiMod getPsiMod() {
        return psiMod;
    }

    public void setPsiMod(PsiMod psiMod) {
        this.psiMod = psiMod;
    }

    public EntityReference getModification() {
        return modification;
    }

    public void setModification(EntityReference modification) {
        this.modification = modification;
    }
}
