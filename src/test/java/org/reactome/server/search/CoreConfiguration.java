package org.reactome.server.search;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;

/**
 * Created by:
 *
 * @author Florian Korninger (florian.korninger@ebi.ac.uk)
 * @since 24.03.16.
 */
@Configuration
@ComponentScan(basePackages = "org.reactome.server.search")
@EnableAspectJAutoProxy
public class CoreConfiguration {

    @Bean
    static PropertySourcesPlaceholderConfigurer propConfig() {
        PropertySourcesPlaceholderConfigurer ppc =  new PropertySourcesPlaceholderConfigurer();
        ppc.setLocation(new ClassPathResource("reactome.properties"));
        return ppc;
    }

}
