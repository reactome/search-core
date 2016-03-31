package org.reactome.server.tools.search.service;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.reactome.server.tools.search.exception.SolrSearcherException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.sql.SQLException;

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
public class DatabaseObjectServiceTest {

    private static final Logger logger = LoggerFactory.getLogger("testLogger");

    @Autowired
    private SearchService searchService;

    @BeforeClass
    public static void setUpClass() {
        logger.info(" --- !!! Running DatabaseObjectServiceTests !!! --- \n");
    }

    @AfterClass
    public static void tearDownClass() {
        logger.info("\n\n");
    }

    @Test
    public void test () throws SolrSearcherException, SQLException {
        System.out.println();
        Object x = searchService.getSpellcheckSuggestions("apoptosi");
        System.out.println();

    }

}