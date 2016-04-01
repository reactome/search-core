package org.reactome.server.tools.search.solr;

import org.apache.solr.client.solrj.response.*;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.reactome.server.tools.interactors.util.InteractorConstant;
import org.reactome.server.tools.interactors.util.Toolbox;
import org.reactome.server.tools.search.domain.*;
import org.reactome.server.tools.search.exception.SolrSearcherException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Converts a Solr QueryResponse into Objects provided by Project Models
 *
 * @author Florian Korninger (fkorn@ebi.ac.uk)
 * @version 1.0
 */
@Component
public class SolrConverter {

    private static final Logger logger = LoggerFactory.getLogger(SolrConverter.class);

    @Autowired
    private SolrCore solrCore;

    private static final String DB_ID = "dbId";
    private static final String ST_ID = "stId";
    private static final String NAME = "name";

    private static final String SPECIES = "species";
    private static final String SPECIES_FACET = "species_facet";
    private static final String TYPES = "type_facet";
    private static final String KEYWORDS = "keywords_facet";
    private static final String COMPARTMENTS = "compartment_facet";

    private static final String SUMMATION = "summation";
    private static final String INFERRED_SUMMATION = "inferredSummation";
    private static final String REFERENCE_NAME = "referenceName";
    private static final String REFERENCE_IDENTIFIERS = "referenceIdentifiers";

    private static final String IS_DISEASE = "isDisease";
    private static final String EXACT_TYPE = "exactType";

    private static final String DATABASE_NAME = "databaseName";
    private static final String REFERENCE_URL = "referenceURL";

    private static final String REGULATOR = "regulator";
    private static final String REGULATED_ENTITY = "regulatedEntity";
    private static final String REGULATOR_ID = "regulatorId";
    private static final String REGULATED_ENTITY_ID = "regulatedEntityId";


    /**
     * Method for testing if a connection to Solr can be established
     * @return true if status is ok
     */
    public boolean ping() {
        return solrCore.ping();
    }

    /**
     * Method for autocompletion
     *
     * @param query String of the query parameter given
     * @return List(String) of Suggestions
     * @throws org.reactome.server.tools.search.exception.SolrSearcherException
     */
    public List<String> getAutocompleteSuggestions(String query) throws SolrSearcherException {
        List<String> aux = new LinkedList<>();
        if (query != null && !query.isEmpty()) {
            aux = suggestionHelper(solrCore.getAutocompleteSuggestions(query));
        }

        List<String> rtn = new LinkedList<>();
        if (aux != null) {
            for (String q : aux) {
                if (solrCore.existsQuery(q)) {
                    rtn.add(q);
                }
            }
        }
        return rtn;
    }

    /**
     * Method for spellcheck and suggestions
     *
     * @param query String of the query parameter given
     * @return List(String) of Suggestions
     * @throws org.reactome.server.tools.search.exception.SolrSearcherException
     */
    public List<String> getSpellcheckSuggestions(String query) throws SolrSearcherException {
        List<String> aux = new LinkedList<>();
        if (query != null && !query.isEmpty()) {
            aux = suggestionHelper(solrCore.getSpellcheckSuggestions(query));
        }

        List<String> rtn = new LinkedList<>();
        if (aux != null) {
            for (String q : aux) {
                if (solrCore.existsQuery(q)) {
                    rtn.add(q);
                }
            }
        }
        return rtn;
    }

    /**
     * Method gets all faceting information for the fields: species, types, compartments, keywords
     *
     * @return FacetMapping
     * @throws org.reactome.server.tools.search.exception.SolrSearcherException
     */
    public FacetMapping getFacetingInformation() throws SolrSearcherException {
        return getFacetMap(solrCore.getFacetingInformation());
    }

    /**
     * Method gets Faceting Info considering Filter of other possible FacetFields
     *
     * @param queryObject QueryObject (query, types, species, keywords, compartments)
     * @return FacetMapping
     * @throws org.reactome.server.tools.search.exception.SolrSearcherException
     */
    public FacetMapping getFacetingInformation(Query queryObject) throws SolrSearcherException {
        return getFacetMap(solrCore.getFacetingInformation(queryObject), queryObject);
    }

