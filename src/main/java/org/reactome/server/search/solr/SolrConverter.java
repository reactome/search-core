package org.reactome.server.search.solr;

import org.apache.solr.client.solrj.response.*;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.reactome.server.search.domain.*;
import org.reactome.server.search.exception.SolrSearcherException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Converts a Solr QueryResponse into Objects provided by Project Models
 *
 * @author Florian Korninger (fkorn@ebi.ac.uk)
 * @author Guilherme Viter (gviteri@ebi.ac.uk)
 * @author Antonio Fabregat (fabregat@ebi.ac.uk)
 * @version 1.0
 */
@Component
public class SolrConverter {

    // REACTOME
    private static final String DB_ID = "dbId";
    private static final String ST_ID = "stId";
    private static final String NAME = "name";
    private static final String SPECIES = "species";
    private static final String SPECIES_FACET = "species_facet";
    private static final String TYPES = "type_facet";
    private static final String KEYWORDS = "keywords_facet";
    private static final String COMPARTMENT_FACET = "compartment_facet";
    private static final String ICON_CATEGORIES_FACET = "iconCategories_facet";
    private static final String SUMMATION = "summation";
    private static final String INFERRED_SUMMATION = "inferredSummation";
    private static final String REFERENCE_URL = "referenceURL";
    private static final String REFERENCE_NAME = "referenceName";
    private static final String REFERENCE_IDENTIFIERS = "referenceIdentifiers";
    private static final String IS_DISEASE = "isDisease";
    private static final String EXACT_TYPE = "exactType";
    private static final String DATABASE_NAME = "databaseName";
    private static final String REGULATOR = "regulator";
    private static final String REGULATOR_ID = "regulatorId";
    private static final String REGULATED_ENTITY = "regulatedEntity";
    private static final String REGULATED_ENTITY_ID = "regulatedEntityId";
    private static final String COMPARTMENT_NAME = "compartmentName";
    private static final String COMPARTMENT_ACCESSION = "compartmentAccession";
    private static final String FIREWORKS_SPECIES = "fireworksSpecies";
    private static final String OCCURRENCES = "occurrences";
    private static final String LLPS = "llps";
    private static final String AUTHORED_PATHWAYS = "authoredPathways";
    private static final String AUTHORED_REACTIONS = "authoredReactions";
    private static final String REVIEWED_PATHWAYS = "reviewedPathways";
    private static final String REVIEWED_REACTIONS = "reviewedReactions";
    private static final String ORCIDID = "orcidId";

    // TARGET
    private static final String TARGET_IDENTIFIER = "identifier";
    private static final String TARGET_ACCESSIONS = "accessions";
    private static final String TARGET_GENENAMES = "geneNames";
    private static final String TARGET_SYNONYMS = "synonyms";
    private static final String TARGET_RESOURCE = "resource";

    // ICONS
    private static final String ICON_NAME = "iconName";
    private static final String ICON_CATEGORIES = "iconCategories";
    private static final String ICON_CURATOR_NAME = "iconCuratorName";
    private static final String ICON_CURATOR_ORCIDID = "iconCuratorOrcidId";
    private static final String ICON_CURATOR_URL = "iconCuratorUrl";
    private static final String ICON_DESIGNER_NAME = "iconDesignerName";
    private static final String ICON_DESIGNER_URL = "iconDesignerUrl";
    private static final String ICON_DESIGNER_ORCIDID = "iconDesignerOrcidId";
    private static final String ICON_REFERENCES = "iconReferences";
    private static final String ICON_PHYSICAL_ENTITIES = "iconPhysicalEntities";
    private static final String ICON_EHLDS = "iconEhlds";

    @Autowired
    private SolrCore solrCore;

    /**
     * Method for testing if a connection to Solr can be established
     *
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
     */
    public List<String> getAutocompleteSuggestions(String query) throws SolrSearcherException {
        List<String> aux = new LinkedList<>();
        if (query != null && !query.isEmpty()) {
            aux = suggestionHelper(solrCore.getAutocompleteSuggestions(query));
        }

        return getSuggestions(aux);
    }

