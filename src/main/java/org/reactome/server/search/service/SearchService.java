package org.reactome.server.search.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.reactome.server.search.domain.*;
import org.reactome.server.search.exception.SolrSearcherException;
import org.reactome.server.search.solr.SolrConverter;
import org.reactome.server.search.util.ReportEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringWriter;
import java.net.ConnectException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.reactome.server.search.util.ReportInformationEnum.*;

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

    private final SolrConverter solrConverter;

    @Value("${report.user:default}")
    private String reportUser;

    @Value("${report.password:default}")
    private String reportPassword;

    public SearchService(@Autowired SolrConverter solrConverter) {
        this.solrConverter = solrConverter;
    }

    /**
     * Method for testing if a connection to Solr can be established
     *
     * @return true if status is ok
     */
    public Boolean ping() {
        return solrConverter.ping();
    }

    /**
     * This method is a simple aggregation of service methods used in the Content project
     *
     * @param query    QueryObject
     * @param rowCount number of rows displayed in one page
     * @param page     page number
     * @param grouped  grouped or not grouped result
     * @return Grouped result
     */
    public SearchResult getSearchResult(Query query, int rowCount, int page, boolean grouped) throws SolrSearcherException {
        FacetMapping facetMapping = getFacetingInformation(query);
        if (facetMapping == null || facetMapping.getTotalNumFount() < 1) {
            query = new Query.Builder(query.getQuery()).keepOriginalQuery(query.getOriginalQuery()).withReportInfo(query.getReportInfo()).build();
            facetMapping = getFacetingInformation(query);
        }
        if (facetMapping != null && facetMapping.getTotalNumFount() == 0) {
            query.setParserType(ParserType.DISMAX);
            facetMapping = getFacetingInformation(query);
        }
        if (facetMapping != null && facetMapping.getTotalNumFount() > 0) {
            setPagingParameters(query, facetMapping, rowCount, page, grouped);
            GroupedResult groupedResult = getEntries(query, grouped);
            return new SearchResult(facetMapping, groupedResult, getHighestResultCount(groupedResult), query.getRows());
        }

        // No results found, check for targets and incorporate them in the SearchResult if present.
        Set<TargetResult> targets = getTargets(query);
        doAsyncReport(query, targets);

        return targets.isEmpty() ? null : new SearchResult(targets);
    }

    /**
     * This Method gets multiple entries for a specific query while considering the filter information
     * the entries will be returned grouped into types and sorted by relevance (depending on the chosen solr properties)
     *
     * @param queryObject QueryObject (query, species, types, keywords, compartments, start, rows)
     *                    start specifies the starting point (offset) and rows the amount of entries returned in total
     * @return GroupedResult
     */
    public GroupedResult getEntries(Query queryObject, Boolean grouped) throws SolrSearcherException {
        grouped = grouped == null ? true : grouped;
        GroupedResult ret = grouped ? solrConverter.getGroupedEntries(queryObject) : solrConverter.getEntries(queryObject);

        if (ret != null && ret.getRowCount() == 0) {
            Set<TargetResult> targetResults = solrConverter.getTargets(queryObject);
            if (!targetResults.isEmpty()) {
                ret.setTargetResults(targetResults);
            }
            doAsyncReport(queryObject, targetResults);
        }
        return ret;
    }

    /**
     * Gets Faceting information for a specific query + filters.
     * This Method will query solr once again if the number of selected filters and found facets differ
     * (this will help preventing false faceting information when filter are contradictory to each other)
     *
     * @param queryObject query and filter (species types keywords compartments)
     * @return FacetMapping
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
            if (queryObject.getCompartments() != null && facetMapping.getCompartmentFacet().getSelected().size() != queryObject.getCompartments().size()) {
                correctFacets = false;
                List<String> compartments = new ArrayList<>();
                for (FacetContainer container : facetMapping.getCompartmentFacet().getSelected()) {
                    compartments.add(container.getName());
                }
                queryObject.setCompartments(compartments);
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
     */
    public FacetMapping getTotalFacetingInformation() throws SolrSearcherException {
        return solrConverter.getFacetingInformation();
    }

    /**
     * Method for providing autocomplete suggestions
     *
     * @param query Term (Snippet) you want to have auto-completed
     * @return List(String) of suggestions if solr is able to provide some
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
     */
    public List<String> getSpellcheckSuggestions(String query) throws SolrSearcherException {
        if (query != null && !query.isEmpty()) {
            return solrConverter.getSpellcheckSuggestions(query);
        }
        return null;
    }

    /**
     * This Method is used for providing results for the SearchOnFire feature in the PathwaysOverview
     *
     * @param queryObject QueryObject (query, species, types, keywords, compartments, start, rows)
     *                    start specifies the starting point (offset) and rows the amount of entries returned in total
     * @return FireworksResult
     */
    public FireworksResult getFireworks(Query queryObject) throws SolrSearcherException {
        if (queryObject.getSpecies() != null) {
            queryObject.getSpecies().add("Entries without species");
        }

        FireworksResult ret = solrConverter.getFireworksResult(queryObject);
        if (ret != null && ret.getFound() == 0) {
            Set<TargetResult> targetResults = solrConverter.getTargets(queryObject);
            if (targetResults != null && !targetResults.isEmpty()) {
                doAsyncTargetReport(queryObject, targetResults);
                ret.setTargetResults(targetResults);
            } else {
                doAsyncSearchReport(queryObject);
            }
        }
        return ret;
    }

    /**
     * Getting diagram occurrences, diagrams and subpathways multivalue fields have been added to the document.
     * Diagrams hold where the entity is present.
     * Occurrences hold a "isInDiagram:occurrences"
     * <p>
     * This is a two steps search:
     * - Submit term and diagram and retrieve a list of documents (getDiagramResult)
     * - Retrieve list of occurrences (getDiagramOccurrencesResults)
     */
    public DiagramResult getDiagrams(Query queryObject) throws SolrSearcherException {
        return solrConverter.getDiagrams(queryObject);
    }

    /**
     * This is stored in the occurrences multivalue field having diagram:isInDiagram:[list of subpathways occurrences]
     *
     * @param queryObject - has the stId of the element we are searching and the diagram to filter
     */
    public DiagramOccurrencesResult getDiagramOccurrencesResult(Query queryObject) throws SolrSearcherException {
        return solrConverter.getDiagramOccurrencesResult(queryObject);
    }

    /**
     * This is stored in the occurrences multivalue field having diagram:isInDiagram:[list of subpathways occurrences]
     *
     * @param queryObject - has the term we are searching to flag the corresponding element and the diagram to filter
     */
    public List<DiagramOccurrencesResult> getDiagramFlagging(Query queryObject) throws SolrSearcherException {
        return solrConverter.getDiagramFlagging(queryObject);
    }

    /**
     * Return a list of StableIds to be flagged in the Fireworks and the diagram that it might interacts with
     */
    public FireworksOccurrencesResult fireworksFlagging(Query queryObject) throws SolrSearcherException {
        return solrConverter.fireworksFlagging(queryObject);
    }

    /**
     * Retrieve a summary of results in the given Diagram (in query) and in other diagrams.
     * Facets are provided too.
     */
    public DiagramSearchSummary getDiagramSearchSummary(Query queryObject) throws SolrSearcherException {
        // Don't get any entry. We only need to count.
        queryObject.setStart(0);
        queryObject.setRows(0);
        if (queryObject.getSpecies() != null) {
            queryObject.getSpecies().add("Entries without species");
        }
        DiagramResult diagrams = solrConverter.getDiagrams(queryObject);
        FireworksResult fireworks = solrConverter.getFireworksResult(queryObject);
        return new DiagramSearchSummary(diagrams, fireworks);
    }

    /**
     * @return FacetMapping
     */
    public FacetMapping getIconFacetingInformation() throws SolrSearcherException {
        return solrConverter.getIconFacetingInformation();
    }

    public Result getIconsResult(Query query, int rows, int page) throws SolrSearcherException {
        query.setStart(rows * (page - 1));
        query.setRows(rows);
        return solrConverter.getIconsResult(query);
    }

    public Entry getIcon(Query query) throws SolrSearcherException {
        return solrConverter.getIcon(query);
    }

    /**
     * Return a list of Proteins that are in our scope for curation
     */
    public Set<TargetResult> getTargets(Query queryObject) {
        return solrConverter.getTargets(queryObject);
    }

    private void setPagingParameters(Query query, FacetMapping facetMapping, int rowCount, int page, boolean cluster) {
        int typeCount;
        if (query.getStart() != null && query.getRows() != null) return;
        if (query.getTypes() != null && !query.getTypes().isEmpty()) {
            typeCount = query.getTypes().size();
        } else {
            typeCount = facetMapping.getTypeFacet().getAvailable().size();
        }
        if (typeCount != 0) {
            int rows = rowCount;
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

    /**
     * Returns all icons
     */
    public List<Entry> getAllIcons() throws SolrSearcherException {
        List<Entry> rtn = null;
        List<String> types = Collections.singletonList("Icon");

        // Simple query to get number of matches
        Query query = new Query.Builder("*:*").withTypes(types).start(0).numberOfRows(0).build();
        GroupedResult results = solrConverter.getEntries(query);
        int rows = results.getNumberOfMatches();

        // Query all the icons
        query = new Query.Builder("*:*").withTypes(types).start(0).numberOfRows(rows).build();
        results = solrConverter.getEntries(query);

        if (results != null && results.getResults() != null) {
            rtn = results.getResults().get(0).getEntries();
        }

        return rtn;
    }

    private void doAsyncReport(Query queryObject, Set<TargetResult> targetResults) {
        if (!targetResults.isEmpty()) {
            doAsyncTargetReport(queryObject, targetResults);
        } else {
            doAsyncSearchReport(queryObject);
        }
    }

    /**
     * It covers two use cases:
     * 1- Not Found and Target
     * 2- Not Found and not a Target
     */
    private void doAsyncTargetReport(Query queryObject, Set<TargetResult> targetResults) {
        Set<TargetResult> targetsOnly = targetResults.stream().filter(TargetResult::isTarget).collect(Collectors.toSet());
        new Thread(() -> report("targets", queryObject, targetsOnly), "ReportTargetThread").start();

        // In the same search we might have one target and term(s) not found. Targets are done in the line above
        // and the others will be the new QueryObject just to be able to reuse the report method.
        Set<String> targetsNotFound = targetResults.stream().filter(t -> !t.isTarget()).map(TargetResult::getTerm).collect(Collectors.toSet());
        if (!targetsNotFound.isEmpty()) {
            queryObject.setQuery(String.join(" ", targetsNotFound));
            new Thread(() -> report("notfound", queryObject, null), "ReportNotFoundTargetThread").start();
        }
    }

    private void doAsyncSearchReport(Query queryObject) {
        new Thread(() -> report("notfound", queryObject, null), "ReportNotFoundThread").start();
    }

    private void report(String requestMapping, Query queryObject, Set<TargetResult> targetResults) {
        if (queryObject == null || queryObject.getReportInfo() == null) return;

        try {
            CredentialsProvider provider = new BasicCredentialsProvider();
            UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(reportUser, reportPassword);
            provider.setCredentials(AuthScope.ANY, credentials);
            CloseableHttpClient client = HttpClients.custom().setDefaultCredentialsProvider(provider).build();
            URIBuilder uriBuilder = new URIBuilder("http://localhost:8080/report/search/" + requestMapping);
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("releaseNumber", queryObject.getReportInfo().get(RELEASEVERSION.getDesc())));
            params.add(new BasicNameValuePair("ip", queryObject.getReportInfo().get(IPADDRESS.getDesc())));
            params.add(new BasicNameValuePair("agent", queryObject.getReportInfo().get(USERAGENT.getDesc())));
            uriBuilder.addParameters(params);

            HttpPost httpPost = new HttpPost(uriBuilder.toString());
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");

            ObjectMapper objectMapper = new ObjectMapper();
            if (targetResults != null && !targetResults.isEmpty()) {
                List<ReportEntity> res = targetResults.stream().map(tr -> new ReportEntity(tr.getTerm(), tr.getResource())).collect(Collectors.toList());
                StringWriter json = new StringWriter();
                objectMapper.writeValue(json, res);

                StringEntity entity = new StringEntity(json.toString());
                httpPost.setEntity(entity);
            } else {
                String json = objectMapper.writeValueAsString(new ReportEntity(queryObject.getOriginalQuery(), ""));
                StringEntity entity = new StringEntity(json);
                httpPost.setEntity(entity);
            }

            CloseableHttpResponse response = client.execute(httpPost);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200) {
                logger.error("[REP001] The url {} returned the code {} and the report hasn't been created.", uriBuilder.toString(), statusCode);
            }
            client.close();
        } catch (ConnectException e) {
            logger.error("[REP002] Report service is unavailable");
        } catch (IOException | URISyntaxException e) {
            logger.error("[REP003] An unexpected error has occurred when saving a report");
        }
    }
}
