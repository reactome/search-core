package org.reactome.server.search.service;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.reactome.server.search.CoreConfiguration;
import org.reactome.server.search.domain.*;
import org.reactome.server.search.exception.SolrSearcherException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Created by:
 *
 * @author Florian Korninger (florian.korninger@ebi.ac.uk)
 * @since 10.11.15.
 * <p>
 * 507868 Will test wrong. Difference is that duplications are removed in the graph
 */
@ContextConfiguration(classes = {CoreConfiguration.class})
@SpringBootTest
public class SearchServiceTest {

    private static final Logger logger = LoggerFactory.getLogger("testLogger");
    private static final String suggest = "apoptos";
    private static final String spellcheck = "appoptosis";
    private static final List<String> species = List.of("Homo sapiens");
    private static final List<String> types = List.of("Pathway", "Reaction");
    private static final Query query = new Query.Builder("apoptosis").forSpecies(species).withTypes(types).build();


    @Autowired
    private SearchService searchService;


    @BeforeAll
    static void setUp(@Autowired SearchService searchService) {
        assumeTrue(searchService.ping());
    }

    @Test
    public void testGetFacetingInformation() throws SolrSearcherException {
        logger.info("Started testing searchService.getFacetingInformation");
        long start, time;
        start = System.currentTimeMillis();
        FacetMapping facetMapping = searchService.getFacetingInformation(query, false);
        time = System.currentTimeMillis() - start;
        logger.info("GraphDb execution time: " + time + "ms");
        assertTrue(309 <= facetMapping.getTotalNumFount());
        logger.info("Finished");
    }

    @Test
    public void testGetForcedFilteredFacetingInformation() throws SolrSearcherException {
        logger.info("Started testing searchService.getFacetingInformation");
        long start, time;
        start = System.currentTimeMillis();
        FacetMapping facetMapping = searchService.getFacetingInformation(new Query.Builder("dog").withTypes(List.of("Icon")).build(), true);
        time = System.currentTimeMillis() - start;
        logger.info("GraphDb execution time: " + time + "ms");
        assertEquals(0, facetMapping.getTotalNumFount());
        logger.info("Finished");
    }

    @Test
    public void testGetTotalFacetingInformation() throws SolrSearcherException {
        logger.info("Started testing searchService.getTotalFacetingInformation");
        long start, time;
        start = System.currentTimeMillis();
        FacetMapping facetMapping = searchService.getTotalFacetingInformation();
        time = System.currentTimeMillis() - start;
        logger.info("GraphDb execution time: " + time + "ms");
        assertTrue(465000 <= facetMapping.getTotalNumFount());
        Optional<FacetContainer> homoSapiens = facetMapping
                .getSpeciesFacet()
                .getAvailable()
                .stream()
                .filter(facetContainer -> facetContainer.getName().equals("Homo sapiens"))
                .findFirst();
        assertTrue(homoSapiens.isPresent());
        assertTrue(homoSapiens.get().getCount() > 68000);
        logger.info("Finished");
    }

    @Test
    public void testGetAutocompleteSuggestions() throws SolrSearcherException {
        logger.info("Started testing searchService.getAutocompleteSuggestions");
        long start, time;
        start = System.currentTimeMillis();
        List<String> suggestionsList = searchService.getAutocompleteSuggestions(suggest);
        time = System.currentTimeMillis() - start;
        logger.info("GraphDb execution time: " + time + "ms");
        Set<String> suggestions = new HashSet<>(suggestionsList);
        assertTrue(3 <= suggestions.size());
        assertTrue(suggestions.contains("apoptosis"));
        logger.info("Finished");
    }

    @Test
    public void testGetSpellcheckSuggestions() throws SolrSearcherException {
        logger.info("Started testing searchService.getSpellcheckSuggestions");
        long start, time;
        start = System.currentTimeMillis();
        List<String> suggestionsList = searchService.getSpellcheckSuggestions(spellcheck);
        time = System.currentTimeMillis() - start;
        logger.info("GraphDb execution time: " + time + "ms");
        Set<String> suggestions = new HashSet<>(suggestionsList);
        assertTrue(suggestions.contains("apoptosis"));
        logger.info("Finished");
    }

