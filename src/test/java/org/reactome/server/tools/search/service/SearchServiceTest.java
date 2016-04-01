package org.reactome.server.tools.search.service;

import org.junit.AfterClass;
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
import java.util.List;

import static org.junit.Assume.assumeTrue;

/**
 * Created by:
 *
 * @author Florian Korninger (florian.korninger@ebi.ac.uk)
 * @since 10.11.15.
 *
 * 507868 Will test wrong. Difference is that duplications are removed in the graph
 *
 */
@ContextConfiguration(classes = { CoreConfiguration.class })
@RunWith(SpringJUnit4ClassRunner.class)
public class SearchServiceTest {

    private static final Logger logger = LoggerFactory.getLogger("testLogger");

    private static Query query;
    private static final String detail = "R-HSA-199420";
    private static final String accession = "P60484";
    private static final String suggest = "apoptos";

    @Autowired
    private SearchService searchService;

    @BeforeClass
    public static void setUpClass() {
        logger.info(" --- !!! Running SearchServiceTests !!! --- \n");

        List<String> species = new ArrayList<>();
        species.add("Homo sapiens");
        List<String> types = new ArrayList<>();
        types.add("Pathway");
        types.add("Reaction");
        query =  new Query("apoptosis", species, types, null, null);
    }

    @AfterClass
    public static void tearDownClass() {
        logger.info("\n\n");
    }


    @Before
    public void setUp() throws Exception {
        assumeTrue(searchService.ping());
    }

    @Test
    public void testGetFacetingInformation() throws SolrSearcherException {
        FacetMapping facetMapping = searchService.getFacetingInformation(query);
        System.out.println();
    }

    @Test
    public void testGetTotalFacetingInformation() throws SolrSearcherException {
        FacetMapping facetMapping = searchService.getTotalFacetingInformation();
        System.out.println();
    }

    @Test
    public void testGetAutocompleteSuggestions() throws SolrSearcherException {
        List<String> suggestions = searchService.getAutocompleteSuggestions(suggest);
        System.out.println();
    }

    @Test
    public void testGetSpellcheckSuggestions() throws SolrSearcherException {
        List<String> suggestions = searchService.getSpellcheckSuggestions(suggest);
        System.out.println();
    }

    @Test
    public void testGetInteractionDetail() throws SolrSearcherException {
        InteractorEntry interactorEntry = searchService.getInteractionDetail(accession);
        System.out.println();
    }

    @Test
    public void testGetEntryById() {
        Entry entry = searchService.getEntryById(detail);
        System.out.println();
    }

    @Test
    public void testGetEntries() throws SolrSearcherException {
        GroupedResult groupedResult = searchService.getEntries(query,true);
        System.out.println();
    }


}