package org.reactome.server.search.solr;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.*;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.reactome.server.search.domain.*;
import org.reactome.server.search.exception.SolrSearcherException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.reactome.server.search.solr.SolrConverter.Field.*;

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

    public enum Field {
        // REACTOME
        DB_ID("dbId"),
        ST_ID("stId"),
        PHYSICAL_ENTITIES_DB_ID("physicalEntitiesDbId"),
        NAME("name"),
        SPECIES("species"),
        SPECIES_FACET("species_facet"),
        TYPES("type_facet"),
        KEYWORDS("keywords_facet"),
        COMPARTMENT_FACET("compartment_facet"),
        ICON_CATEGORIES_FACET("iconCategories_facet"),
        SUMMATION("summation"),
        INFERRED_SUMMATION("inferredSummation"),
        REFERENCE_URL("referenceURL"),
        REFERENCE_NAME("referenceName"),
        REFERENCE_IDENTIFIERS("referenceIdentifiers"),
        IS_DISEASE("isDisease"),
        HAS_REFERENCE_ENTITY("hasReferenceEntity"),
        HAS_EHLD("hasEHLD"),
        EXACT_TYPE("exactType"),
        DATABASE_NAME("databaseName"),
        REGULATOR("regulator"),
        REGULATOR_ID("regulatorId"),
        REGULATED_ENTITY("regulatedEntity"),
        REGULATED_ENTITY_ID("regulatedEntityId"),
        COMPARTMENT_NAME("compartmentName"),
        COMPARTMENT_ACCESSION("compartmentAccession"),
        FIREWORKS_SPECIES("fireworksSpecies"),
        OCCURRENCES("occurrences"),
        OCCURRENCES_INTERACTOR("occurrencesWithInteractor"),
        DIAGRAMS("diagrams"),
        DIAGRAMS_INTERACTOR("diagramsWithInteractor"),
        LLPS("llps"),
        AUTHORED_PATHWAYS("authoredPathways"),
        AUTHORED_REACTIONS("authoredReactions"),
        REVIEWED_PATHWAYS("reviewedPathways"),
        REVIEWED_REACTIONS("reviewedReactions"),
        ORCIDID("orcidId"),

        // TARGET
        TARGET_IDENTIFIER("identifier"),
        TARGET_ACCESSIONS("accessions"),
        TARGET_GENENAMES("geneNames"),
        TARGET_SYNONYMS("synonyms"),
        TARGET_RESOURCE("resource"),

        // ICONS
        ICON_NAME("iconName"),
        ICON_CATEGORIES("iconCategories"),
        ICON_CURATOR_NAME("iconCuratorName"),
        ICON_CURATOR_ORCIDID("iconCuratorOrcidId"),
        ICON_CURATOR_URL("iconCuratorUrl"),
        ICON_DESIGNER_NAME("iconDesignerName"),
        ICON_DESIGNER_URL("iconDesignerUrl"),
        ICON_DESIGNER_ORCIDID("iconDesignerOrcidId"),
        ICON_REFERENCES("iconReferences"),
        ICON_PHYSICAL_ENTITIES("iconPhysicalEntities"),
        ICON_EHLDS("iconEhlds");

        public final String name;

        Field(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public static Field valueOfName(String name) {
            return nameToField.get(name);
        }

        private static final Map<String, Field> nameToField = Arrays.stream(Field.values()).collect(Collectors.toMap(f -> f.name, f -> f));
    }

    private final SolrCore solrCore;

    public SolrConverter(@Autowired SolrCore solrCore) {
        this.solrCore = solrCore;
    }

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
                if (solrDocument.containsKey(FIREWORKS_SPECIES.name)) {
                    Collection<Object> fireworksSpecies = solrDocument.getFieldValues(FIREWORKS_SPECIES.name);
                    entry.setFireworksSpecies(fireworksSpecies.stream().map(Object::toString).collect(Collectors.toList()));
                }

                entries.add(entry);
            }

            List<FacetContainer> facets = new ArrayList<>();
            for (FacetField facetField : response.getFacetFields()) {
                //only the TYPES facets is used in the handler, so no need to check the others
                if (!facetField.getName().equals(Field.TYPES.name)) continue;
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
                    if (facetField.getName().equals(TYPES.name)) {
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
        QueryResponse response = solrCore.getDiagramOccurrences(queryObject);
        if (response != null && queryObject != null) {
            String searchingFilter = queryObject.getFilterQuery();
            List<SolrDocument> solrDocuments = response.getResults();
            for (SolrDocument solrDocument : solrDocuments) {
                if (solrDocument.containsKey(queryObject.getOccurrencesFieldName())) {
                    List<String> rawOccurrences = solrDocument.getFieldValues(queryObject.getOccurrencesFieldName()).stream().map(Object::toString).collect(Collectors.toList());
                    for (String rawOccurrence : rawOccurrences) {
                        if (rawOccurrence.startsWith(searchingFilter)) {
                            return extractRawOccurrence(rawOccurrence, Optional.empty());
                        }
                    }
                }
            }
        }
        return null;
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
                if (solrDocument.containsKey(queryObject.getOccurrencesFieldName())) {
                    List<String> rawOccurrences = solrDocument.getFieldValues(queryObject.getOccurrencesFieldName()).stream().map(Object::toString).collect(Collectors.toList());
                    for (String rawOccurrence : rawOccurrences) {
                        if (rawOccurrence.startsWith(targetedDiagram)) {
                            rtn.add(extractRawOccurrence(rawOccurrence, Optional.of(solrDocument)));
                        }
                    }
                }
            }
        }
        return rtn;
    }

    @NonNull
    public List<Entry> getContainingPathwaysOf(Long dbId, Boolean includeInteractors, Boolean directlyInDiagram, @Nullable List<Field> fields) throws SolrSearcherException {
        if (dbId == null) return List.of();
        String occurrenceField = includeInteractors ? OCCURRENCES_INTERACTOR.name : OCCURRENCES.name;
        SolrDocument entityDocument = this.solrCore.retrieveFromDbId(dbId, List.of(occurrenceField));
        if (entityDocument == null || !entityDocument.containsKey(occurrenceField)) return List.of();
        Stream<DiagramOccurrencesResult> occurrencesStream = entityDocument.getFieldValues(occurrenceField).stream()
                .map(Object::toString)
                .map(raw -> extractRawOccurrence(raw, Optional.empty()));

        if (directlyInDiagram) occurrencesStream = occurrencesStream.filter(DiagramOccurrencesResult::getInDiagram);
        List<String> occurrencesPathwayStIds = occurrencesStream.map(DiagramOccurrencesResult::getDiagramEntity).collect(Collectors.toList());
        return batchRetrieveFromStIds(occurrencesPathwayStIds, fields);
    }

    @NonNull
    public List<Entry> getPhysicalEntitiesOfReference(String stId, @Nullable List<Field> fields) throws SolrSearcherException {
        if (stId == null) return List.of();
        SolrDocument entityDocument = this.solrCore.retrieveFromStId(stId, List.of(PHYSICAL_ENTITIES_DB_ID.name));
        if (entityDocument == null || !entityDocument.containsKey(PHYSICAL_ENTITIES_DB_ID.name)) return List.of();

        if (fields != null && fields.size() == 1 && fields.get(0) == DB_ID) // Special case when we only want the DB ID
            return entityDocument.getFieldValues(PHYSICAL_ENTITIES_DB_ID.name).stream().map(dbId -> {
                Entry entry = new Entry();
                entry.setDbId(dbId.toString());
                return entry;
            }).collect(Collectors.toList());

        List<Long> physicalEntitiesDbId = entityDocument.getFieldValues(PHYSICAL_ENTITIES_DB_ID.name).stream()
                .map(dbId -> Long.parseLong(dbId.toString()))
                .collect(Collectors.toList());
        return batchRetrieveFromDbIds(physicalEntitiesDbId, fields);
    }

    @NonNull
    public List<Entry> batchRetrieveFromStIds(List<String> stIds, @Nullable List<Field> fields) throws SolrSearcherException {
        if (stIds == null) return List.of();
        if (fields == null) fields = List.of();
        List<String> internalFields = fields.stream().map(Field::getName).collect(Collectors.toList());
        QueryResponse response = solrCore.batchRetrieveFromStableIds(stIds, internalFields);
        if (response != null) {
            return response.getResults().stream().map(solrDocument -> buildEntry(solrDocument, null)).collect(Collectors.toList());
        }
        return List.of();
    }

    @NonNull
    public List<Entry> batchRetrieveFromDbIds(List<Long> dbIds, @Nullable List<Field> fields) throws SolrSearcherException {
        if (dbIds == null) return List.of();
        if (fields == null) fields = List.of();
        SolrDocumentList response = solrCore.batchRetrieveFromDbIds(dbIds, fields.stream().map(Field::name).collect(Collectors.toList()));
        if (response != null) {
            return response.stream().map(solrDocument -> buildEntry(solrDocument, null)).collect(Collectors.toList());
        }
        return List.of();
    }

    @Nullable
    public Entry retrieveFromDbId(@Nullable Long dbId, @Nullable List<Field> fields) throws SolrSearcherException {
        if (dbId == null) return null;
        if (fields == null) fields = List.of();
        return buildEntry(this.solrCore.retrieveFromDbId(dbId, fields.stream().map(Field::name).collect(Collectors.toList())), null);
    }


    /**
     * @param rawOccurrence String with the following format "Diagram:Bool(IsInDiagram):CSV of occurrences:CSV of Interacts With"
     * @param document      if null, will not use StId
     * @return DiagramOccurrencesResult
     */
    private static DiagramOccurrencesResult extractRawOccurrence(String rawOccurrence, Optional<SolrDocument> document) {
        String[] line = rawOccurrence.split(":");
        String diagramStId = line[0];
        List<String> occurrences = line[2].equals("#") ? null : Stream.of(line[2].split(",")).collect(Collectors.toList());
        List<String> interactsWith = line[3].equals("#") ? null : Stream.of(line[3].split(",")).collect(Collectors.toList());
        Boolean isInDiagram = Boolean.valueOf(line[1]);

        if (document.isEmpty())
            return new DiagramOccurrencesResult(diagramStId, isInDiagram, occurrences, interactsWith);
        String stId = isInDiagram ? (String) document.get().getFieldValue(ST_ID.name) : null;
        return new DiagramOccurrencesResult(stId, occurrences, interactsWith);
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
                return parseResponse(queryResponse, false);
            }
        }
        return null;
    }

    private GroupedResult parseResponse(QueryResponse queryResponse, boolean ignoreHighlight) {
        if (queryResponse != null) {
            List<SolrDocument> solrDocuments = queryResponse.getResults();
            Map<String, Map<String, List<String>>> highlighting = !ignoreHighlight ? queryResponse.getHighlighting() : new HashMap<>();
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
            facetMapping.setSpeciesFacet(getFacets(response.getFacetField(SPECIES_FACET.name), queryObject.getSpecies()));
            facetMapping.setTypeFacet(getFacets(response.getFacetField(TYPES.name), queryObject.getTypes()));
            facetMapping.setKeywordFacet(getFacets(response.getFacetField(KEYWORDS.name), queryObject.getKeywords()));
            facetMapping.setCompartmentFacet(getFacets(response.getFacetField(COMPARTMENT_FACET.name), queryObject.getCompartments()));
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
                switch (Field.valueOfName(facetField.getName())) {
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

            entry.setDbId((String) solrDocument.getFieldValue(DB_ID.name));
            entry.setStId((String) solrDocument.getFieldValue(ST_ID.name));
            entry.setId((String)
                    (solrDocument.containsKey(ST_ID.name) ? solrDocument.getFieldValue(ST_ID.name) : solrDocument.getFieldValue(DB_ID.name)));

            entry.setExactType((String) solrDocument.getFieldValue(EXACT_TYPE.name));
            entry.setIsDisease((Boolean) solrDocument.getFieldValue(IS_DISEASE.name));
            entry.setHasReferenceEntity((Boolean) solrDocument.getFieldValue(HAS_REFERENCE_ENTITY.name));
            entry.setHasEHLD((Boolean) solrDocument.getFieldValue(HAS_EHLD.name));
            entry.setDatabaseName((String) solrDocument.getFieldValue(DATABASE_NAME.name));
            entry.setReferenceURL((String) solrDocument.getFieldValue(REFERENCE_URL.name));
            entry.setRegulatorId((String) solrDocument.getFieldValue(REGULATOR_ID.name));
            entry.setRegulatedEntityId((String) solrDocument.getFieldValue(REGULATED_ENTITY_ID.name));
            entry.setSummation((String) solrDocument.getFieldValue(INFERRED_SUMMATION.name));
            entry.setAuthoredPathways((String) solrDocument.getFieldValue(AUTHORED_PATHWAYS.name));
            entry.setAuthoredReactions((String) solrDocument.getFieldValue(AUTHORED_REACTIONS.name));
            entry.setReviewedPathways((String) solrDocument.getFieldValue(REVIEWED_PATHWAYS.name));
            entry.setReviewedReactions((String) solrDocument.getFieldValue(REVIEWED_REACTIONS.name));
            entry.setOrcidId((String) solrDocument.getFieldValue(ORCIDID.name));

            //Only the first species is taken into account
            entry.setSpecies(getStringListField(solrDocument, SPECIES.name));
            entry.setCompartmentNames(getStringListField(solrDocument, COMPARTMENT_NAME.name));
            entry.setCompartmentAccession(getStringListField(solrDocument, COMPARTMENT_ACCESSION.name));


            if (highlighting != null && highlighting.containsKey(entry.getDbId())) {
                setHighlighting(entry, solrDocument, highlighting.get(entry.getDbId()));
            } else {
                entry.setName((String) solrDocument.getFieldValue(NAME.name));
                entry.setSummation((String) solrDocument.getFieldValue(SUMMATION.name));
                entry.setReferenceName((String) solrDocument.getFieldValue(REFERENCE_NAME.name));
                entry.setRegulator((String) solrDocument.getFieldValue(REGULATOR.name));
                entry.setRegulatedEntity((String) solrDocument.getFieldValue(REGULATED_ENTITY.name));
                entry.setReferenceIdentifier(selectRightReferenceIdentifier(solrDocument));
            }

            buildIconEntry(solrDocument, entry);

            return entry;
        }
        return null;
    }

    private void buildIconEntry(SolrDocument solrDocument, Entry entry) {
        // Icon Name stores the plain name. After search the name itself might have the highlighting.
        entry.setIconName((String) solrDocument.getFieldValue(ICON_NAME.name));
        entry.setIconCuratorName((String) solrDocument.getFieldValue(ICON_CURATOR_NAME.name));
        entry.setIconCuratorOrcidId((String) solrDocument.getFieldValue(ICON_CURATOR_ORCIDID.name));
        entry.setIconCuratorUrl((String) solrDocument.getFieldValue(ICON_CURATOR_URL.name));
        entry.setIconDesignerName((String) solrDocument.getFieldValue(ICON_DESIGNER_NAME.name));
        entry.setIconDesignerUrl((String) solrDocument.getFieldValue(ICON_DESIGNER_URL.name));
        entry.setIconDesignerOrcidId((String) solrDocument.getFieldValue(ICON_DESIGNER_ORCIDID.name));

        entry.setIconCategories(getStringListField(solrDocument, ICON_CATEGORIES.name));
        entry.setIconReferences(getStringListField(solrDocument, ICON_REFERENCES.name));
        entry.setIconEhlds(getStringListField(solrDocument, ICON_EHLDS.name));
        entry.setIconPhysicalEntities(
                getStringListField(solrDocument, ICON_PHYSICAL_ENTITIES.name)
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
        return Collections.emptyList();
    }

    private String selectRightReferenceIdentifier(SolrDocument solrDocument) {
        Collection<Object> list = solrDocument.getFieldValues(REFERENCE_IDENTIFIERS.name);
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

        snippetHighlight(entry, Entry::setStId, ST_ID.name, snippets);
        snippetHighlight(entry, Entry::setName, NAME.name, snippets);
        snippetHighlight(entry, Entry::setSummation, SUMMATION.name, snippets);
        snippetHighlight(entry, Entry::setReferenceName, REFERENCE_NAME.name, snippets);
        snippetHighlight(entry, Entry::setRegulator, REGULATOR.name, snippets);
        snippetHighlight(entry, Entry::setRegulatedEntity, REGULATED_ENTITY.name, snippets);
    }

    private void snippetHighlight(Entry entry, BiConsumer<Entry, String> fieldSetter, String field, Map<String, List<String>> snippets) {
        List<String> snippet = snippets.get(field);
        if (snippet != null && !snippet.isEmpty()) {
            fieldSetter.accept(entry, snippet.get(0));
        }
    }

    private String selectRightHighlightingForReferenceIdentifiers(SolrDocument solrDocument, Map<String, List<String>> snippets) {
        List<String> identifierSnippets = snippets.get(REFERENCE_IDENTIFIERS.name);
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
                if (solrDocument.containsKey(LLPS.name)) {
                    rtn.addLlps(solrDocument.getFieldValues(LLPS.name).stream().map(Object::toString).collect(Collectors.toList()));
                }
                if (solrDocument.containsKey(queryObject.getOccurrencesFieldName())) {
                    List<String> rawOccurrences = solrDocument.getFieldValues(queryObject.getOccurrencesFieldName()).stream().map(Object::toString).collect(Collectors.toList());
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
            rtn.getLlps().forEach(s -> {
                // getInteractsWith uses a dangerous approach where it returns null if the collection is empty.
                // gviteri doesn't remember why it was designed like that. I will check if not null and fix that
                // when it is safe to do.
                if (rtn.getInteractsWith() != null && !rtn.getInteractsWith().isEmpty())
                    rtn.getInteractsWith().remove(s);
            });

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
                        String identifier = (String) solrDocument.getFieldValue(TARGET_IDENTIFIER.name);
                        resource = (String) solrDocument.getFieldValue(TARGET_RESOURCE.name);
                        List<String> accessions = solrDocument.getFieldValues(TARGET_ACCESSIONS.name).stream().map(Object::toString).collect(Collectors.toList());
                        List<String> geneNames = null;
                        if (solrDocument.containsKey(TARGET_GENENAMES.name))
                            geneNames = solrDocument.getFieldValues(TARGET_GENENAMES.name).stream().map(Object::toString).collect(Collectors.toList());
                        List<String> synonyms = null;
                        if (solrDocument.containsKey(TARGET_SYNONYMS.name))
                            synonyms = solrDocument.getFieldValues(TARGET_SYNONYMS.name).stream().map(Object::toString).collect(Collectors.toList());

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
                return parseResponse(queryResponse, false).getResults().get(0);
            }
        }
        return null;
    }

    public Entry getIcon(Query queryObject) throws SolrSearcherException {
        if (queryObject != null && queryObject.getQuery() != null && !queryObject.getQuery().isEmpty()) {
            QueryResponse queryResponse = solrCore.getIcon(queryObject);
            if (queryResponse != null) {
                if (queryResponse.getResults().getNumFound() > 0L) {
                    return parseResponse(queryResponse, true).getResults().get(0).getEntries().get(0);
                }
            }
        }
        return null;
    }


}