    @Test
    public void testGetEntries() throws SolrSearcherException {
        logger.info("Started testing searchService.getEntries");
        long start, time;
        start = System.currentTimeMillis();
        GroupedResult groupedResult = searchService.getEntries(query, true);
        time = System.currentTimeMillis() - start;
        logger.info("GraphDb execution time: " + time + "ms");
        assertEquals(2, groupedResult.getNumberOfGroups());
        assertTrue(309 <= groupedResult.getNumberOfMatches());
        logger.info("Finished");
    }

    @Test
    public void testGetSearchResultUseCases() throws SolrSearcherException {
        logger.info("Started testing searchService.searchResult with different queries");
        int rowCount = 30;
        int page = 1;
        long start, time;
        Map<String, List<String>> queryToExpectedFirstResultStId = new LinkedHashMap<>();

        queryToExpectedFirstResultStId.put("pten", List.of("R-HSA-199420")); // Gene name
        queryToExpectedFirstResultStId.put("GO:0051800", List.of("R-HSA-1676149")); // Gene Ontology of pten (Other identifiers)
        queryToExpectedFirstResultStId.put("0051800", List.of("R-HSA-1676149")); // Gene Ontology ID of pten
        queryToExpectedFirstResultStId.put("HGNC:5013", List.of("R-HSA-9609901")); // Reference Gene ID
        queryToExpectedFirstResultStId.put("NCBI:3162", List.of("R-HSA-9609901")); // Truncated "NCBI Gene:3162" : ReferenceGene
        queryToExpectedFirstResultStId.put("NCBI-Gene:3162", List.of("R-HSA-9609901")); // Replaced "NCBI Gene:3162" : ReferenceGene
        queryToExpectedFirstResultStId.put("Lymphoid and a non-Lymphoid cell", List.of("R-HSA-198933")); // Pathway name containing "and" next to a stop word

        for (Map.Entry<String, List<String>> searchResultCouple : queryToExpectedFirstResultStId.entrySet()) {
            start = System.currentTimeMillis();
            String queryString = searchResultCouple.getKey();
            Query query = new Query.Builder(queryString).forSpecies(species).build();
            SearchResult result = searchService.getSearchResult(query, rowCount, page, false);
            time = System.currentTimeMillis() - start;
            logger.info(queryString + " searched in " + time + "ms");

            Set<String> resultStIds = result
                    .getGroupedResult()
                    .getResults().get(0)
                    .getEntries().stream()
                    .map(Entry::getStId).collect(Collectors.toSet());

            for (String expected : searchResultCouple.getValue()) {
                assertTrue(resultStIds.contains(expected), "\"" + queryString + "\" did not match the expected document " + expected + "on the top 30 rows");
            }
        }
        logger.info("Finished");
    }

    @Test
    public void testGetSearchResult() throws SolrSearcherException {
        String searchTerm = "apoptosys";
        int rowCount = 30;
        int page = 1;

        List<String> species = new ArrayList<>();
        species.add("Homo sapiens");
        species.add("Entries without species");

        Query query = new Query.Builder(searchTerm).forSpecies(species).build();

        SearchResult searchResult = searchService.getSearchResult(query, rowCount, page, true);

        assertNull(searchResult);

        List<String> suggestions = searchService.getSpellcheckSuggestions(searchTerm);

        assertNotNull(suggestions);
        assertTrue(suggestions.size() > 0);
    }

    @Test
    public void testGetSearchResultFacets() throws SolrSearcherException {
        String searchTerm = "apoptosis";
        int rowCount = 30;
        int page = 2;

        List<String> species = new ArrayList<>();
        species.add("Homo sapiens");
        species.add("Entries without species");

        List<String> types = new ArrayList<>();
        types.add("Pathway");

        List<String> compartment = new ArrayList<>();
        compartment.add("cytosol");

        List<String> keywords = new ArrayList<>();
        keywords.add("binds");

        Query query = new Query.Builder(searchTerm).forSpecies(species).withTypes(types).inCompartments(compartment).withKeywords(keywords).build();
        SearchResult searchResult = searchService.getSearchResult(query, rowCount, page, true);
        assertEquals(searchResult.getGroupedResult().getNumberOfMatches(), 3);
    }

