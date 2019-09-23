package org.reactome.server.search.solr;

import org.apache.commons.lang.StringUtils;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.request.SolrPing;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.SolrPingResponse;
import org.apache.solr.common.SolrException;
import org.reactome.server.search.domain.Query;
import org.reactome.server.search.exception.SolrSearcherException;
import org.reactome.server.search.util.PreemptiveAuthInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * SolrCore converts Queries to SolrQueries and allows to retrieve Data from a solrClient
 * SolrCore returns a QueryResponse (SolrObject)
 *
 * @author Florian Korninger (fkorn@ebi.ac.uk)
 * @author Guilherme Viter (gviteri@ebi.ac.uk)
 * @author Antonio Fabregat (fabregat@ebi.ac.uk)
 * @version 1.0
 */
@Component
class SolrCore {

    private final static Logger logger = LoggerFactory.getLogger("");

    private final SolrClient solrClient;
    private final String solrCore;

    private final static String SEARCH_REQUEST_HANDLER = "/search";
    private final static String CLUSTERED_REQUEST_HANDLER = "/browse";
    private final static String SUGGEST_REQUEST_HANDLER = "/suggest";
    private final static String EXISTS_REQUEST_HANDLER = "/exists";
    private final static String FACET_REQUEST_HANDLER = "/facet";
    private final static String TOTAL_FACET_REQUEST_HANDLER = "/facetall";
    private final static String SPELLCHECK_REQUEST_HANDLER = "/spellcheck";
    private final static String FIREWORKS_REQUEST_HANDLER = "/fireworks";
    private final static String FIREWORKS_FLAGGING_REQUEST_HANDLER = "/fireworksFlagging";
    private final static String DIAGRAM_REQUEST_HANDLER = "/diagrams";
    private final static String DIAGRAM_OCCURRENCES_REQUEST_HANDLER = "/diagramOccurrences";
    private final static String ICON_FACET_HANDLER = "/iconFacet";
    private final static String DIAGRAM_FLAG_REQUEST_HANDLER = "/diagramFlagging";

    private final static String SOLR_SPELLCHECK_QUERY = "spellcheck.q";
    private final static String SOLR_GROUP_OFFSET = "group.offset";
    private final static String SOLR_GROUP_LIMIT = "group.limit";

    private final static String SPECIES_FACET = "species_facet";
    private final static String TYPE_FACET = "type_facet";
    private final static String KEYWORD_FACET = "keywords_facet";
    private final static String COMPARTMENT_FACET = "compartment_facet";
    private final static String FIREWORK_SPECIES = "fireworksSpecies";
    private final static String DIAGRAMS = "diagrams";
    private final static String DIAGRAM_OCCURRENCES = "occurrences";
    private final static String ST_ID = "stId";
    private final static String LLPS = "llps";

    private final static String SPECIES_TAG = "{!tag=sf}";
    private final static String TYPE_TAG = "{!tag=tf}";
    private final static String KEYWORD_TAG = "{!tag=kf}";
    private final static String COMPARTMENT_TAG = "{!tag=cf}";
    private final static String ICON_TYPE_QUERY = "{!term f=type}icon";

    private final static String ALL_FIELDS = "*:*";

    private final static String TARGET_CORE = "target";

    /**
     * Constructor for Dependency Injection
     * InitializeSolrClient
     * since Solr 4.2 Solr is using by default a poolingClientConnectionManager
     *
     * @param url solr URL
     */
    @Autowired
    public SolrCore(@Value("${solr.host}") String url,
                    @Value("${solr.core}") String solrCore,
                    @Value("${solr.user}") String user,
                    @Value("${solr.password}") String password) {
        this.solrCore = solrCore;
        if (user != null && !user.isEmpty() && password != null && !password.isEmpty()) {
            HttpClientBuilder builder = HttpClientBuilder.create().addInterceptorFirst(new PreemptiveAuthInterceptor());
            CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(user, password);
            credentialsProvider.setCredentials(AuthScope.ANY, credentials);
            HttpClient client = builder.setDefaultCredentialsProvider(credentialsProvider).build();
            solrClient = new HttpSolrClient.Builder(url).withHttpClient(client).build();
        } else {
            solrClient = new HttpSolrClient.Builder(url).build();
        }
        logger.info("solrClient initialized");
    }

