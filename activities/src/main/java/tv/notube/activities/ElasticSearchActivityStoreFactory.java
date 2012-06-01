package tv.notube.activities;

import tv.notube.commons.configuration.Configurations;
import tv.notube.commons.configuration.ConfigurationsException;
import tv.notube.commons.configuration.activities.ElasticSearchConfiguration;

/**
 * @author Alex Cowell ( alxcwll@gmail.com )
 */
public class ElasticSearchActivityStoreFactory implements ActivityStoreFactory {

    private static final String ELASTICSEARCH_CONF = "elasticsearch-configuration.xml";

    private static ActivityStoreFactory instance;

    private ActivityStore activityStore;

    public static synchronized ActivityStoreFactory getInstance() {
        if (instance == null) {
            instance = new ElasticSearchActivityStoreFactory();
        }

        return instance;
    }

    public ElasticSearchActivityStoreFactory() {
        ElasticSearchConfiguration configuration;
        try {
            configuration = Configurations.getConfiguration(
                    ELASTICSEARCH_CONF,
                    ElasticSearchConfiguration.class
            );
        } catch (ConfigurationsException cex) {
            final String message = "Error while loading configuration from " +
                    "[" + ELASTICSEARCH_CONF + "]";
            throw new RuntimeException(message, cex);
        }
        activityStore = new ElasticSearchActivityStoreImpl(configuration);
    }

    @Override
    public ActivityStore build() {
        return activityStore;
    }
}
