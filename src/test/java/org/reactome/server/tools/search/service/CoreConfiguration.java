package org.reactome.server.tools.search.service;

import org.reactome.server.tools.interactors.database.InteractorsDatabase;
import org.reactome.server.tools.interactors.service.InteractionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.io.ClassPathResource;

import java.sql.SQLException;

/**
 * Created by:
 *
 * @author Florian Korninger (florian.korninger@ebi.ac.uk)
 * @since 24.03.16.
 */
@Configuration
@ComponentScan(basePackages = "org.reactome.server.tools")
@EnableAspectJAutoProxy
public class CoreConfiguration {

    @Bean
    PropertyPlaceholderConfigurer propConfig() {
        PropertyPlaceholderConfigurer ppc =  new PropertyPlaceholderConfigurer();
        ppc.setLocation(new ClassPathResource("reactome.properties"));
        return ppc;
    }

    @Bean
    public InteractionService interactionService(@Value("${interactors.static.db}") String db) throws SQLException {
        return new InteractionService(new InteractorsDatabase(db));
    }
}