    /**
     * Method for testing if a connection to Solr can be established
     * @return true if status is ok
     */
    boolean ping() {
        try {
            SolrPing ping = new SolrPing();
            SolrPingResponse rsp = ping.process(solrClient, solrCore);
            int status = rsp.getStatus();
            if (status == 0) return true;
        } catch (Exception e) {
            logger.error("Connection to Solr could not be established");
        }
        return false;
    }

    /**
     * Query for checking if this specific String exists in the index
     *
     * @param query String of the query parameter given
     * @return true if there are results
     */
    boolean existsQuery(String query) throws SolrSearcherException {
        SolrQuery solrQuery = new SolrQuery();
        solrQuery.setRequestHandler(EXISTS_REQUEST_HANDLER);
        solrQuery.setQuery(query);

        QueryResponse queryResponse = querysolrClient(solrQuery);
        return queryResponse.getResults().getNumFound() > 0;
    }

    /**
     * Converts all parameters of the given queryObject to Solr parameters and queries Solr Server
     * With this search handler the result will be clustered
     *
     * @param queryObject QueryObject (query, types, species, keywords, compartments, start, rows)
     * @return QueryResponse
     */
    QueryResponse searchCluster(Query queryObject) throws SolrSearcherException {
        SolrQuery parameters = new SolrQuery();

        parameters.setRequestHandler(CLUSTERED_REQUEST_HANDLER);
        parameters.addFilterQuery(getFilterString(queryObject.getSpecies(), SPECIES_FACET));
        parameters.addFilterQuery(getFilterString(queryObject.getTypes(), TYPE_FACET));
        parameters.addFilterQuery(getFilterString(queryObject.getCompartments(), COMPARTMENT_FACET));
        parameters.addFilterQuery(getFilterString(queryObject.getKeywords(), KEYWORD_FACET));

        if (queryObject.getStart() != null && queryObject.getRows() != null) {
            parameters.set(SOLR_GROUP_OFFSET, queryObject.getStart());
            parameters.set(SOLR_GROUP_LIMIT, queryObject.getRows());
        }
        parameters.setQuery(queryObject.getQuery());
        return querysolrClient(parameters);
    }

    /**
     * Converts all parameters of the given queryObject to Solr parameters and queries Solr Server
     * With this search handler the result will not be clustered
     *
     * @param queryObject QueryObject (query, types, species, keywords, compartments, start, rows)
     * @return QueryResponse
     */
    QueryResponse search(Query queryObject) throws SolrSearcherException {
        SolrQuery parameters = new SolrQuery();

        parameters.setRequestHandler(SEARCH_REQUEST_HANDLER);
        parameters.addFilterQuery(getFilterString(queryObject.getSpecies(), SPECIES_FACET));
        parameters.addFilterQuery(getFilterString(queryObject.getTypes(), TYPE_FACET));
        parameters.addFilterQuery(getFilterString(queryObject.getCompartments(), COMPARTMENT_FACET));
        parameters.addFilterQuery(getFilterString(queryObject.getKeywords(), KEYWORD_FACET));

        if (queryObject.getStart() != null && queryObject.getRows() != null) {
            parameters.setStart(queryObject.getStart());
            parameters.setRows(queryObject.getRows());
        }
        parameters.setQuery(queryObject.getQuery());
        return querysolrClient(parameters);
    }

    /**
     * Method for autocompletion
     * Properties (eg number of suggestions returned are set in the solrconfig.xml)
     *
     * @param query String of the query parameter given
     * @return QueryResponse
     */
    QueryResponse getAutocompleteSuggestions(String query) throws SolrSearcherException {
        SolrQuery solrQuery = new SolrQuery();
        solrQuery.setRequestHandler(SUGGEST_REQUEST_HANDLER);
        solrQuery.set(SOLR_SPELLCHECK_QUERY, query);
        return querysolrClient(solrQuery);
    }

    /**
     * Method for spellcheck and suggestions
     * Properties (eg number of suggestions returned are set in the solrconfig.xml)
     *
     * @param query String of the query parameter given
     * @return QueryResponse
     */
    QueryResponse getSpellcheckSuggestions(String query) throws SolrSearcherException {
        SolrQuery solrQuery = new SolrQuery();
        solrQuery.setRequestHandler(SPELLCHECK_REQUEST_HANDLER);
        solrQuery.set(SOLR_SPELLCHECK_QUERY, query);
        return querysolrClient(solrQuery);
    }

