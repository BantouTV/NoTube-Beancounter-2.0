package tv.notube.indexer;

import java.util.Properties;

import com.google.inject.Provides;

import org.apache.camel.component.properties.PropertiesComponent;
import org.apache.camel.guice.CamelModuleWithMatchingRoutes;
import org.guiceyfruit.jndi.JndiBind;

import tv.notube.activities.ActivityStore;
import tv.notube.activities.ElasticSearchActivityStoreImpl;
import tv.notube.commons.helper.PropertiesHelper;
import tv.notube.commons.helper.es.ElasticSearchConfiguration;

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

        bind(ActivityStore.class).to(ElasticSearchActivityStoreImpl.class);
        bind(IndexerRoute.class);
    }


    @Provides
    @JndiBind("properties")
    PropertiesComponent propertiesComponent() {
        PropertiesComponent pc = new PropertiesComponent();
        pc.setLocation("classpath:indexer.properties");
        return pc;
    }

}
