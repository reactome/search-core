package org.reactome.server.search.domain;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.reactome.server.search.solr.SolrConverter.Field.*;

/**
 * Internal Model for Reactome Entries
 *
 * @author Florian Korninger (fkorn@ebi.ac.uk)
 * @version 1.0
 */
@SuppressWarnings("unused")
@Getter
@Setter
public class Query {
    public enum Scope {REFERENCE_ENTITY, PHYSICAL_ENTITY, BOTH}

    private String query;
    private String originalQuery;
    private String filterQuery; //fq
    private List<String> species;
    private List<String> types;
    private List<String> keywords;
    private List<String> compartments;
    private Boolean includeInteractors;
    private Scope scope;
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
        private Boolean includeInteractors = false;
        private Scope scope = Scope.REFERENCE_ENTITY;
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

        public Builder keepOriginalQuery(String originalQuery) {
            this.originalQuery = originalQuery;
            return this;
        }

        public Builder addFilterQuery(String filterQuery) {
            this.filterQuery = filterQuery;
            return this;
        }

        public Builder forSpecies(List<String> species) {
            if (species != null) this.species = new ArrayList<>(species);
            return this;
        }

        public Builder withTypes(List<String> types) {
            if (types != null) this.types = new ArrayList<>(types);
            return this;
        }

        public Builder withKeywords(List<String> keywords) {
            if (keywords != null) this.keywords = new ArrayList<>(keywords);
            return this;
        }

        public Builder inCompartments(List<String> compartments) {
            if (compartments != null) this.compartments = new ArrayList<>(compartments);
            return this;
        }

        public Builder includeInteractors(Boolean includeInteractors) {
            this.includeInteractors = includeInteractors == true;
            return this;
        }

        public Builder withScope(Scope scope) {
            this.scope = scope;
            return this;
        }

        public Builder withDeleted(Boolean deleted) {
            this.deleted = deleted;
            return this;
        }

        public Builder start(Integer start) {
            this.start = start;
            return this;
        }

        public Builder numberOfRows(Integer rows) {
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

        public Query build() {
            Query ret = new Query();
            ret.query = this.query;
            ret.originalQuery = this.originalQuery;
            ret.filterQuery = this.filterQuery;
            ret.species = this.species;
            ret.types = this.types;
            ret.keywords = this.keywords;
            ret.compartments = this.compartments;
            ret.includeInteractors = this.includeInteractors;
            ret.scope = this.scope;
            ret.deleted = this.deleted;
            ret.start = this.start;
            ret.rows = this.rows;
            ret.reportInfo = this.reportInfo;
            ret.parserType = this.parserType;
            return ret;
        }
    }

    private Query() {
    }

    public String getOccurrencesFieldName() {
        return this.includeInteractors ? OCCURRENCES_INTERACTOR.name : OCCURRENCES.name;
    }

    public String getDiagramsFieldName() {
        return this.includeInteractors ? DIAGRAMS_INTERACTOR.name : DIAGRAMS.name;
    }

    public Boolean isDeleted() {
        return deleted;
    }
}