    /**
     * Method for spellcheck and suggestions
     *
     * @param query String of the query parameter given
     * @return List(String) of Suggestions
     */
    public List<String> getSpellcheckSuggestions(String query) throws SolrSearcherException {
        List<String> aux = new LinkedList<>();
        if (query != null && !query.isEmpty()) {
            aux = suggestionHelper(solrCore.getSpellcheckSuggestions(query));
        }

        return getSuggestions(aux);
    }

    private List<String> getSuggestions(List<String> aux) throws SolrSearcherException {
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
     */
    public FacetMapping getFacetingInformation() throws SolrSearcherException {
        return getFacetMap(solrCore.getFacetingInformation());
    }

    /**
     * Method gets Faceting Info considering Filter of other possible FacetFields
     *
     * @param queryObject QueryObject (query, types, species, keywords, compartments)
     * @return FacetMapping
     */
    public FacetMapping getFacetingInformation(Query queryObject) throws SolrSearcherException {
        return getFacetMap(solrCore.getFacetingInformation(queryObject), queryObject);
    }

    public FireworksResult getFireworksResult(Query queryObject) throws SolrSearcherException {
        QueryResponse response = solrCore.getFireworksResult(queryObject);
        if (response != null && queryObject != null) {
            List<SolrDocument> solrDocuments = response.getResults();
            List<Entry> entries = new ArrayList<>();
            for (SolrDocument solrDocument : solrDocuments) {
                Entry entry = buildEntry(solrDocument, null);
                if (solrDocument.containsKey(FIREWORKS_SPECIES)) {
                    Collection<Object> fireworksSpecies = solrDocument.getFieldValues(FIREWORKS_SPECIES);
                    entry.setFireworksSpecies(fireworksSpecies.stream().map(Object::toString).collect(Collectors.toList()));
                }

                entries.add(entry);
            }

            List<FacetContainer> facets = new ArrayList<>();
            for (FacetField facetField : response.getFacetFields()) {
                //only the TYPES facets is used in the handler, so no need to check the others
                if (!facetField.getName().equals(TYPES)) continue;
                facets.addAll(facetField.getValues().stream().map(field -> new FacetContainer(field.getName(), field.getCount())).collect(Collectors.toList()));
            }

            return new FireworksResult(entries, facets, response.getResults().getNumFound());
        }
        return null;
    }

    /**
     * Getting diagram occurrences, diagrams and subpathways multivalue fields have been added to the document.
     * Diagrams hold where the entity is present.
     * Subpathways hold a "isInDiagram:subpathways"
     * <p>
     * This is a two steps search:
     * - Submit term and diagram and retrieve a list of documents (getDiagramResult)
     * - Retrieve list of subpathways (getDiagramEncapsulatedResult)
     */
    public DiagramResult getDiagrams(Query queryObject) throws SolrSearcherException {
        QueryResponse response = solrCore.getDiagrams(queryObject);
        if (response != null && queryObject != null) {
            List<SolrDocument> solrDocuments = response.getResults();
            List<Entry> entries = new ArrayList<>();
            for (SolrDocument solrDocument : solrDocuments) {
                entries.add(buildEntry(solrDocument, null));
            }

            List<FacetContainer> facets = new ArrayList<>();
            if (response.getFacetFields() != null) {
                for (FacetField facetField : response.getFacetFields()) {
                    if (facetField.getName().equals(TYPES)) {
                        facets.addAll(facetField.getValues().stream().map(field -> new FacetContainer(field.getName(), field.getCount())).collect(Collectors.toList()));
                    }
                }
            }
            return new DiagramResult(entries, facets, response.getResults().getNumFound());
        }
        return null;
    }

    /**
     * This is stored in the subpathways multivalue field having diagram:isInDiagram:[list of subpathways]
     *
     * @param queryObject - has the stId of the element we are search and the diagram to filter
     */
    public DiagramOccurrencesResult getDiagramOccurrencesResult(Query queryObject) throws SolrSearcherException {
        DiagramOccurrencesResult ret = null;
        QueryResponse response = solrCore.getDiagramOccurrences(queryObject);
        if (response != null && queryObject != null) {
            String searchingFilter = queryObject.getFilterQuery();
            List<SolrDocument> solrDocuments = response.getResults();
            for (SolrDocument solrDocument : solrDocuments) {
                if (solrDocument.containsKey(OCCURRENCES)) {
                    List<String> rawOccurrences = solrDocument.getFieldValues(OCCURRENCES).stream().map(Object::toString).collect(Collectors.toList());
                    for (String rawOccurrence : rawOccurrences) {
                        if (rawOccurrence.startsWith(searchingFilter)) {
                            // Diagram:Bool(IsInDiagram):CSV of occurrences:CSV of Interacts With
                            String[] line = rawOccurrence.split(":");
                            List<String> occurrences = line[2].equals("#") ? null : Stream.of(line[2].split(",")).collect(Collectors.toList());
                            List<String> interactsWith = line[3].equals("#") ? null : Stream.of(line[3].split(",")).collect(Collectors.toList());
                            ret = new DiagramOccurrencesResult(Boolean.valueOf(line[1]), occurrences, interactsWith);
                        }
                    }
                }
            }
        }
        return ret;
    }

    /**
     * This is stored in the subpathways multivalue field having diagram:isInDiagram:[list of subpathways]
     *
     * @param queryObject - has the term we are searching to flag the corresponding element and the diagram to filter
     */
    public List<DiagramOccurrencesResult> getDiagramFlagging(Query queryObject) throws SolrSearcherException {
        List<DiagramOccurrencesResult> rtn = new ArrayList<>();
        QueryResponse response = solrCore.getDiagramFlagging(queryObject);
        if (response != null && queryObject != null) {
            String targetedDiagram = queryObject.getFilterQuery();
            List<SolrDocument> solrDocuments = response.getResults();
            for (SolrDocument solrDocument : solrDocuments) {
                if (solrDocument.containsKey(OCCURRENCES)) {
                    List<String> rawOccurrences = solrDocument.getFieldValues(OCCURRENCES).stream().map(Object::toString).collect(Collectors.toList());
                    for (String rawOccurrence : rawOccurrences) {
                        if (rawOccurrence.startsWith(targetedDiagram)) {
                            // Diagram:Bool(IsInDiagram):CSV of occurrences:CSV of Interacts With
                            String[] line = rawOccurrence.split(":");
                            List<String> occurrences = line[2].equals("#") ? null : Stream.of(line[2].split(",")).collect(Collectors.toList());
                            List<String> interactsWith = line[3].equals("#") ? null : Stream.of(line[3].split(",")).collect(Collectors.toList());
                            Boolean isInDiagram = Boolean.valueOf(line[1]);
                            String stId = isInDiagram ? (String) solrDocument.getFieldValue(ST_ID) : null;
                            rtn.add(new DiagramOccurrencesResult(stId, occurrences, interactsWith));
                        }
                    }
                }
            }
        }
        return rtn;
    }

    /**
     * Converts Solr QueryResponse to GroupedResult
     *
     * @param queryObject QueryObject (query, types, species, keywords, compartments, start, rows)
     * @return GroupedResponse
     */
    public GroupedResult getGroupedEntries(Query queryObject) throws SolrSearcherException {
        if (queryObject != null && queryObject.getQuery() != null && !queryObject.getQuery().isEmpty()) {
            QueryResponse queryResponse = solrCore.groupedSearch(queryObject);
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
            facetMapping.setCompartmentFacet(getFacets(response.getFacetField(COMPARTMENT_FACET), queryObject.getCompartments()));
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
                List<FacetField.Count> fields = facetField.getValues();
                List<FacetContainer> available = fields.stream().map(field -> new FacetContainer(field.getName(), field.getCount())).collect(Collectors.toList());
                switch (facetField.getName()) {
                    case SPECIES_FACET:
                        facetMapping.setSpeciesFacet(new FacetList(available));
                        break;
                    case TYPES:
                        facetMapping.setTypeFacet(new FacetList(available));
                        break;
                    case KEYWORDS:
                        facetMapping.setKeywordFacet(new FacetList(available));
                        break;
                    case COMPARTMENT_FACET:
                        facetMapping.setCompartmentFacet(new FacetList(available));
                        break;
                    case ICON_CATEGORIES_FACET:
                        facetMapping.setIconCategoriesFacet(new FacetList(available));
                        break;
                }
            }
            return facetMapping;
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
            entry.setStId((String) solrDocument.getFieldValue(ST_ID));
            entry.setId((String)
                    (solrDocument.containsKey(ST_ID) ? solrDocument.getFieldValue(ST_ID) : solrDocument.getFieldValue(DB_ID)));

            entry.setExactType((String) solrDocument.getFieldValue(EXACT_TYPE));
            entry.setIsDisease((Boolean) solrDocument.getFieldValue(IS_DISEASE));
            entry.setDatabaseName((String) solrDocument.getFieldValue(DATABASE_NAME));
            entry.setReferenceURL((String) solrDocument.getFieldValue(REFERENCE_URL));
            entry.setRegulatorId((String) solrDocument.getFieldValue(REGULATOR_ID));
            entry.setRegulatedEntityId((String) solrDocument.getFieldValue(REGULATED_ENTITY_ID));
            entry.setSummation((String) solrDocument.getFieldValue(INFERRED_SUMMATION));
            entry.setAuthoredPathways((String) solrDocument.getFieldValue(AUTHORED_PATHWAYS));
            entry.setAuthoredReactions((String) solrDocument.getFieldValue(AUTHORED_REACTIONS));
            entry.setReviewedPathways((String) solrDocument.getFieldValue(REVIEWED_PATHWAYS));
            entry.setReviewedReactions((String) solrDocument.getFieldValue(REVIEWED_REACTIONS));
            entry.setOrcidId((String) solrDocument.getFieldValue(ORCIDID));

            //Only the first species is taken into account
            entry.setSpecies(getStringListField(solrDocument, SPECIES));
            entry.setCompartmentNames(getStringListField(solrDocument, COMPARTMENT_NAME));
            entry.setCompartmentAccession(getStringListField(solrDocument, COMPARTMENT_ACCESSION));


            if (highlighting != null && highlighting.containsKey(entry.getDbId())) {
                setHighlighting(entry, solrDocument, highlighting.get(entry.getDbId()));
            } else {
                entry.setName((String) solrDocument.getFieldValue(NAME));
                entry.setSummation((String) solrDocument.getFieldValue(SUMMATION));
                entry.setReferenceName((String) solrDocument.getFieldValue(REFERENCE_NAME));
                entry.setRegulator((String) solrDocument.getFieldValue(REGULATOR));
                entry.setRegulatedEntity((String) solrDocument.getFieldValue(REGULATED_ENTITY));
                entry.setReferenceIdentifier(selectRightReferenceIdentifier(solrDocument));
            }

            buildIconEntry(solrDocument, entry);

            return entry;
        }
        return null;
    }

