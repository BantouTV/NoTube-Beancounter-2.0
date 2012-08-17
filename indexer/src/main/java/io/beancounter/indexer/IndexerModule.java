package io.beancounter.indexer;

import java.util.Properties;

import com.google.inject.Provides;

import org.apache.camel.component.properties.PropertiesComponent;
import org.apache.camel.guice.CamelModuleWithMatchingRoutes;
import org.guiceyfruit.jndi.JndiBind;

import io.beancounter.activities.ActivityStore;
import io.beancounter.activities.ElasticSearchActivityStore;
import io.beancounter.commons.helper.PropertiesHelper;
import io.beancounter.commons.helper.es.ElasticSearchConfiguration;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class IndexerModule extends CamelModuleWithMatchingRoutes {

    @Override
    protected void configure() {
        super.configure();
        Properties esProperties = PropertiesHelper.readFromClasspath("/es.properties");
        bindInstance("esConfiguration", ElasticSearchConfiguration.build(esProperties));

        bind(ActivityStore.class).to(ElasticSearchActivityStore.class);
        bind(IndexerRoute.class);
    }


    @Provides
    @JndiBind("properties")
    PropertiesComponent propertiesComponent() {
        PropertiesComponent pc = new PropertiesComponent();
        pc.setLocation("classpath:beancounter.properties");
        return pc;
    }

}
