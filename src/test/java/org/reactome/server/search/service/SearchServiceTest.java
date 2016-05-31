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

    private final static Logger logger = LoggerFactory.getLogger(SearchServiceTest.class);

    private static Query query;
    private static final String accession = "P41227";
    private static final String suggest = "apoptos";
    private static final String spellcheck = "appoptosis";

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
        FacetMapping facetMapping = searchService.getFacetingInformation(query);
        assertTrue(309 <= facetMapping.getTotalNumFount());
    }

    @Test
    public void testGetTotalFacetingInformation() throws SolrSearcherException {
        FacetMapping facetMapping = searchService.getTotalFacetingInformation();
        assertTrue(471389 <= facetMapping.getTotalNumFount());
    }

    @Test
    public void testGetAutocompleteSuggestions() throws SolrSearcherException {
        List<String> suggestionsList = searchService.getAutocompleteSuggestions(suggest);
        Set<String> suggestions = new HashSet<>(suggestionsList);
        assertTrue(3 <= suggestions.size());
        assertTrue(suggestions.contains("apoptosis"));
    }

    @Test
    public void testGetSpellcheckSuggestions() throws SolrSearcherException {
        List<String> suggestionsList = searchService.getSpellcheckSuggestions(spellcheck);
        Set<String> suggestions = new HashSet<>(suggestionsList);
        assertTrue(suggestions.contains("apoptosis"));
    }

    @Test
    public void testGetInteractionDetail() throws SolrSearcherException {
        InteractorEntry interactorEntry = searchService.getInteractionDetail(accession);
        assertNotNull(interactorEntry);
        assertNotNull(interactorEntry.getInteractions());
        assertTrue(6 <= interactorEntry.getInteractions().size());
    }

    @Test
    public void testGetEntries() throws SolrSearcherException {
        GroupedResult groupedResult = searchService.getEntries(query, true);
        assertEquals(2, groupedResult.getNumberOfGroups());
        assertTrue(309 <= groupedResult.getNumberOfMatches());
    }

    @Test
    public void testGetSearchResult() throws SolrSearcherException {
        String searchTerm = "apoo";
        boolean cluster = true;
        int rowCount = 30;
        int page = 1;

        List<String> species = new ArrayList<>();
        species.add("Homo sapiens");
        species.add("Entries without species");

        Query query = new Query(searchTerm, species, null, null, null);

        SearchResult searchResult = searchService.getSearchResult(query, rowCount, page, cluster);

        assertNull(searchResult);

        List<String> suggestions = searchService.getSpellcheckSuggestions(searchTerm);

        assertNotNull(suggestions);
        assertTrue(suggestions.size() > 0);
    }

    @Test
    public void testGetSearchResultFacets() throws SolrSearcherException {
        String searchTerm = "apoptosis";
        boolean cluster = true;
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

        SearchResult searchResult = searchService.getSearchResult(query, rowCount, page, cluster);

        assertEquals(searchResult.getGroupedResult().getNumberOfMatches(), 2);

    }

    @Test
    public void testGetEntriesNameGram() throws SolrSearcherException {
        Query query = new Query("transp", Collections.singletonList("Homo sapiens"), null, null, null);
        GroupedResult groupedResult = searchService.getEntries(query, true);
        assertEquals(5, groupedResult.getNumberOfGroups());
        assertTrue(300 <= groupedResult.getNumberOfMatches());
    }

    @Test
    public void testGetEntriesNoResults() throws SolrSearcherException {
        Query query = new Query("apoo", Collections.singletonList("Homo sapiens"), null, null, null);
        GroupedResult groupedResult = searchService.getEntries(query, true);
        assertEquals(0, groupedResult.getNumberOfGroups());
        assertEquals(0, groupedResult.getNumberOfMatches());
    }

    @Test
    public void testFireworks() throws SolrSearcherException {
        Query query = new Query("PTEN", Collections.singletonList("Homo sapiens"), Collections.singletonList("Protein"), null, null);
        FireworksResult fireworksResult = searchService.getFireworks(query);
        assertTrue("15 results or more are expected", 15 <= fireworksResult.getFound());
    }

}