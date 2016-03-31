package org.reactome.server.tools.search.database;

import org.gk.model.GKInstance;
import org.gk.model.ReactomeJavaConstants;
import org.gk.persistence.MySQLAdaptor;
import org.reactome.server.tools.search.domain.EnrichedEntry;
import org.reactome.server.tools.search.exception.EnricherException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Queries the MySql database and converts entry to a local object
 *
 * @author Florian Korninger (fkorn@ebi.ac.uk)
 * @version 1.0
 */
@Component
public class Enricher {

    private static final Logger logger = LoggerFactory.getLogger(Enricher.class);

    private static MySQLAdaptor dba;

    /**
     * Constructor that sets up a database connection
     *
     * @param host,database,user,password,port parameters to set up connection
     * @throws EnricherException
     */
    @Autowired
    public Enricher(@Value("${database_host}") String host,
                    @Value("${database_name}") String database,
                    @Value("${database_user}") String user,
                    @Value("${database_password}") String password,
                    @Value("${database_port}") Integer port) throws EnricherException {
        try {
            dba = new MySQLAdaptor(host, database, user, password, port);
        } catch (SQLException e) {
            logger.error("Could not initiate MySQLAdapter", e);
            throw new EnricherException("Could not initiate MySQLAdapter", e);
        }
    }

    /**
     * Only public method available to generate a entry from the database
     *
     * @param id MySql Adapter will fetch instance for given StId old_StId or DbId
     * @return EnrichedEntry
     */
    public EnrichedEntry enrichEntry(String id) {
        try {
            GKInstance instance = getInstance(id);
            if (instance != null) {
                EnrichedEntry enrichedEntry = new EnrichedEntry();
                GeneralAttributeEnricher.setGeneralAttributes(instance, enrichedEntry);
                if (instance.getSchemClass().isa(ReactomeJavaConstants.Event)) {
                    EventAttributeEnricher.setEventAttributes(instance, enrichedEntry);
                } else if (instance.getSchemClass().isa(ReactomeJavaConstants.PhysicalEntity)) {
                    PhysicalEntityAttributeEnricher.setPhysicalEntityAttributes(instance, enrichedEntry);
                } else if (instance.getSchemClass().isa(ReactomeJavaConstants.Regulation)) {
                    enrichedEntry.setRegulation(EnricherUtil.getRegulation(instance));
                } else {
                    logger.warn("Unexpected schema class found");
                }

                instance.deflate();

                return enrichedEntry;
            }
        } catch (Exception e) {
            logger.error("Error occurred when trying to fetch Instance by dbId");
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private GKInstance getInstance(String identifier) throws Exception {
        identifier = identifier.trim().split("\\.")[0];
        if (identifier.startsWith("REACT")) {
            return getInstance(dba.fetchInstanceByAttribute(ReactomeJavaConstants.StableIdentifier, "oldIdentifier", "=", identifier));
        } else if (identifier.startsWith("R-")) {
            return getInstance(dba.fetchInstanceByAttribute(ReactomeJavaConstants.StableIdentifier, ReactomeJavaConstants.identifier, "=", identifier));
        } else {
            return dba.fetchInstance(Long.parseLong(identifier));
        }
    }

    private GKInstance getInstance(Collection<GKInstance> target) throws Exception {
        if (target == null) throw new Exception("No entity found");
        if (target.size() != 1) throw new Exception("Many options have been found for the specified identifier");
        GKInstance stId = target.iterator().next();
        return (GKInstance) dba.fetchInstanceByAttribute(ReactomeJavaConstants.DatabaseObject, ReactomeJavaConstants.stableIdentifier, "=", stId).iterator().next();
    }

    /**
     * The FrontPage data schema holds all the TopLevelPathways.
     * In the search only TopLevelPathways should be listed
     *
     * @return Set of FrontPages.
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public static Set<String> loadFrontPage() throws Exception {
        Set<String> topLevelPathway = new HashSet<>();
        Collection<?> frontPage = dba.fetchInstancesByClass(ReactomeJavaConstants.FrontPage);
        GKInstance instance = (GKInstance) frontPage.iterator().next();
        List<GKInstance> instances = instance.getAttributeValuesList(ReactomeJavaConstants.frontPageItem);

        /**
         * Matching only the number in the stable identifier, because in the
         * FrontPage model there aren't top level pathways for all species.
         * The IDs are the same for the species.
         */
        Pattern p = Pattern.compile("([0-9]+)");

        for (GKInstance gki : instances) {
            String stId = EnricherUtil.getStableIdentifier(gki);
            Matcher m = p.matcher(stId);

            if (m.find()) {
                String s = m.group(1);
                topLevelPathway.add(s);
            }
        }

        return topLevelPathway;
    }


    /**
     * Helper method to determine if a instance contains a diagram
     *
     * @param dbId id
     * @return boolean
     * @throws EnricherException
     */
    public static boolean hasDiagram(long dbId) throws EnricherException {
        try {
            GKInstance inst = dba.fetchInstance(dbId);
            if (inst.getSchemClass().isa(ReactomeJavaConstants.Pathway)) {
                Collection<?> diagrams = inst.getReferers(ReactomeJavaConstants.representedPathway);
                if (diagrams != null && diagrams.size() > 0) {
                    for (Object diagram1 : diagrams) {
                        GKInstance diagram = (GKInstance) diagram1;
                        if (diagram.getSchemClass().isa(ReactomeJavaConstants.PathwayDiagram)) {
                            return true;
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new EnricherException(e.getMessage(), e);
        }
        return false;
    }

}