    private void buildIconEntry(SolrDocument solrDocument, Entry entry) {
        // Icon Name stores the plain name. After search the name itself might have the highlighting.
        entry.setIconName((String) solrDocument.getFieldValue(ICON_NAME));
        entry.setIconCuratorName((String) solrDocument.getFieldValue(ICON_CURATOR_NAME));
        entry.setIconCuratorOrcidId((String) solrDocument.getFieldValue(ICON_CURATOR_ORCIDID));
        entry.setIconCuratorUrl((String) solrDocument.getFieldValue(ICON_CURATOR_URL));
        entry.setIconDesignerName((String) solrDocument.getFieldValue(ICON_DESIGNER_NAME));
        entry.setIconDesignerUrl((String) solrDocument.getFieldValue(ICON_DESIGNER_URL));
        entry.setIconDesignerOrcidId((String) solrDocument.getFieldValue(ICON_DESIGNER_ORCIDID));

        entry.setIconCategories(getStringListField(solrDocument, ICON_CATEGORIES));
        entry.setIconReferences(getStringListField(solrDocument, ICON_REFERENCES));
        entry.setIconEhlds(getStringListField(solrDocument, ICON_EHLDS));
        entry.setIconPhysicalEntities(
                getStringListField(solrDocument, ICON_PHYSICAL_ENTITIES)
                        .stream()
                        .map(iconPE -> {
                            String[] iconPEs = iconPE.split("#");
                            return new IconPhysicalEntity(iconPEs[0], iconPEs[1], iconPEs[2], iconPEs[3]);
                        }).collect(Collectors.toCollection(TreeSet::new))
        );
    }

