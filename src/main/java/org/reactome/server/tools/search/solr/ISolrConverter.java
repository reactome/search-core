package org.reactome.server.tools.search.solr;

import org.reactome.server.tools.search.domain.*;
import org.reactome.server.tools.search.exception.SolrSearcherException;

import java.util.List;

/**
 * Converts a Solr QueryResponse into Objects provided by domain
 *
 * @author Florian Korninger (fkorn@ebi.ac.uk)
 * @version 1.0
 */


public interface ISolrConverter {

    FacetMapping getFacetingInformation(Query query) throws SolrSearcherException;

    FacetMapping getFacetingInformation() throws SolrSearcherException;

    List<String> getAutocompleteSuggestions(String query) throws SolrSearcherException;

    List<String> getSpellcheckSuggestions(String query) throws SolrSearcherException;

    Entry getEntryById(String id) throws SolrSearcherException;

    GroupedResult getEntries(Query query) throws SolrSearcherException;

    GroupedResult getClusteredEntries(Query query) throws SolrSearcherException;

    InteractorEntry getInteractionDetail(String accession) throws SolrSearcherException;

}