    @Test
    public void testGetEntriesNameGram() throws SolrSearcherException {
        List<String> species = List.of("Homo sapiens");
        Query query = new Query.Builder("transp").forSpecies(species).build();
        GroupedResult groupedResult = searchService.getEntries(query, true);
        assertTrue(5 <= groupedResult.getNumberOfGroups());
        assertTrue(300 <= groupedResult.getNumberOfMatches());
    }

    @Test
    public void testFireworks() throws SolrSearcherException {
        List<String> species = List.of("Homo sapiens");
        Query query = new Query.Builder("PTEN").forSpecies(species).withTypes(Collections.singletonList("Protein")).build();
        FireworksResult fireworksResult = searchService.getFireworks(query);
        assertTrue(15 <= fireworksResult.getFound(), "15 results or more are expected");
    }

    @Test
    public void testFireworksWithInteractors() throws SolrSearcherException {
        List<String> species = List.of("Homo sapiens");
        Query query = new Query.Builder("IKZF3").forSpecies(species).withTypes(Collections.singletonList("Interactor")).build();
        FireworksResult fireworksResult = searchService.getFireworks(query);
        assertTrue(1 <= fireworksResult.getFound(), "1 or more results are expected");
    }

    @Test
    public void testDiagramSearchSummary() throws SolrSearcherException {
        // Do not initialize as Collections.singletonList
        List<String> species = new ArrayList<>();
        species.add("Homo sapiens");
        String term = "KIF";
        String diagram = "R-HSA-8848021";
        Query query = new Query.Builder(term).addFilterQuery(diagram).forSpecies(species).build();
        DiagramSearchSummary dss = searchService.getDiagramSearchSummary(query);

        assertTrue(8 <= dss.getDiagramResult().getFound(), "8 or more results diagram results are expected");
        Optional<FacetContainer> diagramFacets = dss.getDiagramResult().getFacets().stream().findFirst();
        assertTrue(diagramFacets.isPresent());
        assertTrue(7 <= diagramFacets.get().getCount(), "7 or more results other diagrams results are expected");
        assertEquals("Protein", diagramFacets.get().getName(), "Protein is expected");
        assertTrue(94 <= dss.getFireworksResult().getFound(), "94 or more results other diagrams results are expected");
        Optional<FacetContainer> fireworkFacets = dss.getFireworksResult().getFacets().stream().findFirst();
        assertTrue(fireworkFacets.isPresent());
        assertEquals("Protein", fireworkFacets.get().getName(), "Protein is expected");
        assertTrue(60 <= fireworkFacets.get().getCount(), "60 or more results other diagrams results are expected");
    }

    @Test
    public void testDiagramSearchSummarySmallMolecules() throws SolrSearcherException {
        List<String> species = new ArrayList<>();
        species.add("Homo sapiens");
        species.add("Entries without species");
        String term = "ATP";
        String diagram = "R-HSA-69620";
        Query query = new Query.Builder(term).addFilterQuery(diagram).forSpecies(species).build();
        DiagramSearchSummary dss = searchService.getDiagramSearchSummary(query);

        assertTrue(2 <= dss.getDiagramResult().getFound(), "2 or more results diagram results are expected");
        assertTrue(690 <= dss.getFireworksResult().getFound(), "690 or more results other diagrams results are expected");
    }

    @Test
    public void testFireworksSpecies() throws SolrSearcherException {
        // Do not initialize as Collections.singletonList

        List<String> species = new ArrayList<>();
        species.add("Gallus gallus");
        Query query = new Query.Builder("PG").forSpecies(species).build();
        FireworksResult fireworksResult = searchService.getFireworks(query);

        assertNotNull(fireworksResult);
        assertNotNull(fireworksResult.getEntries());
        assertNotEquals(fireworksResult.getEntries().size(), 0);
        List<Entry> fsten = fireworksResult.getEntries().stream().filter(entry -> entry.getFireworksSpecies().size() >= 10).collect(Collectors.toList());
        assertTrue(10 <= fsten.iterator().next().getFireworksSpecies().size(), "10 species or more are expected");
    }

