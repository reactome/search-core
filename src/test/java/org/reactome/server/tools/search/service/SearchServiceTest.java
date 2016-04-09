package org.reactome.server.tools.search.service;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.reactome.server.tools.search.domain.*;
import org.reactome.server.tools.search.exception.SolrSearcherException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    private final static Logger logger = LoggerFactory.getLogger("ee");

    private static Query query;
    private static final String stId = "R-HSA-199420";
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
    public void test() {
        System.out.println("");
        for (int i = 0; i < 100; i++) {
            System.out.println("xx" + i);
            logger.warn("some warning " + i);
        }
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
        assertTrue(8 <= interactorEntry.getInteractions().size());
    }

    @Test
    public void testGetEntryById() {
        Entry entry = searchService.getEntryById(stId);
        assertEquals("PTEN", entry.getName());
    }

    @Test
    public void testGetEntries() throws SolrSearcherException {
        GroupedResult groupedResult = searchService.getEntries(query, true);
        assertEquals(2, groupedResult.getNumberOfGroups());
        assertTrue(309 <= groupedResult.getNumberOfMatches());
    }


}