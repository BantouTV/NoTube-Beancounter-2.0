package io.beancounter.activities;

import io.beancounter.commons.helper.PropertiesHelper;
import io.beancounter.commons.helper.es.ElasticSearchConfiguration;

import java.util.Properties;

/**
 * @author Alex Cowell ( alxcwll@gmail.com )
 */
public class ElasticSearchActivityStoreFactory implements ActivityStoreFactory {

    private static final String ELASTICSEARCH_CONF = "/es.properties";

    private static ActivityStoreFactory instance;

    private ActivityStore activityStore;

    public static synchronized ActivityStoreFactory getInstance() {
        if (instance == null) {
            instance = new ElasticSearchActivityStoreFactory();
        }

        return instance;
    }

    public ElasticSearchActivityStoreFactory() {
        Properties properties = PropertiesHelper.readFromClasspath(ELASTICSEARCH_CONF);
        ElasticSearchConfiguration configuration = ElasticSearchConfiguration.build(properties);
        activityStore = new ElasticSearchActivityStore(configuration);
    }

    @Override
    public ActivityStore build() {
        return activityStore;
    }
}
