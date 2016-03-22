package org.reactome.server.tools.search.controller;

import org.apache.log4j.Logger;
import org.reactome.server.tools.search.domain.EnrichedEntry;
import org.reactome.server.tools.search.domain.FacetMapping;
import org.reactome.server.tools.search.domain.GroupedResult;
import org.reactome.server.tools.search.domain.Query;
import org.reactome.server.tools.search.exception.EnricherException;
import org.reactome.server.tools.search.exception.SearchServiceException;
import org.reactome.server.tools.search.exception.SolrSearcherException;
import org.reactome.server.tools.search.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;


/**
 * Converts a Solr QueryResponse into Objects provided by Project Models
 *
 * @author Florian Korninger (fkorn@ebi.ac.uk)
 * @version 1.0
 */
@Controller
@RequestMapping("/json")
class RestController {

    private static final Logger logger = Logger.getLogger(WebController.class);

    @Autowired
    private SearchService searchService;

    //    http://localhost:8080/search_service/detail/REACT_578.2
    @RequestMapping(value="/detail/{id:.*}", method = RequestMethod.GET)
    @ResponseBody
    public EnrichedEntry getEntry(@PathVariable String id) throws Exception {
         return searchService.getEntryById(id);
    }

    @RequestMapping(value="/detail/v{version}/{id:.*}", method = RequestMethod.GET)
    @ResponseBody
    public EnrichedEntry getEntryByVersion(@PathVariable String id,
                                                         @PathVariable Integer version) throws Exception {
        return searchService.getEntryById(version, id);
    }

    @RequestMapping(value = "/spellcheck", method = RequestMethod.GET)
    @ResponseBody
    public List<String> spellcheckSuggestions(@RequestParam ( required = true ) String query) throws SolrSearcherException {
        return searchService.getSpellcheckSuggestions(query);
    }

    // http://localhost:8080/json/suggester?query=apop
    @RequestMapping(value = "/suggest", method = RequestMethod.GET)
    @ResponseBody
    public List<String> suggesterSuggestions(@RequestParam ( required = true ) String query) throws SolrSearcherException {
        return searchService.getAutocompleteSuggestions(query);
    }

    //localhost:8080/json/facet
    @RequestMapping(value = "/facet", method = RequestMethod.GET)
    @ResponseBody
    public FacetMapping facet() throws SolrSearcherException {
            return searchService.getTotalFacetingInformation();
    }

    //http://localhost:8080/json/facet_query?query=apoptosis&species=%22Homo%20sapiens%22&species=%22Bos%20taurus%22
    @RequestMapping(value = "/facet_query", method = RequestMethod.GET)
    @ResponseBody
    public FacetMapping facet_type(@RequestParam ( required = true ) String query,
                                              @RequestParam ( required = false ) List<String> species,
                                              @RequestParam ( required = false ) List<String> types,
                                              @RequestParam ( required = false ) List<String> compartments,
                                              @RequestParam ( required = false ) List<String> keywords ) throws SolrSearcherException {
            Query queryObject = new Query(query, species, types, compartments, keywords);
            return searchService.getFacetingInformation(queryObject);
    }

    //http://localhost:8080/json/search?query=apoptosis&species=Homo%20sapiens,%20Bos%20taurus
    @RequestMapping(value = "/search", method = RequestMethod.GET)
    @ResponseBody
    public GroupedResult getResult (@RequestParam( required = true ) String query,
                                            @RequestParam ( required = false ) List<String> species,
                                            @RequestParam ( required = false ) List<String> types,
                                            @RequestParam ( required = false ) List<String> compartments,
                                            @RequestParam ( required = false ) List<String> keywords,
                                            @RequestParam ( required = false ) Boolean cluster,
                                            @RequestParam ( required = false ) Integer page,
                                            @RequestParam ( required = false ) Integer rows ) throws SolrSearcherException {
        Query queryObject = new Query(query, species,types,compartments,keywords,page, rows);
        return searchService.getEntries(queryObject, cluster);
    }

    /**
     * Overwrites the Global Exception Handler
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(SolrSearcherException.class)
    @ResponseBody
    ErrorInfo handleSolrException(HttpServletRequest req, SolrSearcherException e) {
        logger.error(e);
        return new ErrorInfo("SolrService Exception occurred", req.getRequestURL(), e);
    }
    /**
     * Overwrites the Global Exception Handler
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(SearchServiceException.class)
    @ResponseBody
    ErrorInfo handleServiceException(HttpServletRequest req, SearchServiceException e) {
        logger.error(e);
        return new ErrorInfo("SearchService Exception occurred", req.getRequestURL(), e);
    }
    /**
     * Overwrites the Global Exception Handler
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(EnricherException.class)
    @ResponseBody
    ErrorInfo handleEnricherException(HttpServletRequest req, EnricherException e) {
        logger.error(e);
        return new ErrorInfo("Enricher Exception occurred", req.getRequestURL(), e);
    }

    public void setSearchService(SearchService searchService) {
        this.searchService = searchService;
    }
}