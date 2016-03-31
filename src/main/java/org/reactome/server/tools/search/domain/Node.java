package org.reactome.server.tools.search.domain;

import javax.annotation.Nonnull;
import java.util.Set;
import java.util.TreeSet;

/**
 * Internal Model for Reactome Entries
 * @author Florian Korninger (fkorn@ebi.ac.uk)
 * @version 1.0
 */
@SuppressWarnings("unused")
public class Node implements Comparable<Node> {

    private String stId;
    private String name;
    private String species;
    private String url;
    private String type;
    private Boolean diagram;
    private Boolean unique;

    private Set<Node> children;
    private Set<Node> parent;

    public void addParent(Node node) {
        if (parent==null) {
            parent = new TreeSet<>();
        }
        parent.add(node);
    }
    public void addChild(Node node) {
        if (children==null) {
            children = new TreeSet<>();
        }
        children.add(node);
    }

    public Set<Node> getLeaves() {
        Set<Node> leaves = new TreeSet<>();
        if (this.children == null) {
            leaves.add(this);
        } else {
            for (Node child : this.children) {
                leaves.addAll(child.getLeaves());
            }
        }
        return leaves;
    }



    public String getStId() {
        return stId;
    }

    public void setStId(String stId) {
        this.stId = stId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSpecies() {
        return species;
    }

    public void setSpecies(String species) {
        this.species = species;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Boolean hasDiagram() {
        return diagram;
    }

    public void setDiagram(Boolean diagram) {
        this.diagram = diagram;
    }

    public Boolean isUnique() {
        return unique;
    }

    public void setUnique(Boolean unique) {
        this.unique = unique;
    }

    public Set<Node> getChildren() {
        return children;
    }

    public void setChildren(Set<Node> children) {
        this.children = children;
    }

    public Set<Node> getParent() {
        return parent;
    }

    public void setParent(Set<Node> parent) {
        this.parent = parent;
    }

    @Override
    public int compareTo(@Nonnull Node node) {
        // If stId is not present we set it as the dbId
        return this.stId.compareTo(node.stId);
    }


}
