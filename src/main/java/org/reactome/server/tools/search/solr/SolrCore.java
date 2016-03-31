package org.reactome.server.tools.search.solr;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.reactome.server.tools.search.domain.Query;
import org.reactome.server.tools.search.exception.SolrSearcherException;
import org.reactome.server.tools.search.util.PreemptiveAuthInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

/**
 * SolrCore converts Queries to SolrQueries and allows to retrieve Data from a solrClient
 * SolrCore returns a QueryResponse (SolrObject)
 *
 * @author Florian Korninger (fkorn@ebi.ac.uk)
 * @version 1.0
 */
@Component
class SolrCore {

    private final SolrClient solrClient;
    private final static Logger logger = Logger.getRootLogger();

    //    private final static String DEFAULT_REQUEST_HANDLER     =  "/select";
    private final static String SEARCH_REQUEST_HANDLER = "/search";
    private final static String CLUSTERED_REQUEST_HANDLER = "/browse";
    private final static String SUGGEST_REQUEST_HANDLER = "/suggest";
    private final static String EXISTS_REQUEST_HANDLER = "/exists";
    private final static String FACET_REQUEST_HANDLER = "/facet";
    private final static String TOTAL_FACET_REQUEST_HANDLER = "/facetall";
    private final static String SPELLCHECK_REQUEST_HANDLER = "/spellcheck";
    private final static String INTACT_REQUEST_HANDLER = "/intactdetail";

    private final static String SOLR_SPELLCHECK_QUERY = "spellcheck.q";
    private final static String SOLR_GROUP_OFFSET = "group.offset";
    private final static String SOLR_GROUP_LIMIT = "group.limit";

    private final static String SPECIES_FACET = "species_facet";
    private final static String TYPE_FACET = "type_facet";
    private final static String KEYWORD_FACET = "keywords_facet";
    private final static String COMPARTMENT_FACET = "compartment_facet";

    private final static String SPECIES_TAG = "{!tag=sf}";
    private final static String TYPE_TAG = "{!tag=tf}";
    private final static String KEYWORD_TAG = "{!tag=kf}";
    private final static String COMPARTMENT_TAG = "{!tag=cf}";

    //    private final static String DB_ID                       =  "dbId";
//    private final static String ST_ID                       =  "stId";
//    private final static String OLD_ST_ID                   =  "oldStId";
    private final static String ALL_FIELDS = "*:*";

    /**
     * Constructor for Dependency Injection
     * InitializeSolrClient
     * since Solr 4.2 Solr is using by default a poolingClientConnectionManager
     *
     * @param url solr URL
     */
    @Autowired
    public SolrCore(@Value("${solr_url}") String url,
                    @Value("${solr_user}") String user,
                    @Value("${solr_password}") String password) {

        logger.setLevel(Level.INFO);
        logger.addAppender(new ConsoleAppender(new PatternLayout("%-6r [%p] %c - %m%n")));

        if (user != null && !user.isEmpty() && password != null && !password.isEmpty()) {
            HttpClientBuilder builder = HttpClientBuilder.create().addInterceptorFirst(new PreemptiveAuthInterceptor());
            CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(user, password);
            credentialsProvider.setCredentials(AuthScope.ANY, credentials);
            HttpClient client = builder.setDefaultCredentialsProvider(credentialsProvider).build();
            solrClient = new HttpSolrClient(url, client);
        } else {
            solrClient = new HttpSolrClient(url);
        }
        logger.info("solrClient initialized");
    }

    /**
     * Query for checking if this specific String exists in the index
     *
     * @param query String of the query parameter given
     * @return true if there are results
     * @throws SolrSearcherException
     */
    boolean existsQuery(String query) throws SolrSearcherException {
        SolrQuery solrQuery = new SolrQuery();
        solrQuery.setRequestHandler(EXISTS_REQUEST_HANDLER);
        solrQuery.setQuery(query);

        QueryResponse queryResponse = querysolrClient(solrQuery);
        return queryResponse.getResults().getNumFound() > 0;
    }

    QueryResponse intactDetail(String query) throws SolrSearcherException {
        SolrQuery parameters = new SolrQuery();

        parameters.setRequestHandler(INTACT_REQUEST_HANDLER);
        parameters.setQuery(query);
        return querysolrClient(parameters);
    }