    @Test
    public void testDiagram() throws SolrSearcherException {
        // Do not initialize as Collections.singletonList
        List<String> species = new ArrayList<>();
        species.add("Homo sapiens");
        String term = "MAD";
        String diagram = "R-HSA-8848021";
        Query query = new Query.Builder(term).addFilterQuery(diagram).forSpecies(species).build();
        DiagramResult diagramResults = searchService.getDiagrams(query);

        assertNotNull(diagramResults);
        assertNotNull(diagramResults.getEntries());
        assertTrue(2 <= diagramResults.getFound(), "2 or more entries are expected");
    }

    @Test
    public void testOccurrences() throws SolrSearcherException {
        // Do not initialize as Collections.singletonList
        List<String> species = List.of("Homo sapiens");
        String termStId = "R-HSA-879382";
        String diagram = "R-HSA-168164";
        Query query = new Query.Builder(termStId).addFilterQuery(diagram).forSpecies(species).build();
        DiagramOccurrencesResult diagramOccurrencesResult = searchService.getDiagramOccurrencesResult(query);

        assertNotNull(diagramOccurrencesResult);

        assertFalse(diagramOccurrencesResult.getInDiagram(), "The entry " + termStId + " is not expected to be in the diagram " + diagram);
        assertNotNull(diagramOccurrencesResult.getOccurrences());
        assertTrue(1 <= diagramOccurrencesResult.getOccurrences().size(), "1 or more occurrences is expected");
    }

    @Test
    public void testNoOccurrences() throws SolrSearcherException {
        // Do not initialize as Collections.singletonList
        List<String> species = new ArrayList<>();
        species.add("Homo sapiens");
        String termStId = "R-HSA-879382";
        String diagram = "R-HSA-6798695"; // 168164
        Query query = new Query.Builder(termStId).addFilterQuery(diagram).forSpecies(species).build();
        DiagramOccurrencesResult diagramOccurrencesResult = searchService.getDiagramOccurrencesResult(query);

        assertNotNull(diagramOccurrencesResult);
        assertTrue(diagramOccurrencesResult.getInDiagram(), "The entry " + termStId + " is not expected to be in the diagram " + diagram);
        assertNull(diagramOccurrencesResult.getOccurrences());
    }

    @Test
    public void testFireworksFlagging() throws SolrSearcherException {
        // By default filter query by Human and Entries without species
        String term = "TPM3";
        Query query = new Query.Builder(term).build();
        FireworksOccurrencesResult fireworksFlaggingSet = searchService.fireworksFlagging(query);

        assertFalse(fireworksFlaggingSet.isEmpty());
        assertTrue(7 <= fireworksFlaggingSet.getLlps().size(), "7 or more fireworks flagging 'lower level' pathways are expected");
        assertTrue(6 <= fireworksFlaggingSet.getInteractsWith().size(), "6 or more fireworks flagging 'interacts with' stid are expected");
    }

    @Test
    public void testFireworksFlaggingAnotherSpecies() throws SolrSearcherException {
        // Do not initialize as Collections.singletonList
        List<String> species = new ArrayList<>();
        species.add("Bos taurus");

        String term = "STAT4";
        Query query = new Query.Builder(term).forSpecies(species).build();
        FireworksOccurrencesResult fireworksFlaggingSet = searchService.fireworksFlagging(query);

        assertFalse(fireworksFlaggingSet.isEmpty());
    }

    @Test
    public void testFireworksFlaggingSmallMolecule() throws SolrSearcherException {
        // By default filter query by Human and Entries without species
        String term = "CHEBI:15377";
        Query query = new Query.Builder(term).build();
        FireworksOccurrencesResult fireworksFlaggingSet = searchService.fireworksFlagging(query);

        assertFalse(fireworksFlaggingSet.isEmpty());
        assertTrue(4000 <= fireworksFlaggingSet.getLlps().size(), "4000 or more fireworks flagging stid are expected");
    }

