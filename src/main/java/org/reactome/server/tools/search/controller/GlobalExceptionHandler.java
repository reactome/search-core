package org.reactome.server.tools.search.controller;

import org.reactome.server.tools.search.exception.EnricherException;
import org.reactome.server.tools.search.exception.SearchServiceException;
import org.reactome.server.tools.search.exception.SolrSearcherException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * Global exception handler controller
 * This controller will deal with all exceptions thrown by the other controllers if they don't treat them individually
 *
 * @author Florian Korninger (fkorn@ebi.ac.uk)
 * @version 1.0
 */
@SuppressWarnings("SameReturnValue")
@ControllerAdvice
class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String EXCEPTION = "exception";
    private static final String URL = "url";
    private static final String SUBJECT = "subject";
    private static final String MESSAGE = "message";
    private static final String TITLE = "title";

    private static final String PAGE = "generic_error";

//    @ResponseStatus(value= HttpStatus.INTERNAL_SERVER_ERROR, reason="EnricherException occurred")
    @ExceptionHandler(EnricherException.class)
    public ModelAndView handleOtherExceptions(HttpServletRequest request, EnricherException e) {
        return buildModelView(PAGE, request, e);
    }

//    @ResponseStatus(value= HttpStatus.INTERNAL_SERVER_ERROR, reason="SolrSearcherException occurred")
    @ExceptionHandler(SolrSearcherException.class)
    public ModelAndView handleSolrSearcherException(HttpServletRequest request, SolrSearcherException e) {
        return buildModelView(PAGE, request, e);

    }

//    @ResponseStatus(value= HttpStatus.INTERNAL_SERVER_ERROR, reason="SearchServiceException occurred")
    @ExceptionHandler(SearchServiceException.class)
    public ModelAndView handleSQLException(HttpServletRequest request, SearchServiceException e) {
        return buildModelView(PAGE, request, e);

    }

    @ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "IOException occurred")
    @ExceptionHandler(IOException.class)
    public void handleIOException() {
        logger.error("IOException handler executed");  //returning 404 error code
    }

    public ModelAndView buildModelView(String modelName, HttpServletRequest request, Exception e) {
        logger.info("Exception occurred:: URL=" + request.getRequestURL());

        ModelAndView model = new ModelAndView(modelName);
        model.addObject(EXCEPTION, e);
        model.addObject(URL, request.getRequestURL());

        model.addObject(SUBJECT, "Unexpected error occurred.");

        StringBuilder sb = new StringBuilder();
        sb.append("Dear HelpDesk,");
        sb.append("\n\n");
        sb.append("An unexpected error has occurred during my search.");
        sb.append("\n\n");
        sb.append("<< Please add more information >>");
        sb.append("\n\n");
        sb.append("Thank you");
        sb.append("\n\n");

        model.addObject(MESSAGE, sb.toString());

        model.addObject(TITLE, "Unexpected error occurred.");
       
        return model;
    }
}