    /**
     * Method gets Faceting Info considering Filter of other possible FacetFields
     * Tags are used to exclude filtering Parameters from the same Faceting Field
     *
     * @param queryObject QueryObject (query, types, species, keywords, compartments)
     * @return QueryResponse
     */
    QueryResponse getFacetingInformation(Query queryObject) throws SolrSearcherException {
        SolrQuery parameters = new SolrQuery();
        parameters.setRequestHandler(FACET_REQUEST_HANDLER);
        if (queryObject.getSpecies() != null && !queryObject.getSpecies().isEmpty()) {
            parameters.addFilterQuery(SPECIES_TAG + getFilterString(queryObject.getSpecies(), SPECIES_FACET));
        }
        if (queryObject.getTypes() != null && !queryObject.getTypes().isEmpty()) {
            parameters.addFilterQuery(TYPE_TAG + getFilterString(queryObject.getTypes(), TYPE_FACET));
        }
        if (queryObject.getKeywords() != null && !queryObject.getKeywords().isEmpty()) {
            parameters.addFilterQuery(KEYWORD_TAG + getFilterString(queryObject.getKeywords(), KEYWORD_FACET));
        }
        if (queryObject.getCompartments() != null && !queryObject.getCompartments().isEmpty()) {
            parameters.addFilterQuery(COMPARTMENT_TAG + getFilterString(queryObject.getCompartments(), COMPARTMENT_FACET));
        }
        parameters.setQuery(queryObject.getQuery());
        return querysolrClient(parameters);
    }

    /**
     * Method gets all faceting information for the fields: species, types, compartments, keywords
     *
     * @return QueryResponse
     */
    QueryResponse getFacetingInformation() throws SolrSearcherException {
        SolrQuery parameters = new SolrQuery();
        parameters.setRequestHandler(TOTAL_FACET_REQUEST_HANDLER);
        parameters.setQuery(ALL_FIELDS);
        return querysolrClient(parameters);
    }

    QueryResponse getFireworksResult(Query queryObject) throws SolrSearcherException {
        SolrQuery parameters = new SolrQuery();
        parameters.setRequestHandler(FIREWORKS_REQUEST_HANDLER);

        parameters.addFilterQuery(getFilterString(queryObject.getSpecies(), FIREWORK_SPECIES));
        if (queryObject.getTypes() != null && !queryObject.getTypes().isEmpty()) {
            parameters.addFilterQuery(TYPE_TAG + getFilterString(queryObject.getTypes(), TYPE_FACET));
        }

        parameters.setStart(queryObject.getStart());
        parameters.setRows(queryObject.getRows());
        parameters.setQuery(queryObject.getQuery());

        return querysolrClient(parameters);
    }

    /**
     * Getting all documents of a given term filtering by the Diagram stId where the user is
     * @return QueryResponse
     */
    QueryResponse getDiagrams(Query queryObject) throws SolrSearcherException {
        SolrQuery parameters = new SolrQuery();
        parameters.setRequestHandler(DIAGRAM_REQUEST_HANDLER);
        if (queryObject.getSpecies() != null && !queryObject.getSpecies().isEmpty()) {
            parameters.addFilterQuery(getFilterString(queryObject.getSpecies(), SPECIES_FACET));
        }
        if (queryObject.getTypes() != null && !queryObject.getTypes().isEmpty()) {
            parameters.addFilterQuery(TYPE_TAG + getFilterString(queryObject.getTypes(), TYPE_FACET));
        }
        parameters.addFilterQuery(DIAGRAMS + ":" + queryObject.getFilterQuery());
        parameters.setStart(queryObject.getStart());
        parameters.setRows(queryObject.getRows());
        parameters.setQuery(queryObject.getQuery());
        return querysolrClient(parameters);
    }

    /**
     * Getting document based on the given stId (entry selected by the user).
     * Only subpathways field is returned.
     */
    QueryResponse getDiagramOccurrences(Query queryObject) throws SolrSearcherException {
        SolrQuery parameters = new SolrQuery();
        parameters.setRequestHandler(DIAGRAM_OCCURRENCES_REQUEST_HANDLER);
        parameters.setQuery(queryObject.getQuery());
        parameters.setFields(DIAGRAM_OCCURRENCES); // solr response will contain only DIAGRAM_OCCURRENCES.
        return querysolrClient(parameters);
    }

