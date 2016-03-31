package org.reactome.server.tools.search.domain;

/**
 * @author Florian Korninger (fkorn@ebi.ac.uk)
 * @version 1.0
 */
@SuppressWarnings("unused")
public class Regulation {

    private EntityReference regulator;
    private EntityReference regulatedEntity;
    private String regulationType;

    public String getRegulationType() {
        return regulationType;
    }

    public void setRegulationType(String regulationType) {
        this.regulationType = regulationType;
    }

    public EntityReference getRegulator() {
        return regulator;
    }

    public void setRegulator(EntityReference regulator) {
        this.regulator = regulator;
    }

    public EntityReference getRegulatedEntity() {
        return regulatedEntity;
    }

    public void setRegulatedEntity(EntityReference regulatedEntity) {
        this.regulatedEntity = regulatedEntity;
    }
}
