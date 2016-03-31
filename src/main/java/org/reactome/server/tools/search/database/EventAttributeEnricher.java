package org.reactome.server.tools.search.database;

import org.gk.model.GKInstance;
import org.gk.model.ReactomeJavaConstants;
import org.reactome.server.tools.search.domain.CatalystActivity;
import org.reactome.server.tools.search.domain.EnrichedEntry;
import org.reactome.server.tools.search.exception.EnricherException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Queries the MySql database and converts entry to a local object
 *
 * @author Florian Korninger (fkorn@ebi.ac.uk)
 * @version 1.0
 */
class EventAttributeEnricher {

    private static final Logger logger = LoggerFactory.getLogger(EventAttributeEnricher.class);

    public static void setEventAttributes(GKInstance instance, EnrichedEntry enrichedEntry) throws EnricherException {
        try {

            if (instance.getSchemClass().isa(ReactomeJavaConstants.ReactionlikeEvent)) {
                enrichedEntry.setCatalystActivities(getCatalystActivities(instance));
                enrichedEntry.setInput(EnricherUtil.getEntityReferences(instance, ReactomeJavaConstants.input));
                enrichedEntry.setOutput(EnricherUtil.getEntityReferences(instance, ReactomeJavaConstants.output));
                if (instance.getSchemClass().isa(ReactomeJavaConstants.Reaction)) {
                    enrichedEntry.setReverseReaction(EnricherUtil.getEntityReference(instance, ReactomeJavaConstants.reverseReaction));

                }
            }
            if (EnricherUtil.hasValue(instance, ReactomeJavaConstants.goBiologicalProcess)) {
                GKInstance goBiologicalProcess = (GKInstance) instance.getAttributeValue(ReactomeJavaConstants.goBiologicalProcess);
                enrichedEntry.setGoBiologicalProcess(EnricherUtil.getGoTerm(goBiologicalProcess));
            }
            enrichedEntry.setRegulatedEvents(EnricherUtil.getRegulations(instance, ReactomeJavaConstants.regulatedEntity));

        } catch (Exception e) {
            logger.error("Error occurred when trying to set event attributes", e);
            throw new EnricherException("Error occurred when trying to set event attributes", e);
        }
    }

    /**
     * Returns a list of catalyst activities of a instance
     *
     * @param instance GkInstance
     * @return List of CatalystActivities
     * @throws EnricherException
     */
    private static List<CatalystActivity> getCatalystActivities(GKInstance instance) throws EnricherException {
        if (EnricherUtil.hasValues(instance, ReactomeJavaConstants.catalystActivity)) {
            try {
                List<CatalystActivity> catalystActivityList = new ArrayList<>();
                List<?> catalystActivityInstanceList = instance.getAttributeValuesList(ReactomeJavaConstants.catalystActivity);

                for (Object catalystActivityObject : catalystActivityInstanceList) {
                    GKInstance catalystActivityInstance = (GKInstance) catalystActivityObject;
                    CatalystActivity catalystActivity = new CatalystActivity();
                    catalystActivity.setPhysicalEntity(EnricherUtil.getEntityReference(catalystActivityInstance, ReactomeJavaConstants.physicalEntity));
                    catalystActivity.setActiveUnit(EnricherUtil.getEntityReferences(catalystActivityInstance, ReactomeJavaConstants.activeUnit));
                    if (EnricherUtil.hasValue(catalystActivityInstance, ReactomeJavaConstants.activity)) {
                        GKInstance activityInstance = (GKInstance) catalystActivityInstance.getAttributeValue(ReactomeJavaConstants.activity);
                        catalystActivity.setActivity(EnricherUtil.getGoTerm(activityInstance));
                    }
                    catalystActivityList.add(catalystActivity);
                }
                return catalystActivityList;
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                throw new EnricherException(e.getMessage(), e);
            }
        }
        return null;
    }
}