    /**
     * Getting document based on the given stId (entry selected by the user).
     * Only subpathways field is returned.
     */
    QueryResponse getDiagramFlagging(Query queryObject) throws SolrSearcherException {
        SolrQuery parameters = new SolrQuery();
        parameters.setRequestHandler(DIAGRAM_FLAG_REQUEST_HANDLER);
        parameters.setQuery(queryObject.getQuery() + " AND occurrences:" + queryObject.getFilterQuery() + "*");
        parameters.setFields(DIAGRAM_OCCURRENCES, ST_ID); // solr response will contain only DIAGRAM_OCCURRENCES and ST_ID.
        //If the term returns more than 100, it is not accurate enough. Only first 100 are taken into account for flagging
        parameters.setRows(100);
        return querysolrClient(parameters);
    }

    QueryResponse fireworksFlagging(Query queryObject) throws SolrSearcherException {
        SolrQuery parameters = new SolrQuery();
        parameters.setRequestHandler(FIREWORKS_FLAGGING_REQUEST_HANDLER);
        parameters.setFields(LLPS, DIAGRAM_OCCURRENCES);
        parameters.setQuery(queryObject.getQuery());

        if (queryObject.getSpecies() == null) queryObject.setSpecies(new ArrayList<>());
        if (queryObject.getSpecies().isEmpty()) queryObject.getSpecies().add("Homo sapiens");
        if (!queryObject.getSpecies().contains("Entries without species")) queryObject.getSpecies().add("Entries without species");

        if (queryObject.getSpecies() != null && !queryObject.getSpecies().isEmpty()) {
            parameters.addFilterQuery(getFilterString(queryObject.getSpecies(), SPECIES_FACET));
        }
        //If the term returns more than 100, it is not accurate enough. Only first 100 are taken into account for flagging
        parameters.setRows(100);
        return querysolrClient(parameters);
    }

    /**
     * Getting document based on the given stId (entry selected by the user).
     * Only subpathways field is returned.
     */
    QueryResponse getTargets(Query queryObject) {
        SolrQuery parameters = new SolrQuery();
        parameters.setRequestHandler(SEARCH_REQUEST_HANDLER);
        parameters.setQuery(queryObject.getQuery());
        try {
            return solrClient.query(TARGET_CORE, parameters);
        } catch (IOException | SolrServerException | SolrException e) {
            // nothing here
        }
        return null;
    }

    QueryResponse getIconFacetingInformation() throws SolrSearcherException {
        SolrQuery parameters = new SolrQuery();
        parameters.setRequestHandler(ICON_FACET_HANDLER);
        parameters.setQuery(ICON_TYPE_QUERY);
        return querysolrClient(parameters);
    }

    QueryResponse getIconsResult(Query queryObject) throws SolrSearcherException {
        SolrQuery parameters = new SolrQuery();
        parameters.setRequestHandler(SEARCH_REQUEST_HANDLER);
        parameters.setSort("iconName_sort", SolrQuery.ORDER.asc);
        if (queryObject.getStart() != null && queryObject.getRows() != null) {
            parameters.setStart(queryObject.getStart());
            parameters.setRows(queryObject.getRows());
        }

        parameters.setQuery(queryObject.getQuery());
        return querysolrClient(parameters);
    }

    QueryResponse getIcon(Query queryObject) throws SolrSearcherException {
        SolrQuery parameters = new SolrQuery();
        parameters.setRequestHandler(SEARCH_REQUEST_HANDLER);
        parameters.setQuery("stId:" + queryObject.getQuery());
        parameters.setFilterQueries(ICON_TYPE_QUERY);
        parameters.set("mm", "100%"); // min match - one field or another is fine
        return querysolrClient(parameters);
    }

    /**
     * Helper Method to construct the filter that is sent to Solr
     *
     * @param facet     list of selected faceting parameters
     * @param fieldName name of the faceting field
     * @return filterQuery ready to get sent to solr
     */
    private String getFilterString(List<String> facet, String fieldName) {
        if (facet != null) {
            facet.removeAll(Collections.singletonList(""));
            facet.removeAll(Collections.singletonList(null));
            if(!facet.isEmpty() && fieldName != null && !fieldName.isEmpty()) {
                return fieldName + ":(\"" + StringUtils.join(facet, "\" OR \"") + "\")";
            }
        }
        return "";
    }

    /**
     * executes a Query
     *
     * @param query SolrQuery Object
     * @return QueryResponse
     */
    private QueryResponse querysolrClient(SolrQuery query) throws SolrSearcherException {
        try {
            return solrClient.query(solrCore, query);
        } catch (IOException | SolrServerException e) {
            logger.error("Solr exception occurred with query: " + query, e);
            throw new SolrSearcherException("Solr exception occurred with query: " + query, e);
        }
    }
}