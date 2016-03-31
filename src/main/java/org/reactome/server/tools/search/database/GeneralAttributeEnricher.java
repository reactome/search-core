package org.reactome.server.tools.search.database;

import org.gk.model.GKInstance;
import org.gk.model.ReactomeJavaConstants;
import org.reactome.server.tools.search.domain.Disease;
import org.reactome.server.tools.search.domain.EnrichedEntry;
import org.reactome.server.tools.search.domain.Literature;
import org.reactome.server.tools.search.domain.Node;
import org.reactome.server.tools.search.exception.EnricherException;
import org.reactome.server.tools.search.util.InstanceTypeExplanation;
import org.reactome.server.tools.search.util.SchemaClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static org.reactome.server.tools.search.database.EnricherUtil.*;

/**
 * Queries the MySql database and converts entry to a local object
 *
 * @author Florian Korninger (fkorn@ebi.ac.uk)
 * @version 1.0
 */
class GeneralAttributeEnricher {

    private static final Logger logger = LoggerFactory.getLogger(Enricher.class);

    private static final String PUBMED_URL = "http://www.ncbi.nlm.nih.gov/pubmed/";

    public static void setGeneralAttributes(GKInstance instance, EnrichedEntry enrichedEntry) throws EnricherException {
        try {
            List<String> names = getAttributes(instance, ReactomeJavaConstants.name);
            if (names != null && !names.isEmpty()) {
                if (names.size() >= 1) {
                    enrichedEntry.setName(names.get(0));
                    if (names.size() > 1) {
                        enrichedEntry.setSynonyms(names.subList(1, names.size() - 1));
                    }
                }
            } else {
                enrichedEntry.setName(instance.getDisplayName());
            }
            if (hasValue(instance, ReactomeJavaConstants.stableIdentifier)) {
                enrichedEntry.setStId((String) ((GKInstance) instance.getAttributeValue(ReactomeJavaConstants.stableIdentifier)).getAttributeValue(ReactomeJavaConstants.identifier));
            }
            enrichedEntry.setSpecies(getAttributeDisplayName(instance, ReactomeJavaConstants.species));
            List<?> summationInstances = instance.getAttributeValuesList(ReactomeJavaConstants.summation);
            List<String> summations = new ArrayList<>();
            for (Object summationInstance : summationInstances) {
                GKInstance summation = (GKInstance) summationInstance;
                summations.add((String) summation.getAttributeValue(ReactomeJavaConstants.text));
            }
            enrichedEntry.setSummations(summations);
            enrichedEntry.setCompartments(getGoTerms(instance, ReactomeJavaConstants.compartment));
            enrichedEntry.setInferredFrom(getEntityReferences(instance, ReactomeJavaConstants.inferredFrom));
            enrichedEntry.setOrthologousEvents(getEntityReferences(instance, ReactomeJavaConstants.orthologousEvent));
            enrichedEntry.setCrossReferences(getCrossReferences(instance, ReactomeJavaConstants.crossReference, null));
            enrichedEntry.setDiseases(getDiseases(instance));
            enrichedEntry.setLiterature(setLiteratureReferences(instance));

            if (hasValue(instance, ReactomeJavaConstants.referenceEntity)) {
                GKInstance referenceEntity = (GKInstance) instance.getAttributeValue(ReactomeJavaConstants.referenceEntity);
                enrichedEntry.setExactType(referenceEntity.getSchemClass().getName());
            } else {
                enrichedEntry.setExactType(instance.getSchemClass().getName());
            }

            SchemaClass schemaClass = SchemaClass.getSchemaClass(enrichedEntry.getExactType());
            enrichedEntry.setType(schemaClass.name);
            enrichedEntry.setInstanceTypeExplanation(InstanceTypeExplanation.getExplanation(schemaClass));

            enrichedEntry.setIsDisease(enrichedEntry.getDiseases() != null);

            PathwayBrowserTreeGenerator pathwayBrowserTreeGenerator = new PathwayBrowserTreeGenerator();
            Set<Node> graph = pathwayBrowserTreeGenerator.generateGraphForGivenGkInstance(instance);

            enrichedEntry.setLocationsPathwayBrowser(graph);
            enrichedEntry.setAvailableSpecies(getAvailableSpecies(graph));

        } catch (Exception e) {
            logger.error("Error occurred when trying to set general Attributes", e);
            throw new EnricherException("Error occurred when trying to set general Attributes", e);
        }
    }

