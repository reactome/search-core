package org.reactome.server.search.domain;

public enum ParserType {
    DISMAX("dismax"),
    EDISMAX("edismax"),
    STD("");

    public final String defType;

    ParserType(String defType) {
        this.defType = defType;
    }
}
