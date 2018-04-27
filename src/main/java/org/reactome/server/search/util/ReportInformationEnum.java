package org.reactome.server.search.util;

/**
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */

public enum ReportInformationEnum {
    RELEASEVERSION("release-version"),
    USERAGENT("user-agent"),
    IPADDRESS("ip-address");

    private String desc;

    ReportInformationEnum(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}
