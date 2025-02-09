package org.reactome.server.search.domain;

/**
 * @author Guilherme S Viteri (gviteri@ebi.ac.uk)
 */

public class IconPhysicalEntity implements Comparable<IconPhysicalEntity> {

    private String stId;
    private String type;
    private String name;
    private String compartments;

    public IconPhysicalEntity(String stId, String type, String name, String compartments) {
        this.stId = stId;
        this.name = name;
        this.type = type;
        this.compartments = compartments;
    }

    public String getStId() {
        return stId;
    }

    public void setStId(String stId) {
        this.stId = stId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCompartments() {
        return compartments;
    }

    public void setCompartments(String compartments) {
        this.compartments = compartments;
    }

    public String getDisplayName() {
        return name + (compartments.isBlank() ? "" : " [" + compartments + "]");
    }

    @Override
    public int compareTo(IconPhysicalEntity o) {
        return this.getDisplayName().compareToIgnoreCase(o.getDisplayName());
    }
}