    /**
     * Converts all parameters of the given queryObject to Solr parameters and queries Solr Server
     * With this search handler the result will be clustered
     *
     * @param queryObject QueryObject (query, types, species, keywords, compartments, start, rows)
     * @return QueryResponse
     * @throws org.reactome.server.tools.search.exception.SolrSearcherException
     */
    QueryResponse searchCluster(Query queryObject) throws SolrSearcherException {
        SolrQuery parameters = new SolrQuery();

        parameters.setRequestHandler(CLUSTERED_REQUEST_HANDLER);
        parameters.addFilterQuery(getFilterString(queryObject.getSpecies(), SPECIES_FACET));
        parameters.addFilterQuery(getFilterString(queryObject.getTypes(), TYPE_FACET));
        parameters.addFilterQuery(getFilterString(queryObject.getCompartment(), COMPARTMENT_FACET));
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
     * @throws org.reactome.server.tools.search.exception.SolrSearcherException
     */
    QueryResponse search(Query queryObject) throws SolrSearcherException {
        SolrQuery parameters = new SolrQuery();

        parameters.setRequestHandler(SEARCH_REQUEST_HANDLER);
        parameters.addFilterQuery(getFilterString(queryObject.getSpecies(), SPECIES_FACET));
        parameters.addFilterQuery(getFilterString(queryObject.getTypes(), TYPE_FACET));
        parameters.addFilterQuery(getFilterString(queryObject.getCompartment(), COMPARTMENT_FACET));
        parameters.addFilterQuery(getFilterString(queryObject.getKeywords(), KEYWORD_FACET));

        if (queryObject.getStart() != null && queryObject.getRows() != null) {
            parameters.setStart(queryObject.getStart());
            parameters.setRows(queryObject.getRows());
        }
        parameters.setQuery(queryObject.getQuery());
        return querysolrClient(parameters);
    }

//    /**
//     * Queries Solr Server for a single given for given id
//     * @param id can be dbId of stId
//     * @return QueryResponse
//     * @throws org.reactome.server.tools.search.exception.SolrSearcherException
//     */
//    public QueryResponse searchById(String id) throws SolrSearcherException {
//        SolrQuery solrQuery = new SolrQuery();
//        solrQuery.setRequestHandler(DEFAULT_REQUEST_HANDLER);
//        solrQuery.setStart(0);
//        solrQuery.setRows(1);
//        solrQuery.setQuery(DB_ID + ":" + id + " OR " + ST_ID + ":" + id + " OR " + OLD_ST_ID + ":" + id);
//        return querysolrClient(solrQuery);
//    }

    /**
     * Method for autocompletion
     * Properties (eg number of suggestions returned are set in the solrconfig.xml)
     *
     * @param query String of the query parameter given
     * @return QueryResponse
     * @throws org.reactome.server.tools.search.exception.SolrSearcherException
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
     * @throws org.reactome.server.tools.search.exception.SolrSearcherException
     */
    QueryResponse getSpellcheckSuggestions(String query) throws SolrSearcherException {
        SolrQuery solrQuery = new SolrQuery();
        solrQuery.setRequestHandler(SPELLCHECK_REQUEST_HANDLER);
        solrQuery.set(SOLR_SPELLCHECK_QUERY, query);
//        if (query.toLowerCase().matches("^uniprot:[a-z0-9]+$") || query.toLowerCase().matches("^[a-z]+:[0-9]+$") ){
//            solrQuery.set("spellcheck.collate", false);
//        }
        return querysolrClient(solrQuery);
    }

    /**
     * Method gets Faceting Info considering Filter of other possible FacetFields
     * Tags are used to exclude filtering Parameters from the same Faceting Field
     *
     * @param queryObject QueryObject (query, types, species, keywords, compartments)
     * @return QueryResponse
     * @throws org.reactome.server.tools.search.exception.SolrSearcherException
     */
    QueryResponse getFacetingInformation(Query queryObject) throws SolrSearcherException {
        SolrQuery parameters = new SolrQuery();
        parameters.setRequestHandler(FACET_REQUEST_HANDLER);
        if (queryObject.getSpecies() != null) {
            parameters.addFilterQuery(SPECIES_TAG + getFilterString(queryObject.getSpecies(), SPECIES_FACET));
        }
        if (queryObject.getTypes() != null) {
            parameters.addFilterQuery(TYPE_TAG + getFilterString(queryObject.getTypes(), TYPE_FACET));
        }
        if (queryObject.getKeywords() != null) {
            parameters.addFilterQuery(KEYWORD_TAG + getFilterString(queryObject.getKeywords(), KEYWORD_FACET));
        }
        if (queryObject.getCompartment() != null) {
            parameters.addFilterQuery(COMPARTMENT_TAG + getFilterString(queryObject.getCompartment(), COMPARTMENT_FACET));
        }
        parameters.setQuery(queryObject.getQuery());
        return querysolrClient(parameters);
    }

    /**
     * Method gets all faceting information for the fields: species, types, compartments, keywords
     *
     * @return QueryResponse
     * @throws org.reactome.server.tools.search.exception.SolrSearcherException
     */
    QueryResponse getFacetingInformation() throws SolrSearcherException {
        SolrQuery parameters = new SolrQuery();
        parameters.setRequestHandler(TOTAL_FACET_REQUEST_HANDLER);
        parameters.setQuery(ALL_FIELDS);
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
        if (facet != null && !facet.isEmpty()) {
            if (fieldName != null && !fieldName.isEmpty()) {
                String filter = fieldName + ":(";
                for (int i = 0; i < facet.size() - 1; i++) {
                    filter += "\"" + facet.get(i) + "\" OR ";
                }
                filter += "\"" + facet.get(facet.size() - 1) + "\")";
                return filter;
            }
        }
        return null;
    }

    /**
     * executes a Query
     *
     * @param query SolrQuery Object
     * @return QueryResponse
     * @throws org.reactome.server.tools.search.exception.SolrSearcherException
     */
    private QueryResponse querysolrClient(SolrQuery query) throws SolrSearcherException {
        try {
            return solrClient.query(query);
        } catch (IOException | SolrServerException e) {
            logger.error("Solr exception occurred with query: " + query, e);
            throw new SolrSearcherException("Solr exception occurred with query: " + query, e);
        }
    }

//    /**
//     * Shutdown only closes the connection to Solr
//     * never used
//     */
//    private void closesolrClient() {
//        solrClient.shutdown();
//        logger.info("solrClient shutdown");
//    }
}