    public InteractorEntry getInteractionDetail(String accession) throws SolrSearcherException {
        if (accession != null && !accession.isEmpty()) {
            QueryResponse response = solrCore.intactDetail(accession);
            List<SolrDocument> solrDocuments = response.getResults();
            if (solrDocuments != null && !solrDocuments.isEmpty() && solrDocuments.get(0) != null) {
                return buildInteractorEntry(solrDocuments.get(0));
            }
        }

        logger.warn("No Entry found for this id: " + accession);
        return null;
    }


//    /**
//     * Converts single SolrResponseEntry to Object Entry (Model)
//     *
//     * @param id can be dbId of stId
//     * @return Entry Object
//     * @throws org.reactome.server.tools.search.exception.SolrSearcherException
//     */
//    public Entry getEntryById(String id) throws SolrSearcherException {
//        if (id != null && !id.isEmpty()) {
//            QueryResponse response = solrCore.searchById(id);
//            List<SolrDocument> solrDocuments = response.getResults();
//            if (solrDocuments != null && !solrDocuments.isEmpty() && solrDocuments.get(0) != null) {
//                return buildEntry(solrDocuments.get(0), null);
//            }
//        }
//        logger.warn("no Entry found for this id" + id);
//        return null;
//    }

    /**
     * Converts Solr QueryResponse to GroupedResult
     *
     * @param queryObject QueryObject (query, types, species, keywords, compartments, start, rows)
     * @return GroupedResponse
     * @throws org.reactome.server.tools.search.exception.SolrSearcherException
     */
    public GroupedResult getClusteredEntries(Query queryObject) throws SolrSearcherException {

        if (queryObject != null && queryObject.getQuery() != null && !queryObject.getQuery().isEmpty()) {
            QueryResponse queryResponse = solrCore.searchCluster(queryObject);
            if (queryResponse != null) {
                return parseClusteredResponse(queryResponse);
            }
        }
        return null;
    }

    /**
     * Converts Solr QueryResponse to GroupedResult
     *
     * @param queryObject QueryObject (query, types, species, keywords, compartments, start, rows)
     * @return GroupedResponse
     * @throws org.reactome.server.tools.search.exception.SolrSearcherException
     */
    public GroupedResult getEntries(Query queryObject) throws SolrSearcherException {

        if (queryObject != null && queryObject.getQuery() != null && !queryObject.getQuery().isEmpty()) {
            QueryResponse queryResponse = solrCore.search(queryObject);
            if (queryResponse != null) {
                return parseResponse(queryResponse);
            }
        }
        return null;
    }

    private GroupedResult parseResponse(QueryResponse queryResponse) {
        if (queryResponse != null) {
            List<SolrDocument> solrDocuments = queryResponse.getResults();
            Map<String, Map<String, List<String>>> highlighting = queryResponse.getHighlighting();
            List<Result> resultList = new ArrayList<>();
            List<Entry> entries = new ArrayList<>();

            for (SolrDocument solrDocument : solrDocuments) {
                Entry entry = buildEntry(solrDocument, highlighting);
                entries.add(entry);
            }
            resultList.add(new Result(entries, "Results", queryResponse.getResults().getNumFound(), entries.size()));
            return new GroupedResult(resultList, solrDocuments.size(), 1, (int) queryResponse.getResults().getNumFound());

        }
        return null;
    }

    /**
     * Helper Method to convert Solr faceting information to FacetMapping considering the selected Filtering Options
     *
     * @param response    Solr QueryResponse
     * @param queryObject considers species, types keywords compartments
     * @return FacetMapping
     */
    private FacetMapping getFacetMap(QueryResponse response, Query queryObject) {

        if (response != null && queryObject != null) {
            FacetMapping facetMapping = new FacetMapping();
            facetMapping.setTotalNumFount(response.getResults().getNumFound());
            facetMapping.setSpeciesFacet(getFacets(response.getFacetField(SPECIES_FACET), queryObject.getSpecies()));
            facetMapping.setTypeFacet(getFacets(response.getFacetField(TYPES), queryObject.getTypes()));
            facetMapping.setKeywordFacet(getFacets(response.getFacetField(KEYWORDS), queryObject.getKeywords()));
            facetMapping.setCompartmentFacet(getFacets(response.getFacetField(COMPARTMENTS), queryObject.getCompartment()));
            return facetMapping;
        }
        return null;
    }

