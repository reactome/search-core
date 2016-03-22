package org.reactome.server.tools.search.database;

import org.reactome.server.tools.search.domain.EnrichedEntry;
import org.reactome.server.tools.search.exception.EnricherException;

/**
 * Interface for accessing Enricher
 *
 * @author Florian Korninger (fkorn@ebi.ac.uk)
 * @version 1.0
 */
public interface IEnricher {

    public EnrichedEntry enrichEntry(String dbId) throws EnricherException;

}
