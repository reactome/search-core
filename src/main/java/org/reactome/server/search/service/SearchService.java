package org.reactome.server.search.service;

import org.reactome.server.search.domain.*;
import org.reactome.server.search.exception.SolrSearcherException;
import org.reactome.server.search.solr.SolrConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Search Service acts as api between the Controller and Solr / Database
 *
 * @author Florian Korninger (fkorn@ebi.ac.uk)
 * @version 1.0
 */
@SuppressWarnings({"unused", "WeakerAccess"})
@Service
public class SearchService {

    private final static Logger logger = LoggerFactory.getLogger("");

    @Autowired
    private SolrConverter solrConverter;

    /**
     * Method for testing if a connection to Solr can be established
     * @return true if status is ok
     */
    public Boolean ping() {
        return solrConverter.ping();
    }

    /**
     * This method is a simple aggregation of service methods used in the Content project
     * @param query QueryObject
     * @param rowCount number of rows displayed in one page
     * @param page page number
     * @param cluster clustered or not clustered result
     * @return Grouped result
     * @throws SolrSearcherException
     */
    public SearchResult getSearchResult (Query query, int rowCount, int page, boolean cluster) throws SolrSearcherException {
        FacetMapping facetMapping = getFacetingInformation(query);
        if (facetMapping == null || facetMapping.getTotalNumFount() < 1) {
            query = new Query(query.getQuery(),null,null,null,null);
            facetMapping = getFacetingInformation(query);
        }
        if (facetMapping != null && facetMapping.getTotalNumFount() > 0) {
            setPagingParameters(query,facetMapping,rowCount,page,cluster);
            GroupedResult groupedResult = getEntries(query, cluster);
            return new SearchResult(facetMapping,groupedResult,getHighestResultCount(groupedResult),query.getRows());
        }
        return null;
    }

    /**
     * Gets Faceting information for a specific query + filters.
     * This Method will query solr once again if the number of selected filters and found facets differ
     * (this will help preventing false faceting information when filter are contradictory to each other)
     *
     * @param queryObject query and filter (species types keywords compartments)
     * @return FacetMapping
     * @throws SolrSearcherException
     */
    public FacetMapping getFacetingInformation(Query queryObject) throws SolrSearcherException {
        if (queryObject != null && queryObject.getQuery() != null && !queryObject.getQuery().isEmpty()) {

            FacetMapping facetMapping = solrConverter.getFacetingInformation(queryObject);
            boolean correctFacets = true;
            // Each faceting group(species,types,keywords,compartments) is dependent from all selected filters of other faceting groups
            // This brings the risk of having filters that contradict each other. To avoid having selected facets that will cause problems
            // with the next filtering or querying it is necessary to remove those from the filtering process and repeat the faceting step
            if (queryObject.getSpecies() != null && facetMapping.getSpeciesFacet().getSelected().size() != queryObject.getSpecies().size()) {
                correctFacets = false;
                List<String> species = new ArrayList<>();
                for (FacetContainer container : facetMapping.getSpeciesFacet().getSelected()) {
                    species.add(container.getName());
                }
                queryObject.setSpecies(species);
            }
            if (queryObject.getTypes() != null && facetMapping.getTypeFacet().getSelected().size() != queryObject.getTypes().size()) {
                correctFacets = false;
                List<String> types = new ArrayList<>();
                for (FacetContainer container : facetMapping.getTypeFacet().getSelected()) {
                    types.add(container.getName());
                }
                queryObject.setTypes(types);
            }
            if (queryObject.getKeywords() != null && facetMapping.getKeywordFacet().getSelected().size() != queryObject.getKeywords().size()) {
                correctFacets = false;
                List<String> keywords = new ArrayList<>();
                for (FacetContainer container : facetMapping.getKeywordFacet().getSelected()) {
                    keywords.add(container.getName());
                }
                queryObject.setKeywords(keywords);
            }
            if (queryObject.getCompartment() != null && facetMapping.getCompartmentFacet().getSelected().size() != queryObject.getCompartment().size()) {
                correctFacets = false;
                List<String> compartments = new ArrayList<>();
                for (FacetContainer container : facetMapping.getCompartmentFacet().getSelected()) {
                    compartments.add(container.getName());
                }
                queryObject.setCompartment(compartments);
            }
            if (correctFacets) {
                return facetMapping;
            } else {
                return solrConverter.getFacetingInformation(queryObject);
            }
        }
        return null;
    }