    /**
     * Helper Method separates Faceting information into selected and available facets
     *
     * @param facetField    Solr FacetField (of a selected field)
     * @param selectedItems List of selected Strings of the queryObject
     * @return FacetList
     */
    private FacetList getFacets(FacetField facetField, List<String> selectedItems) {
        if (facetField != null) {
            List<FacetContainer> selected = new ArrayList<>();
            List<FacetContainer> available = new ArrayList<>();
            for (FacetField.Count field : facetField.getValues()) {
                if (selectedItems != null && selectedItems.contains(field.getName())) {
                    selected.add(new FacetContainer(field.getName(), field.getCount()));
                } else {
                    available.add(new FacetContainer(field.getName(), field.getCount()));
                }
            }
            return new FacetList(selected, available);
        }
        return null;
    }

    /**
     * Helper Method to convert Solr faceting information to FacetMapping
     *
     * @param queryResponse Solr QueryResponse
     * @return FacetMapping
     */
    private FacetMapping getFacetMap(QueryResponse queryResponse) {

        if (queryResponse != null && queryResponse.getFacetFields() != null && !queryResponse.getFacetFields().isEmpty()) {
            FacetMapping facetMapping = new FacetMapping();
            facetMapping.setTotalNumFount(queryResponse.getResults().getNumFound());
            List<FacetField> facetFields = queryResponse.getFacetFields();
            for (FacetField facetField : facetFields) {
                List<FacetContainer> available = new ArrayList<>();
                List<FacetField.Count> fields = facetField.getValues();
                for (FacetField.Count field : fields) {
                    available.add(new FacetContainer(field.getName(), field.getCount()));
                }
                if (facetField.getName().equals(SPECIES_FACET)) {
                    facetMapping.setSpeciesFacet(new FacetList(available));
                } else if (facetField.getName().equals(TYPES)) {
                    facetMapping.setTypeFacet(new FacetList(available));
                } else if (facetField.getName().equals(KEYWORDS)) {
                    facetMapping.setKeywordFacet(new FacetList(available));
                } else if (facetField.getName().equals(COMPARTMENTS)) {
                    facetMapping.setCompartmentFacet(new FacetList(available));
                }
            }
            return facetMapping;
        }
        return null;
    }

    private InteractorEntry buildInteractorEntry(SolrDocument solrDocument) {
        if (solrDocument != null && !solrDocument.isEmpty()) {
            InteractorEntry interactorEntry = new InteractorEntry();
            String accession = (String) solrDocument.getFieldValue(DB_ID);
            interactorEntry.setAccession(accession);
            interactorEntry.setName((String) solrDocument.getFieldValue(NAME));
            interactorEntry.setUrl((String) solrDocument.getFieldValue(REFERENCE_URL));
            List<Object> reactomeInteractorIds = (List<Object>) solrDocument.getFieldValues("reactomeInteractorIds");
            List<Object> reactomeInteractorNames = (List<Object>) solrDocument.getFieldValues("reactomeInteractorNames");
            List<Object> scores = (List<Object>) solrDocument.getFieldValues("scores");
            List<Object> interactionIds = (List<Object>) solrDocument.getFieldValues("interactionsIds");
            List<Object> accessions = (List<Object>) solrDocument.getFieldValues("interactorAccessions");

            List<Interactor> interactionList = new ArrayList<>(interactionIds.size());
            for (int i = 0; i < interactionIds.size(); i++) {
                Interactor interaction = new Interactor();

                String[] interactionsEvidencesArray = ((String) interactionIds.get(i)).split("#");
                interaction.setInteractionEvidences(Arrays.asList(interactionsEvidencesArray));
                interaction.setEvidencesURL(Toolbox.getEvidencesURL(interaction.getInteractionEvidences(), InteractorConstant.STATIC));
                interaction.setScore(Double.parseDouble((String) scores.get(i)));

                String accessionB = (String) accessions.get(i);
                interaction.setAccession((String) accessions.get(i));
                String[] reactomeNames = ((String) reactomeInteractorNames.get(i)).split("#");
                String[] reactomeIds = ((String) reactomeInteractorIds.get(i)).split("#");
                List<InteractorReactomeEntry> reactomeEntries = new ArrayList<>();
                for (int j = 0; j < reactomeIds.length; j++) {
                    reactomeEntries.add(new InteractorReactomeEntry(reactomeIds[j], reactomeNames[j]));
                }
                interaction.setInteractorReactomeEntries(reactomeEntries);
                interaction.setAccessionURL(Toolbox.getAccessionURL(accessionB, InteractorConstant.STATIC));
                interactionList.add(interaction);
            }

            interactorEntry.setInteractions(interactionList);

            return interactorEntry;
        }

        return null;
    }