    private List<String> getStringListField(SolrDocument document, String field) {
        Collection<Object> objects = document.getFieldValues(field);
        if (objects != null && !objects.isEmpty()) {
            return objects.stream().map(Objects::toString).collect(Collectors.toList());
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
        entry.setReferenceIdentifier(selectRightHighlightingForReferenceIdentifiers(solrDocument, snippets));

        snippetHighlight(entry, Entry::setName, NAME, snippets);
        snippetHighlight(entry, Entry::setSummation, SUMMATION, snippets);
        snippetHighlight(entry, Entry::setReferenceName, REFERENCE_NAME, snippets);
        snippetHighlight(entry, Entry::setRegulator, REGULATOR, snippets);
        snippetHighlight(entry, Entry::setRegulatedEntity, REGULATED_ENTITY, snippets);
    }

    private void snippetHighlight(Entry entry, BiConsumer<Entry, String> fieldSetter, String field, Map<String, List<String>> snippets) {
        List<String> snippet = snippets.get(field);
        if (snippet != null && !snippet.isEmpty()) {
            fieldSetter.accept(entry, snippet.get(0));
        }
    }

    private String selectRightHighlightingForReferenceIdentifiers(SolrDocument solrDocument, Map<String, List<String>> snippets) {
        List<String> identifierSnippets = snippets.get(REFERENCE_IDENTIFIERS);
        if (identifierSnippets != null && !identifierSnippets.isEmpty()) {
            for (String snippet : identifierSnippets) {
                if (snippet.contains("highlighting")) {
                    return snippet;
                }
            }
            return identifierSnippets.get(0);
        } else {
            return selectRightReferenceIdentifier(solrDocument);
        }
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
                            if (!entries.isEmpty()) {
                                // An empty list was added to the GroupedResult then the web results become odd with blank blocks
                                resultList.add(new Result(entries, group.getGroupValue(), solrDocumentList.getNumFound(), entries.size()));
                                rowCounter += entries.size();
                            }
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
            List<SpellCheckResponse.Collation> suggestions = response.getSpellCheckResponse().getCollatedResults();
            if (suggestions != null && !suggestions.isEmpty()) {
                return suggestions.stream().map(SpellCheckResponse.Collation::getCollationQueryString).collect(Collectors.toList());
            }
        }
        return null;
    }

