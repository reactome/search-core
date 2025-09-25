package org.reactome.server.search.domain;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.Nullable;

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Internal Model for Reactome Entries
 *
 * @author Florian Korninger (fkorn@ebi.ac.uk)
 * @version 1.0
 */
@SuppressWarnings("unused")
@Getter
@Setter
public class Entry {
    private String dbId;
    private String stId;
    private String id;
    private String name;
    private String type; // translated type from SchemaClass enum
    private String instanceTypeExplanation;
    private String exactType;
    private List<String> species;
    private String summation;
    private String referenceName;
    private String referenceIdentifier;
    private List<String> compartmentNames;
    private List<String> compartmentAccession;
    private Boolean isDisease;
    private Boolean hasReferenceEntity;
    private Boolean hasEHLD;
    private String databaseName;
    private String referenceURL;
    private String regulator;
    private String regulatedEntity;
    private String regulatorId;
    private String regulatedEntityId;
    private String authoredPathways;
    private String authoredReactions;
    private String reviewedPathways;
    private String reviewedReactions;
    private String orcidId;
    private List<String> fireworksSpecies;

    // Icons
    private String iconName; // original filename, name might have solr highlighting
    private List<String> iconCategories;
    private String iconCuratorName;
    private String iconCuratorOrcidId;
    private String iconCuratorUrl;
    private String iconDesignerName;
    private String iconDesignerOrcidId;
    private String iconDesignerUrl;
    private List<String> iconReferences;
    private Set<IconPhysicalEntity> iconPhysicalEntities;
    private List<String> iconEhlds;

    // Deleted
    private Boolean deleted;
    private String reason;
    private String explanation;
    private Date date;
    private List<Long> replacementDbIds;
    private List<String> replacementStIds;

    @Nullable
    public Boolean isIcon() {
        return this.iconName != null ? true : null;
    }

    public boolean isDeleted() {
        return this.deleted != null ? this.deleted : false;
    }

    /**
     * Get entry name
     *
     * @return name might contain highlighting added by solr.
     */
    public String getName() {
        return name;
    }


    /**
     * This is the iconName. @getName() returns the same information.
     * However if a solr search hits the name, then solr highlights the term in the name,
     * and we can't use inside a title or as just name.
     *
     * @return plain icon name, same as written in the <name></name> in the metadata
     */
    public String getIconName() {
        return iconName;
    }


    public Boolean getDisease() {
        return isDisease;
    }

    public void setDisease(Boolean disease) {
        isDisease = disease;
    }


}