    /**
     * Method for the construction of an entry from a SolrDocument, taking into account available highlighting information
     *
     * @param solrDocument Solr Document
     * @param highlighting SOLRJ Object Map<String,Map<String, List<String>>> used for highlighting
     * @return Entry
     */
    private Entry buildEntry(SolrDocument solrDocument, Map<String, Map<String, List<String>>> highlighting) {
        if (solrDocument != null && !solrDocument.isEmpty()) {
            Entry entry = new Entry();
            entry.setDbId((String) solrDocument.getFieldValue(DB_ID));

            if (solrDocument.containsKey(ST_ID)) {
                entry.setStId((String) solrDocument.getFieldValue(ST_ID));
                entry.setId((String) solrDocument.getFieldValue(ST_ID));
            } else {
                entry.setId((String) solrDocument.getFieldValue(DB_ID));
            }

            entry.setExactType((String) solrDocument.getFieldValue(EXACT_TYPE));
            entry.setIsDisease((Boolean) solrDocument.getFieldValue(IS_DISEASE));
            //Only the first species is taken into account
            Collection species = solrDocument.getFieldValues(SPECIES);
            if (species != null) {
                entry.setSpecies((String) species.toArray()[0]);
            }
            entry.setDatabaseName((String) solrDocument.getFieldValue(DATABASE_NAME));
            entry.setReferenceURL((String) solrDocument.getFieldValue(REFERENCE_URL));
            entry.setRegulatorId((String) solrDocument.getFieldValue(REGULATOR_ID));
            entry.setRegulatedEntityId((String) solrDocument.getFieldValue(REGULATED_ENTITY_ID));
            if (solrDocument.containsKey(COMPARTMENTS)) {
                Collection<Object> compartments = solrDocument.getFieldValues(COMPARTMENTS);
                List<String> list = new ArrayList<>();
                for (Object compartment : compartments) {
                    list.add(compartment.toString());
                }
                entry.setCompartmentNames(list);
            }

            if (highlighting != null && highlighting.containsKey(solrDocument.getFieldValue(DB_ID))) {
                setHighlighting(entry, solrDocument, highlighting.get(solrDocument.getFieldValue(DB_ID)));
            } else {
                entry.setName((String) solrDocument.getFieldValue(NAME));
                entry.setSummation((String) solrDocument.getFieldValue(SUMMATION));
                entry.setReferenceName((String) solrDocument.getFieldValue(REFERENCE_NAME));
                entry.setReferenceIdentifier(selectRightReferenceIdentifier(solrDocument));
                entry.setRegulator((String) solrDocument.getFieldValue(REGULATOR));
                entry.setRegulatedEntity((String) solrDocument.getFieldValue(REGULATED_ENTITY));
            }
            if (solrDocument.containsKey(INFERRED_SUMMATION)) {
                entry.setSummation((String) solrDocument.getFieldValue(INFERRED_SUMMATION));
            }

            return entry;
        }
        return null;
    }

    private String selectRightReferenceIdentifier(SolrDocument solrDocument) {
        Collection<Object> list = solrDocument.getFieldValues(REFERENCE_IDENTIFIERS);
        if (list != null) {
            String candidate = null;
            for (Object obj : list) {
                String str = (String) obj;
                candidate = candidate == null ? str : candidate;
                if (!str.contains(":")) {
                    return str;
                }
            }
            return candidate;
        }
        return null;
    }

