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
        int page = 1;

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
        assertEquals(5, groupedResult.getNumberOfGroups());
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
    public void testFireworksSpecies() throws SolrSearcherException {
        // Do not initialize as Collections.singletonList
        List<String> species = new ArrayList<>();
        species.add("Gallus gallus");
        Query query = new Query("PG", species, null, null, null);
        FireworksResult fireworksResult = searchService.getFireworks(query);

        assertNotNull(fireworksResult);
        assertNotNull(fireworksResult.getEntries());
        assertTrue("14 species or more are expected", 14 <= fireworksResult.getEntries().iterator().next().getFireworksSpecies().size());
    }

    @Test
    public void testDiagram() throws SolrSearcherException {
        // Do not initialize as Collections.singletonList
        List<String> species = new ArrayList<>();
        species.add("Homo sapiens");
        String term = "MAD1";
        String diagram = "R-HSA-9006927";
        Query query = new Query(term, diagram, species, null, null, null);
        DiagramResult diagramResults = searchService.getDiagrams(query);

        assertNotNull(diagramResults);
        assertNotNull(diagramResults.getEntries());
        assertTrue("5 or more entries are expected", 5 <= diagramResults.getFound());
    }

    @Test
    public void testOccurrences() throws SolrSearcherException {
        // Do not initialize as Collections.singletonList
        List<String> species = new ArrayList<>();
        species.add("Homo sapiens");
        String termStId = "R-HSA-879382";
        String diagram = "R-HSA-168164";
        Query query = new Query(termStId, diagram,  species, null, null, null);
        DiagramOccurrencesResult diagramOccurrencesResult = searchService.getDiagramOccurrencesResult(query);

        assertNotNull(diagramOccurrencesResult);
        assertFalse("The entry " + termStId + " is not expected to be in the diagram " + diagram, diagramOccurrencesResult.getInDiagram());
        assertNotNull(diagramOccurrencesResult.getOccurrences());
        assertTrue("2 or more occurrences are expected", 6 <= diagramOccurrencesResult.getOccurrences().size());
    }

    @Test
    public void testNoOccurrences() throws SolrSearcherException {
        // Do not initialize as Collections.singletonList
        List<String> species = new ArrayList<>();
        species.add("Homo sapiens");
        String termStId = "R-HSA-879382";
        String diagram = "R-HSA-6798695"; // 168164
        Query query = new Query(termStId, diagram,  species, null, null, null);
        DiagramOccurrencesResult diagramOccurrencesResult = searchService.getDiagramOccurrencesResult(query);

        assertNotNull(diagramOccurrencesResult);
        assertTrue("The entry " + termStId + " is not expected to be in the diagram " + diagram, diagramOccurrencesResult.getInDiagram());
        assertNull(diagramOccurrencesResult.getOccurrences());
    }
}