    public FireworksOccurrencesResult fireworksFlagging(Query queryObject) throws SolrSearcherException {
        FireworksOccurrencesResult rtn = new FireworksOccurrencesResult();
        QueryResponse response = solrCore.fireworksFlagging(queryObject);
        if (response != null && queryObject != null) {
            List<SolrDocument> solrDocuments = response.getResults();
            for (SolrDocument solrDocument : solrDocuments) {
                if (solrDocument.containsKey(LLPS)) {
                    rtn.addLlps(solrDocument.getFieldValues(LLPS).stream().map(Object::toString).collect(Collectors.toList()));
                }
                if (solrDocument.containsKey(OCCURRENCES)) {
                    List<String> rawOccurrences = solrDocument.getFieldValues(OCCURRENCES).stream().map(Object::toString).collect(Collectors.toList());
                    for (String rawOccurrence : rawOccurrences) {
                        // Diagram:Bool(IsInDiagram):CSV of occurrences:CSV of Interacts With
                        String[] line = rawOccurrence.split(":");
                        String pathwayStId = line[0];
                        boolean interacts = !line[3].equals("#");
                        // if there is(are) interactor(s), then get the diagram (first value) so the Fireworks can flag them.
                        if (interacts) {
                            // get the diagram and add it
                            rtn.addInteractsWith(pathwayStId);
                        }
                    }
                }
            }
        }

        // if it is in llps, remove from interacts with
        if (rtn.getLlps() != null && rtn.getInteractsWith() != null)
            rtn.getLlps().forEach(s -> rtn.getInteractsWith().remove(s));

        return rtn;
    }

