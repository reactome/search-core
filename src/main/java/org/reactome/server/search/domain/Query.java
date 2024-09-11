package org.reactome.server.search.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Internal Model for Reactome Entries
 * @author Florian Korninger (fkorn@ebi.ac.uk)
 * @version 1.0
 */
@SuppressWarnings("unused")
public class Query {

    private String query;
    private String originalQuery;
    private String filterQuery; //fq
    private List<String> species;
    private List<String> types;
    private List<String> keywords;
    private List<String> compartments;
    private Boolean deleted;
    private Integer start;
    private Integer rows;
    private ParserType parserType; // defType
    private Map<String, String> reportInfo; // extra information for report, useragent, ip, etc

    /**
     * Builder by default will keep a copy of the original query.
     * In case the query String is massaged to get accurate results from solr,
     * the query itself must not be use in a report, instead use originalQuery
     */
    public static class Builder {
        private String query;
        private String originalQuery;
        private String filterQuery; //fq
        private List<String> species = null;
        private List<String> types = null;
        private List<String> keywords = null;
        private List<String> compartments = null;
        private Boolean deleted = Boolean.FALSE;
        private Integer start;
        private Integer rows;
        private ParserType parserType = ParserType.STD; // defType
        private Map<String, String> reportInfo = null; // extra information for report, useragent, ip, etc

        /**
         * When building, the query is automatically copied to the originalQuery.
         * If the query is already modified, make sure you invoke keepOriginalQuery if you to store the query in a
         * report later.
         */
        public Builder(String query) {
            this.query = query;
            this.originalQuery = query;
        }

        public Builder keepOriginalQuery(String originalQuery){
            this.originalQuery = originalQuery;
            return this;
        }

        public Builder addFilterQuery(String filterQuery){
            this.filterQuery = filterQuery;
            return this;
        }

        public Builder forSpecies(List<String> species){
            if (species != null) this.species = new ArrayList<>(species);
            return this;
        }

        public Builder withTypes(List<String> types){
            if (types != null) this.types = new ArrayList<>(types);
            return this;
        }

        public Builder withKeywords(List<String> keywords){
            if (keywords != null) this.keywords = new ArrayList<>(keywords);
            return this;
        }

        public Builder inCompartments(List<String> compartments){
            if (compartments != null) this.compartments = new ArrayList<>(compartments);
            return this;
        }

        public Builder withDeleted(Boolean deleted){
            this.deleted = deleted;
            return this;
        }

        public Builder start(Integer start){
            this.start = start;
            return this;
        }

        public Builder numberOfRows(Integer rows){
            this.rows = rows;
            return this;
        }

        public Builder withReportInfo(Map<String, String> reportInfo) {
            if (reportInfo != null) this.reportInfo = new HashMap<>(reportInfo);
            return this;
        }

        public Builder withParserType(ParserType parserType) {
            this.parserType = parserType;
            return this;
        }

        public Query build(){
            Query ret = new Query();
            ret.query = this.query;
            ret.originalQuery = this.originalQuery;
            ret.filterQuery = this.filterQuery;
            ret.species = this.species;
            ret.types = this.types;
            ret.keywords = this.keywords;
            ret.compartments = this.compartments;
            ret.deleted = this.deleted;
            ret.start = this.start;
            ret.rows = this.rows;
            ret.reportInfo = this.reportInfo;
            ret.parserType = this.parserType;
            return ret;
        }
    }

    private Query() {}

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getOriginalQuery() {
        return originalQuery;
    }

    public void setOriginalQuery(String originalQuery) {
        this.originalQuery = originalQuery;
    }

    public String getFilterQuery() {
        return filterQuery;
    }

    public void setFilterQuery(String filterQuery) {
        this.filterQuery = filterQuery;
    }

    public List<String> getSpecies() {
        return species;
    }

    public void setSpecies(List<String> species) {
        this.species = species;
    }

    public List<String> getTypes() {
        return types;
    }

    public void setTypes(List<String> types) {
        this.types = types;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    public List<String> getCompartments() {
        return compartments;
    }

    public void setCompartments(List<String> compartments) {
        this.compartments = compartments;
    }

    public Boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    public Integer getStart() {
        return start;
    }

    public void setStart(Integer start) {
        this.start = start;
    }

    public Integer getRows() {
        return rows;
    }

    public void setRows(Integer rows) {
        this.rows = rows;
    }

    public Map<String, String> getReportInfo() {
        return reportInfo;
    }

    public void setReportInfo(Map<String, String> reportInfo) {
        this.reportInfo = reportInfo;
    }

    public ParserType getParserType() {
        return parserType;
    }

    public void setParserType(ParserType parserType) {
        this.parserType = parserType;
    }
}
