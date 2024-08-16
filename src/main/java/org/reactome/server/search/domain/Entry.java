package org.reactome.server.search.domain;


import java.util.List;
import java.util.Set;

/**
 * Internal Model for Reactome Entries
 * @author Florian Korninger (fkorn@ebi.ac.uk)
 * @version 1.0
 */
@SuppressWarnings("unused")
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
    private String reason;
    private String explanation;
    private String date;
    private List<Long> replacementDbIds;
    private List<String> replacementStIds;

    public boolean isIcon() {
        return this.type.equalsIgnoreCase("icon");
    }

    public boolean isDeleted() {
        return this.type.equalsIgnoreCase("deleted");
    }

    public String getRegulatorId() {
        return regulatorId;
    }

    public void setRegulatorId(String regulatorId) {
        this.regulatorId = regulatorId;
    }

    public String getRegulatedEntityId() {
        return regulatedEntityId;
    }

    public void setRegulatedEntityId(String regulatedEntityId) {
        this.regulatedEntityId = regulatedEntityId;
    }

    public String getRegulator() {
        return regulator;
    }

    public void setRegulator(String regulator) {
        this.regulator = regulator;
    }

    public String getRegulatedEntity() {
        return regulatedEntity;
    }

    public void setRegulatedEntity(String regulatedEntity) {
        this.regulatedEntity = regulatedEntity;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public String getReferenceURL() {
        return referenceURL;
    }

    public void setReferenceURL(String referenceURL) {
        this.referenceURL = referenceURL;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Boolean getIsDisease() {
        return isDisease;
    }

    public void setIsDisease(Boolean isDisease) {
        this.isDisease = isDisease;
    }

    public String getExactType() {
        return exactType;
    }

    public void setExactType(String exactType) {
        this.exactType = exactType;
    }

    public List<String> getCompartmentNames() {
        return compartmentNames;
    }

    public void setCompartmentNames(List<String> compartmentNames) {
        this.compartmentNames = compartmentNames;
    }

    public List<String> getCompartmentAccession() {
        return compartmentAccession;
    }

    public void setCompartmentAccession(List<String> compartmentAccession) {
        this.compartmentAccession = compartmentAccession;
    }

    public String getDbId() {
        return dbId;
    }

    public void setDbId(String dbId) {
        this.dbId = dbId;
    }

    public String getStId() {
        return stId;
    }

    public void setStId(String stId) {
        this.stId = stId;
    }

    /**
     * Get entry name
     * @return name might contain highlighting added by solr.
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getSpecies() {
        return species;
    }

    public void setSpecies(List<String> species) {
        this.species = species;
    }

    public String getSummation() {
        return summation;
    }

    public void setSummation(String summation) {
        this.summation = summation;
    }

    public String getReferenceName() {
        return referenceName;
    }

    public void setReferenceName(String referenceName) {
        this.referenceName = referenceName;
    }

    public String getReferenceIdentifier() {
        return referenceIdentifier;
    }

    public void setReferenceIdentifier(String referenceIdentifier) {
        this.referenceIdentifier = referenceIdentifier;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getInstanceTypeExplanation() {
        return instanceTypeExplanation;
    }

    public void setInstanceTypeExplanation(String instanceTypeExplanation) {
        this.instanceTypeExplanation = instanceTypeExplanation;
    }

    public String getAuthoredPathways() {
        return authoredPathways;
    }

    public void setAuthoredPathways(String authoredPathways) {
        this.authoredPathways = authoredPathways;
    }

    public String getAuthoredReactions() {
        return authoredReactions;
    }

    public void setAuthoredReactions(String authoredReactions) {
        this.authoredReactions = authoredReactions;
    }

    public String getReviewedPathways() {
        return reviewedPathways;
    }

    public void setReviewedPathways(String reviewedPathways) {
        this.reviewedPathways = reviewedPathways;
    }

    public String getReviewedReactions() {
        return reviewedReactions;
    }

    public void setReviewedReactions(String reviewedReactions) {
        this.reviewedReactions = reviewedReactions;
    }

    public String getOrcidId() {
        return orcidId;
    }

    public void setOrcidId(String orcidId) {
        this.orcidId = orcidId;
    }

    public List<String> getFireworksSpecies() {
        return fireworksSpecies;
    }

    public void setFireworksSpecies(List<String> fireworksSpecies) {
        this.fireworksSpecies = fireworksSpecies;
    }

    /**
     * This is the iconName. @getName() returns the same information.
     * However if a solr search hits the name, then solr highlights the term in the name,
     * and we can't use inside a title or as just name.
     * @return plain icon name, same as written in the <name></name> in the metadata
     */
    public String getIconName() {
        return iconName;
    }

    public void setIconName(String iconName) {
        this.iconName = iconName;
    }

    public List<String> getIconCategories() {
        return iconCategories;
    }

    public void setIconCategories(List<String> iconCategories) {
        this.iconCategories = iconCategories;
    }

    public String getIconCuratorName() {
        return iconCuratorName;
    }

    public void setIconCuratorName(String iconCuratorName) {
        this.iconCuratorName = iconCuratorName;
    }

    public String getIconCuratorOrcidId() {
        return iconCuratorOrcidId;
    }

    public void setIconCuratorOrcidId(String iconCuratorOrcidId) {
        this.iconCuratorOrcidId = iconCuratorOrcidId;
    }

    public String getIconCuratorUrl() {
        return iconCuratorUrl;
    }

    public void setIconCuratorUrl(String iconCuratorUrl) {
        this.iconCuratorUrl = iconCuratorUrl;
    }

    public String getIconDesignerName() {
        return iconDesignerName;
    }

    public void setIconDesignerName(String iconDesignerName) {
        this.iconDesignerName = iconDesignerName;
    }

    public String getIconDesignerOrcidId() {
        return iconDesignerOrcidId;
    }

    public void setIconDesignerOrcidId(String iconDesignerOrcidId) {
        this.iconDesignerOrcidId = iconDesignerOrcidId;
    }

    public String getIconDesignerUrl() {
        return iconDesignerUrl;
    }

    public void setIconDesignerUrl(String iconDesignerUrl) {
        this.iconDesignerUrl = iconDesignerUrl;
    }

    public List<String> getIconReferences() {
        return iconReferences;
    }

    public void setIconReferences(List<String> iconReferences) {
        this.iconReferences = iconReferences;
    }

    public Set<IconPhysicalEntity> getIconPhysicalEntities() {
        return iconPhysicalEntities;
    }

    public void setIconPhysicalEntities(Set<IconPhysicalEntity> iconPhysicalEntities) {
        this.iconPhysicalEntities = iconPhysicalEntities;
    }

    public List<String> getIconEhlds() {
        return iconEhlds;
    }

    public void setIconEhlds(List<String> iconEhlds) {
        this.iconEhlds = iconEhlds;
    }

    public Boolean getDisease() {
        return isDisease;
    }

    public void setDisease(Boolean disease) {
        isDisease = disease;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public List<Long> getReplacementDbIds() {
        return replacementDbIds;
    }

    public void setReplacementDbIds(List<Long> replacementDbIds) {
        this.replacementDbIds = replacementDbIds;
    }

    public List<String> getReplacementStIds() {
        return replacementStIds;
    }

    public void setReplacementStIds(List<String> replacementStIds) {
        this.replacementStIds = replacementStIds;
    }
}