    /**
     * If the entry is available in more the one species,
     * we show all present species and then the user can choose
     * them in a dropdown list.
     * This method just prepare the species list where the HomoSapiens is the first
     * and the following species sorted without the HomoSapiens.
     */
    private static List<String> getAvailableSpecies(Set<Node> graph) {
        Set<String> availableSpecies = new TreeSet<>();
        for (Node n : graph) {
            availableSpecies.add(n.getSpecies());
        }

        final String DEFAULT_SPECIES = "Homo sapiens";
        List<String> newAvailableSpecies = new ArrayList<>();
        if (availableSpecies.contains(DEFAULT_SPECIES)) {
            newAvailableSpecies.add(DEFAULT_SPECIES);
            availableSpecies.remove(DEFAULT_SPECIES);
        }

        for (String species : availableSpecies) {
            newAvailableSpecies.add(species);
        }

        return newAvailableSpecies;
    }

    /**
     * Returns a list of literature for a given instance
     *
     * @param instance GkInstance
     * @return List of Literature Objects
     * @throws EnricherException
     */
    private static List<Literature> setLiteratureReferences(GKInstance instance) throws EnricherException {
        if (hasValues(instance, ReactomeJavaConstants.literatureReference)) {
            List<Literature> literatureList = new ArrayList<>();
            try {
                List<?> literatureInstanceList = instance.getAttributeValuesList(ReactomeJavaConstants.literatureReference);
                for (Object literatureObject : literatureInstanceList) {
                    GKInstance literatureInstance = (GKInstance) literatureObject;
                    Literature literature = new Literature();
                    literature.setTitle(getAttributeString(literatureInstance, ReactomeJavaConstants.title));
                    literature.setJournal(getAttributeString(literatureInstance, ReactomeJavaConstants.journal));
                    literature.setPubMedIdentifier(getAttributeString(literatureInstance, ReactomeJavaConstants.pubMedIdentifier));
                    literature.setYear(getAttributeInteger(literatureInstance, ReactomeJavaConstants.year));
                    if (literature.getPubMedIdentifier() != null) {
                        literature.setUrl(PUBMED_URL + literature.getPubMedIdentifier());
                    }
                    literatureList.add(literature);
                }
                return literatureList;
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                throw new EnricherException(e.getMessage(), e);
            }
        }
        return null;
    }

    /**
     * Returns a list of disease information related to an instance
     *
     * @param instance GkInstance
     * @return List of Disease Objects
     * @throws EnricherException
     */
    private static List<Disease> getDiseases(GKInstance instance) throws EnricherException {
        if (hasValues(instance, ReactomeJavaConstants.disease)) {
            try {
                List<Disease> diseases = new ArrayList<>();
                List<?> diseaseInstanceList = instance.getAttributeValuesList(ReactomeJavaConstants.disease);
                for (Object diseaseObject : diseaseInstanceList) {
                    Disease disease = new Disease();
                    GKInstance diseaseInstance = (GKInstance) diseaseObject;
                    disease.setName(getAttributeString(diseaseInstance, ReactomeJavaConstants.name));
                    disease.setSynonyms(getAttributes(diseaseInstance, ReactomeJavaConstants.synonym));
                    disease.setIdentifier(getAttributeString(diseaseInstance, ReactomeJavaConstants.identifier));
                    disease.setDatabase(getDatabase(diseaseInstance, disease.getIdentifier()));
                    diseases.add(disease);
                }
                return diseases;
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                throw new EnricherException(e.getMessage(), e);
            }
        }
        return null;
    }
}