    /**
     * Helper Method that sets Highlighted snippets if they are available
     *
     * @param entry        Entry Object
     * @param solrDocument Solr Document used when there are no highlighting Values
     * @param snippets     Map containing the Highlighted Strings
     */
    private void setHighlighting(Entry entry, SolrDocument solrDocument, Map<String, List<String>> snippets) {

        if (snippets.containsKey(NAME) && snippets.get(NAME) != null && !snippets.get(NAME).isEmpty()) {
            entry.setName(snippets.get(NAME).get(0));
        } else {
            entry.setName((String) solrDocument.getFieldValue(NAME));
        }
        if (snippets.containsKey(SUMMATION) && snippets.get(SUMMATION) != null && !snippets.get(SUMMATION).isEmpty()) {
            entry.setSummation(snippets.get(SUMMATION).get(0));
        } else {
            entry.setSummation((String) solrDocument.getFieldValue(SUMMATION));
        }
        if (snippets.containsKey(REFERENCE_NAME) && snippets.get(REFERENCE_NAME) != null && !snippets.get(REFERENCE_NAME).isEmpty()) {
            entry.setReferenceName(snippets.get(REFERENCE_NAME).get(0));
        } else {
            entry.setReferenceName((String) solrDocument.getFieldValue(REFERENCE_NAME));
        }
        entry.setReferenceIdentifier(selectRightHighlightingForReferenceIdentifiers(solrDocument, snippets));
        if (snippets.containsKey(REGULATOR) && snippets.get(REGULATOR) != null && !snippets.get(REGULATOR).isEmpty()) {
            entry.setRegulator(snippets.get(REGULATOR).get(0));
        } else {
            entry.setRegulator((String) solrDocument.getFieldValue(REGULATOR));
        }
        if (snippets.containsKey(REGULATED_ENTITY) && snippets.get(REGULATED_ENTITY) != null && !snippets.get(REGULATED_ENTITY).isEmpty()) {
            entry.setRegulatedEntity(snippets.get(REGULATED_ENTITY).get(0));
        } else {
            entry.setRegulatedEntity((String) solrDocument.getFieldValue(REGULATED_ENTITY));
        }
    }

    private String selectRightHighlightingForReferenceIdentifiers(SolrDocument solrDocument, Map<String, List<String>> snippets) {
        String candidate = null;
        if (snippets.containsKey(REFERENCE_IDENTIFIERS) && snippets.get(REFERENCE_IDENTIFIERS) != null && !snippets.get(REFERENCE_IDENTIFIERS).isEmpty()) {
            for (String snippet : snippets.get(REFERENCE_IDENTIFIERS)) {
                if (snippet.contains("highlighting")) {
                    return snippet;
                } else {
                    candidate = candidate == null ? snippet : candidate;
                }
            }
        } else {
            return selectRightReferenceIdentifier(solrDocument);
        }
        return candidate;
    }

    /**
     * Helper Method for converting a Solr Grouped Response into a Grouped Result (Model)
     *
     * @param queryResponse Solr QueryResponse
     * @return GroupedResult
     */
    private GroupedResult parseClusteredResponse(QueryResponse queryResponse) {
        if (queryResponse != null) {
            if (queryResponse.getGroupResponse() != null) {
                if (queryResponse.getGroupResponse().getValues() != null && !queryResponse.getGroupResponse().getValues().isEmpty()) {
                    if (queryResponse.getGroupResponse().getValues().get(0) != null) {
                        GroupCommand groupCommand = queryResponse.getGroupResponse().getValues().get(0);
                        List<Group> groups = groupCommand.getValues();
                        Map<String, Map<String, List<String>>> highlighting = queryResponse.getHighlighting();
                        int rowCounter = 0;
                        List<Result> resultList = new ArrayList<>();
                        for (Group group : groups) {
                            SolrDocumentList solrDocumentList = group.getResult();
                            List<Entry> entries = new ArrayList<>();
                            for (SolrDocument solrDocument : solrDocumentList) {
                                Entry entry = buildEntry(solrDocument, highlighting);
                                entries.add(entry);
                            }
                            resultList.add(new Result(entries, group.getGroupValue(), solrDocumentList.getNumFound(), entries.size()));
                            rowCounter += entries.size();
                        }
                        return new GroupedResult(resultList, rowCounter, groupCommand.getNGroups(), groupCommand.getMatches());
                    }
                }
            }
        }
        return null;
    }

    /**
     * Helper Function for converting SolrCollatedResults
     *
     * @param response Solr QueryResponse
     * @return List(String) of Suggestions
     */
    private List<String> suggestionHelper(QueryResponse response) {
        if (response != null && response.getSpellCheckResponse() != null) {
            List<String> list = new ArrayList<>();
            List<SpellCheckResponse.Collation> suggestions = response.getSpellCheckResponse().getCollatedResults();
            if (suggestions != null && !suggestions.isEmpty()) {
                for (SpellCheckResponse.Collation suggestion : suggestions) {
                    list.add(suggestion.getCollationQueryString());
                }
                return list;
            }
        }
        return null;
    }

//    public SolrCore getSolrCore() {
//        return solrCore;
//    }
//
//    public void setSolrCore(SolrCore solrCore) {
//        this.solrCore = solrCore;
//    }
}