    @Test
    public void testTargetForReactome() {
        // By default filter query by Human and Entries without species
        String q = "A6NCF5 NOTTARGET";
        List<String> species = new ArrayList<>();
        species.add("Homo sapiens");
        species.add("Entries without species");
        Query query = new Query.Builder(q).forSpecies(species).build();
        // The reporting is done the report project. Just check if the solr core is available.
        Set<TargetResult> targets = searchService.getTargets(query);
        assertNotNull(targets);
        assertFalse(targets.isEmpty());
        assertEquals(2, targets.size());
        assertEquals(1, targets.stream().filter(TargetResult::isTarget).count());
        assertEquals(1, targets.stream().filter(t -> !t.isTarget()).count());
    }

    @Test
    public void testPersonSearchByName() throws SolrSearcherException {
        String q = "John";
        List<String> species = new ArrayList<>();
        species.add("Homo sapiens");
        species.add("Entries without species");
        Query query = new Query.Builder(q).forSpecies(species).build();
        SearchResult searchResult = searchService.getSearchResult(query, 30, 1, true);
        assertNotNull(searchResult);
        assertNotNull(searchResult.getGroupedResult());
        List<Result> results = searchResult.getGroupedResult().getResults();
        assertNotNull(results);
        for (Result result : results) {
            if (result.getTypeName().equals("Person")) {
                List<Entry> entries = result.getEntries();
                assertNotNull(entries);
                assertTrue(4 <= entries.size(), "4 or more people are expected");
            }
        }
    }

    @Test
    public void testPersonSearchByOrcidId() throws SolrSearcherException {
        String q = "0000-0002-5494-626X";
        List<String> species = new ArrayList<>();
        species.add("Homo sapiens");
        species.add("Entries without species");
        Query query = new Query.Builder(q).forSpecies(species).build();
        SearchResult searchResult = searchService.getSearchResult(query, 30, 1, true);
        assertNotNull(searchResult);
        assertNotNull(searchResult.getGroupedResult());
        List<Result> results = searchResult.getGroupedResult().getResults();
        assertNotNull(results);
        for (Result result : results) {
            if (result.getTypeName().equals("Person")) {
                List<Entry> entries = result.getEntries();
                assertNotNull(entries);
                assertTrue(1 <= entries.size(), "1 or more people are expected");

            }
        }
    }

    @Test
    public void getIconFacetingInformation() throws SolrSearcherException {
        logger.info("Started testing searchService.getIconFacetingInformation()");
        FacetMapping facetMapping = searchService.getIconFacetingInformation();
        List<FacetContainer> cc = facetMapping.getIconCategoriesFacet().getAvailable();
        assertTrue(cc.size() >= 8, "Icon faceting didn't match");
    }

    @Test
    public void testIconsResult() throws SolrSearcherException {
        logger.info("Started testing searchService.getIconFacetingInformation()");
        Query query = new Query.Builder("{!term f=iconCategories}protein").build();
        Result icons = searchService.getIconsResult(query, 30, 1);
        assertNotNull(icons);
        assertNotNull(icons.getEntries());
        assertTrue(icons.getEntries().size() >= 20, "Couldn't find all the icons");
    }

    @Test
    public void testGetIcon() throws SolrSearcherException {
        logger.info("Started testing searchService.testGetIcon");
        String name = "R-ICO-013100";
        Query query = new Query.Builder(name).build();
        Entry icon = searchService.getIcon(query);
        assertNotNull(icon);
        assertEquals(name, icon.getStId());
    }

    @Test
    public void testGetAllIcons() throws SolrSearcherException {
        logger.info("Started testing searchService.testGetAllIcons");
        List<Entry> icons = searchService.getAllIcons();
        assertNotNull(icons);
        assertTrue(icons.size() >= 2040 && icons.size() <= 3000, "Couldn't find all the icons");
    }
}