    /**
     * Query "Target" Solr Core for potential targets in our scope of annotation
     *
     * @return TargetResult - term:isTarget
     */
    public Set<TargetResult> getTargets(Query queryObject) {
        Set<TargetResult> ret = new HashSet<>();
        QueryResponse response = solrCore.getTargets(queryObject);
        if (response != null) {
            List<SolrDocument> solrDocuments = response.getResults();
            if (solrDocuments != null && !solrDocuments.isEmpty()) {
                String[] terms = queryObject.getQuery().split("\\s+");
                for (String singleTerm : terms) {
                    boolean isTarget = false;
                    String resource = null;
                    for (SolrDocument solrDocument : solrDocuments) {
                        String identifier = (String) solrDocument.getFieldValue(TARGET_IDENTIFIER);
                        resource = (String) solrDocument.getFieldValue(TARGET_RESOURCE);
                        List<String> accessions = solrDocument.getFieldValues(TARGET_ACCESSIONS).stream().map(Object::toString).collect(Collectors.toList());
                        List<String> geneNames = null;
                        if (solrDocument.containsKey(TARGET_GENENAMES))
                            geneNames = solrDocument.getFieldValues(TARGET_GENENAMES).stream().map(Object::toString).collect(Collectors.toList());
                        List<String> synonyms = null;
                        if (solrDocument.containsKey(TARGET_SYNONYMS))
                            synonyms = solrDocument.getFieldValues(TARGET_SYNONYMS).stream().map(Object::toString).collect(Collectors.toList());

                        if (identifier.equalsIgnoreCase(singleTerm) ||
                                accessions.stream().anyMatch(singleTerm::equalsIgnoreCase) ||
                                (geneNames != null && geneNames.stream().anyMatch(singleTerm::equalsIgnoreCase)) ||
                                (synonyms != null && synonyms.stream().anyMatch(singleTerm::equalsIgnoreCase))) {
                            isTarget = true;
                        }
                    }
                    ret.add(new TargetResult(singleTerm, resource, isTarget));
                }
            }
        }
        return ret;
    }

    public FacetMapping getIconFacetingInformation() throws SolrSearcherException {
        return getFacetMap(solrCore.getIconFacetingInformation());
    }

    public Result getIconsResult(Query queryObject) throws SolrSearcherException {
        if (queryObject != null && queryObject.getQuery() != null && !queryObject.getQuery().isEmpty()) {
            QueryResponse queryResponse = solrCore.getIconsResult(queryObject);
            if (queryResponse != null) {
                return parseResponse(queryResponse).getResults().get(0);
            }
        }
        return null;
    }

    public Entry getIcon(Query queryObject) throws SolrSearcherException {
        if (queryObject != null && queryObject.getQuery() != null && !queryObject.getQuery().isEmpty()) {
            QueryResponse queryResponse = solrCore.getIcon(queryObject);
            if (queryResponse != null) {
                if (queryResponse.getResults().getNumFound() > 0L) {
                    return parseResponse(queryResponse).getResults().get(0).getEntries().get(0);
                }
            }
        }
        return null;
    }
}
