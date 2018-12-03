package org.reactome.server.search.service;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.reactome.server.search.CoreConfiguration;
import org.reactome.server.search.domain.*;
import org.reactome.server.search.exception.SolrSearcherException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

/**
 * Created by:
 *
 * @author Florian Korninger (florian.korninger@ebi.ac.uk)
 * @since 10.11.15.
 * <p>
 * 507868 Will test wrong. Difference is that duplications are removed in the graph
 */
@ContextConfiguration(classes = {CoreConfiguration.class})
@RunWith(SpringJUnit4ClassRunner.class)
public class SearchServiceTest {

    private final static Logger logger = LoggerFactory.getLogger("testLogger");
    private static final String suggest = "apoptos";
    private static final String spellcheck = "appoptosis";
    private static Query query;
    @Autowired
    private SearchService searchService;

    @BeforeClass
    public static void setUpClass() {
        List<String> species = new ArrayList<>();
        species.add("Homo sapiens");
        List<String> types = new ArrayList<>();
        types.add("Pathway");
        types.add("Reaction");
        query = new Query("apoptosis", species, types, null, null);
    }

    @Before
    public void setUp() {
        assumeTrue(searchService.ping());
    }

    @Test
    public void testGetFacetingInformation() throws SolrSearcherException {
        logger.info("Started testing searchService.getFacetingInformation");
        long start, time;
        start = System.currentTimeMillis();
        FacetMapping facetMapping = searchService.getFacetingInformation(query);
        time = System.currentTimeMillis() - start;
        logger.info("GraphDb execution time: " + time + "ms");
        assertTrue(309 <= facetMapping.getTotalNumFount());
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
        assertTrue(471389 <= facetMapping.getTotalNumFount());
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
    public void testGetSearchResult() throws SolrSearcherException {
        String searchTerm = "apoptosys";
        int rowCount = 30;
        int page = 1;

        List<String> species = new ArrayList<>();
        species.add("Homo sapiens");
        species.add("Entries without species");

        Query query = new Query(searchTerm, species, null, null, null);

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

        Query query = new Query(searchTerm, species, types, compartment, keywords);
        SearchResult searchResult = searchService.getSearchResult(query, rowCount, page, true);
        assertEquals(searchResult.getGroupedResult().getNumberOfMatches(), 2);
    }

    @Test
    public void testGetEntriesNameGram() throws SolrSearcherException {
        // Do not initialize as Collections.singletonList
        List<String> species = new ArrayList<>();
        species.add("Homo sapiens");
        Query query = new Query("transp", species, null, null, null);
        GroupedResult groupedResult = searchService.getEntries(query, true);
        assertTrue(5 <= groupedResult.getNumberOfGroups());
        assertTrue(300 <= groupedResult.getNumberOfMatches());
    }

    @Test
    public void testFireworks() throws SolrSearcherException {
        // Do not initialize as Collections.singletonList
        List<String> species = new ArrayList<>();
        species.add("Homo sapiens");
        Query query = new Query("PTEN", species, Collections.singletonList("Protein"), null, null);
        FireworksResult fireworksResult = searchService.getFireworks(query);
        assertTrue("15 results or more are expected", 15 <= fireworksResult.getFound());
    }

    @Test
    public void testFireworksWithInteractors() throws SolrSearcherException {
        // Do not initialize as Collections.singletonList
        List<String> species = new ArrayList<>();
        species.add("Homo sapiens");
        Query query = new Query("IKZF3", species, Collections.singletonList("Interactor"), null, null);
        FireworksResult fireworksResult = searchService.getFireworks(query);
        assertTrue("1 or more results are expected", 1 <= fireworksResult.getFound());
    }

    @Test
    public void testDiagramSearchSummary() throws SolrSearcherException {
        // Do not initialize as Collections.singletonList
        List<String> species = new ArrayList<>();
        species.add("Homo sapiens");
        String term = "KIF";
        String diagram = "R-HSA-8848021";
        Query query = new Query(term, diagram, species, null, null, null);
        DiagramSearchSummary dss = searchService.getDiagramSearchSummary(query);

        assertTrue("8 or more results diagram results are expected", 8 <= dss.getDiagramResult().getFound());
        assertTrue("7 or more results other diagrams results are expected", 7 <= dss.getDiagramResult().getFacets().stream().findFirst().get().getCount());
        assertEquals("Protein is expected", "Protein", dss.getDiagramResult().getFacets().stream().findFirst().get().getName());
        assertTrue("94 or more results other diagrams results are expected", 94 <= dss.getFireworksResult().getFound());
        assertEquals("Protein is expected", "Protein", dss.getFireworksResult().getFacets().stream().findFirst().get().getName());
        assertTrue("60 or more results other diagrams results are expected", 60 <= dss.getFireworksResult().getFacets().stream().findFirst().get().getCount());
    }

    @Test
    public void testDiagramSearchSummarySmallMolecules() throws SolrSearcherException {
        // Do not initialize as Collections.singletonList
        List<String> species = new ArrayList<>();
        species.add("Homo sapiens");
        species.add("Entries without species");
        String term = "ATP";
        String diagram = "R-HSA-69620";
        Query query = new Query(term, diagram, species, null, null, null);
        DiagramSearchSummary dss = searchService.getDiagramSearchSummary(query);

        assertTrue("2 or more results diagram results are expected", 2 <= dss.getDiagramResult().getFound());
        assertTrue("690 or more results other diagrams results are expected", 690 <= dss.getFireworksResult().getFound());
    }

    @Test
    public void testFireworksSpecies() throws SolrSearcherException {
        // Do not initialize as Collections.singletonList
        List<String> species = new ArrayList<>();
        species.add("Gallus gallus");
        Query query = new Query("PG", species, null, null, null);
        FireworksResult fireworksResult = searchService.getFireworks(query);

        assertNotNull(fireworksResult);
        assertNotNull(fireworksResult.getEntries());
        List<Entry> fsten = fireworksResult.getEntries().stream().filter(entry -> entry.getFireworksSpecies().size() >= 10).collect(Collectors.toList());
        assertTrue("14 species or more are expected", 10 <= fsten.iterator().next().getFireworksSpecies().size());
    }

    @Test
    public void testDiagram() throws SolrSearcherException {
        // Do not initialize as Collections.singletonList
        List<String> species = new ArrayList<>();
        species.add("Homo sapiens");
        String term = "MAD";
        String diagram = "R-HSA-8848021";
        Query query = new Query(term, diagram, species, null, null, null);
        DiagramResult diagramResults = searchService.getDiagrams(query);

        assertNotNull(diagramResults);
        assertNotNull(diagramResults.getEntries());
        assertTrue("2 or more entries are expected", 2 <= diagramResults.getFound());
    }

    @Test
    public void testOccurrences() throws SolrSearcherException {
        // Do not initialize as Collections.singletonList
        List<String> species = new ArrayList<>();
        species.add("Homo sapiens");
        String termStId = "R-HSA-879382";
        String diagram = "R-HSA-168164";
        Query query = new Query(termStId, diagram, species, null, null, null);
        DiagramOccurrencesResult diagramOccurrencesResult = searchService.getDiagramOccurrencesResult(query);

        assertNotNull(diagramOccurrencesResult);
        assertFalse("The entry " + termStId + " is not expected to be in the diagram " + diagram, diagramOccurrencesResult.getInDiagram());
        assertNotNull(diagramOccurrencesResult.getOccurrences());
        assertTrue("1 or more occurrences is expected", 1 <= diagramOccurrencesResult.getOccurrences().size());
    }

    @Test
    public void testNoOccurrences() throws SolrSearcherException {
        // Do not initialize as Collections.singletonList
        List<String> species = new ArrayList<>();
        species.add("Homo sapiens");
        String termStId = "R-HSA-879382";
        String diagram = "R-HSA-6798695"; // 168164
        Query query = new Query(termStId, diagram, species, null, null, null);
        DiagramOccurrencesResult diagramOccurrencesResult = searchService.getDiagramOccurrencesResult(query);

        assertNotNull(diagramOccurrencesResult);
        assertTrue("The entry " + termStId + " is not expected to be in the diagram " + diagram, diagramOccurrencesResult.getInDiagram());
        assertNull(diagramOccurrencesResult.getOccurrences());
    }

    @Test
    public void testFireworksFlagging() throws SolrSearcherException {
        // By default filter query by Human and Entries without species
        String term = "PTEN";
        Query query = new Query(term, null, null, null, null);
        Collection<String> fireworksFlaggingSet = searchService.fireworksFlagging(query);

        assertFalse(fireworksFlaggingSet.isEmpty());
        assertTrue("12 or more fireworks flagging stid are expected", 12 <= fireworksFlaggingSet.size());
    }

    @Test
    public void testFireworksFlaggingAnotherSpecies() throws SolrSearcherException {
        // Do not initialize as Collections.singletonList
        List<String> species = new ArrayList<>();
        species.add("Bos taurus");

        String term = "NTN1";
        Query query = new Query(term, species, null, null, null);
        Collection<String> fireworksFlaggingSet = searchService.fireworksFlagging(query);

        assertFalse(fireworksFlaggingSet.isEmpty());
    }

    @Test
    public void testFireworksFlaggingSmallMolecule() throws SolrSearcherException {
        // By default filter query by Human and Entries without species
        String term = "CHEBI:15377";
        Query query = new Query(term, null, null, null, null);
        Collection<String> fireworksFlaggingSet = searchService.fireworksFlagging(query);

        assertFalse(fireworksFlaggingSet.isEmpty());
        assertTrue("4000 or more fireworks flagging stid are expected", 4000 <= fireworksFlaggingSet.size());
    }

    @Test
    public void testTargetForReactome() {
        // By default filter query by Human and Entries without species
        String q = "A6NCF5 NOTTARGET";
        List<String> species = new ArrayList<>();
        species.add("Homo sapiens");
        species.add("Entries without species");
        Query query = new Query(q, species, null, null, null);
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
        Query query = new Query(q, species, null, null, null);
        SearchResult searchResult = searchService.getSearchResult(query, 30, 1, true);
        assertNotNull(searchResult);
        assertNotNull(searchResult.getGroupedResult());
        List<Result> results = searchResult.getGroupedResult().getResults();
        assertNotNull(results);
        for (Result result : results) {
            if (result.getTypeName().equals("Person")) {
                List<Entry> entries = result.getEntries();
                assertNotNull(entries);
                assertTrue("4 or more people are expected", 4 <= entries.size());
            }
        }
    }

    @Test
    public void testPersonSearchByOrcidId() throws SolrSearcherException {
        String q = "0000-0002-5494-626X";
        List<String> species = new ArrayList<>();
        species.add("Homo sapiens");
        species.add("Entries without species");
        Query query = new Query(q, species, null, null, null);
        SearchResult searchResult = searchService.getSearchResult(query, 30, 1, true);
        assertNotNull(searchResult);
        assertNotNull(searchResult.getGroupedResult());
        List<Result> results = searchResult.getGroupedResult().getResults();
        assertNotNull(results);
        for (Result result : results) {
            if (result.getTypeName().equals("Person")) {
                List<Entry> entries = result.getEntries();
                assertNotNull(entries);
                assertTrue("1 or more people are expected", 1 <= entries.size());

            }
        }
    }

    @Test
    public void getIconFacetingInformation() throws SolrSearcherException {
        logger.info("Started testing searchService.getIconFacetingInformation()");
        FacetMapping facetMapping = searchService.getIconFacetingInformation();
        List<FacetContainer> cc = facetMapping.getIconCategoriesFacet().getAvailable();
        assertTrue("Icon faceting didn't match", cc.size() >= 8);
    }

    @Test
    public void testIconsResult() throws SolrSearcherException {
        logger.info("Started testing searchService.getIconFacetingInformation()");
        Query query = new Query("{!term f=iconCategories}protein", null, null, null, null);
        Result icons = searchService.getIconsResult(query, 30, 1);
        assertNotNull(icons);
        assertNotNull(icons.getEntries());
        assertTrue("Couldn't find all the icons", icons.getEntries().size() >= 20);
    }

    @Test
    public void testGetIcon() throws SolrSearcherException {
        logger.info("Started testing searchService.testGetIcon");
        String name = "R-ICO-013100";
        Query query = new Query(name, null, null, null, null);
        Entry icon = searchService.getIcon(query);
        assertNotNull(icon);
        assertEquals(name, icon.getStId());
    }

    @Test
    public void testGetAllIcons() throws SolrSearcherException {
        logger.info("Started testing searchService.testGetAllIcons");
        List<Entry> icons = searchService.getAllIcons();
        assertNotNull(icons);
        assertTrue("Couldn't find all the icons", icons.size() >= 1150 && icons.size() <= 1500);
    }
}