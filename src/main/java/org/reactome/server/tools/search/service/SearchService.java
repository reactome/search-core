package org.reactome.server.tools.search.service;

import org.reactome.server.tools.interactors.exception.InvalidInteractionResourceException;
import org.reactome.server.tools.interactors.model.Interaction;
import org.reactome.server.tools.interactors.service.InteractionService;
import org.reactome.server.tools.interactors.util.InteractorConstant;
import org.reactome.server.tools.search.database.Enricher;
import org.reactome.server.tools.search.database.IEnricher;
import org.reactome.server.tools.search.domain.*;
import org.reactome.server.tools.search.exception.EnricherException;
import org.reactome.server.tools.search.exception.SearchServiceException;
import org.reactome.server.tools.search.exception.SolrSearcherException;
import org.reactome.server.tools.search.solr.ISolrConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * Search Service acts as api between the Controller and Solr / Database
 *
 * @author Florian Korninger (fkorn@ebi.ac.uk)
 * @version 1.0
 */
@Service
public class SearchService {
    private static final Logger logger = LoggerFactory.getLogger(SearchService.class);

    @Autowired
    private ISolrConverter solrConverter;

    @Value("${database_host}")
    private  String host;
    @Value("${database_name}")
    private  String database;
    @Value("${database_currentDatabase}")
    private  String currentDatabase;
    @Value("${database_user}")
    private  String user;
    @Value("${database_password}")
    private  String password;
    @Value("${database_port}")
    private  Integer port;

    @Autowired
//    @Qualifier(value = "interactionService")
    private InteractionService interactionService;


//    @Autowired(required = false)
//    public void setInteractionService(InteractionService interactionService) {
//        this.interactionService = interactionService;
//    }



    /**
     * Constructor for Spring Dependency Injection and loading MavenProperties
     *
     * @throws SearchServiceException
     */
    public SearchService(){}

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
     * @param query Term (Snippet) you want to have autocompleted
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
            Collections.sort(entry.getInteractions());
            Collections.reverse(entry.getInteractions());

            return entry;
        }
        return null;
    }


    /**
     * Returns one specific Entry by DbId
     *
     * @param id StId or DbId
     * @return Entry Object
     */
    public EnrichedEntry getEntryById(String id) throws EnricherException, SolrSearcherException {
        if (id != null && !id.isEmpty()) {
            IEnricher enricher = new Enricher(host, currentDatabase, user, password, port);
            EnrichedEntry enrichedEntry = enricher.enrichEntry(id);

            ReferenceEntity referenceEntity = enrichedEntry.getReferenceEntity();
            if(referenceEntity != null) {
                String acc = referenceEntity.getReferenceIdentifier();
                if (acc != null) {
                    try {
                        List<Interaction> interactionsList = interactionService.getInteractions(acc, InteractorConstant.STATIC);

                        enrichedEntry.setInteractionList(interactionsList);

                    } catch (InvalidInteractionResourceException | SQLException e) {
                        logger.error("Error retrieving interactions from Database");
                    }
                }
            }
            return enrichedEntry;
        }

        return null;
    }

    /**
     * Returns one specific Entry by DbId
     *
     * @param id StId or DbId
     * @return Entry Object
     */
    public EnrichedEntry getEntryById(Integer version, String id) throws EnricherException, SolrSearcherException {
        if (id != null && !id.isEmpty()) {
            IEnricher enricher = new Enricher(host, database + version, user, password, port);
            return enricher.enrichEntry(id);
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


}