    /**
     * Method for providing Faceting information for Species,Types,Keywords and Compartments
     *
     * @return FacetMapping
     * @throws SolrSearcherException
     */
    public FacetMapping getTotalFacetingInformation() throws SolrSearcherException {
        return solrConverter.getFacetingInformation();
    }

    /**
     * Method for providing autocomplete suggestions
     *
     * @param query Term (Snippet) you want to have auto-completed
     * @return List(String) of suggestions if solr is able to provide some
     * @throws SolrSearcherException
     */
    public List<String> getAutocompleteSuggestions(String query) throws SolrSearcherException {
        if (query != null && !query.isEmpty()) {
            return solrConverter.getAutocompleteSuggestions(query);
        }
        return null;
    }

    /**
     * Method for supplying spellcheck suggestions
     *
     * @param query Term you searched for
     * @return List(String) of suggestions if solr is able to provide some
     * @throws SolrSearcherException
     */
    public List<String> getSpellcheckSuggestions(String query) throws SolrSearcherException {
        if (query != null && !query.isEmpty()) {
            return solrConverter.getSpellcheckSuggestions(query);
        }
        return null;
    }

    public InteractorEntry getInteractionDetail(String query) throws SolrSearcherException {
        if (query != null && !query.isEmpty()) {
            InteractorEntry entry = solrConverter.getInteractionDetail(query);
            if (entry != null) {
                Collections.sort(entry.getInteractions());
                Collections.reverse(entry.getInteractions());

                return entry;
            }
        }
        return null;
    }


    /**
     * This Method gets multiple entries for a specific query while considering the filter information
     * the entries will be returned grouped into types and sorted by relevance (depending on the chosen solr properties)
     *
     * @param queryObject QueryObject (query, species, types, keywords, compartments, start, rows)
     *                    start specifies the starting point (offset) and rows the amount of entries returned in total
     * @return GroupedResult
     * @throws SolrSearcherException
     */
    public GroupedResult getEntries(Query queryObject, Boolean cluster) throws SolrSearcherException {
        if (cluster) {
            return solrConverter.getClusteredEntries(queryObject);
        } else {
            return solrConverter.getEntries(queryObject);
        }
    }

    /**
     * This Method is used for providing results for the SearchOnFire feature in the PathwaysOverview
     *
     * @param queryObject QueryObject (query, species, types, keywords, compartments, start, rows)
     *                    start specifies the starting point (offset) and rows the amount of entries returned in total
     * @return FireworksResult
     * @throws SolrSearcherException
     */
    public FireworksResult getFireworks(Query queryObject) throws SolrSearcherException {
        List<String> species = queryObject.getSpecies();
        if (species != null) {
            species.add("Entries without species"); //This will force to include the small molecules
        }
        return solrConverter.getFireworksResult(queryObject);
    }

    private void setPagingParameters(Query query, FacetMapping facetMapping, int rowCount, int page, boolean cluster) {
        Integer typeCount;
        if (query.getTypes() != null && !query.getTypes().isEmpty()) {
            typeCount = query.getTypes().size();
        } else {
            typeCount =  facetMapping.getTypeFacet().getAvailable().size();
        }
        if (typeCount != 0) {
            Integer rows = rowCount;
            if (cluster) {
                rows = rowCount / typeCount;
            }
            query.setStart(rows * (page - 1));
            query.setRows(rows);
        }
    }

    /**
     * Returns the highest result number for the different groups
     *
     * @param groupedResult result set
     * @return double highest result number
     */
    private double getHighestResultCount(GroupedResult groupedResult) {
        double max = 0;
        for (Result result : groupedResult.getResults()) {
            if (max < result.getEntriesCount()) {
                max = result.getEntriesCount();
            }
        }
        return max;
    }
}
