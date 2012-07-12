package tv.notube.indexer;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.name.Names;
import org.apache.camel.component.properties.PropertiesComponent;
import org.apache.camel.guice.CamelModuleWithMatchingRoutes;
import org.guiceyfruit.jndi.JndiBind;
import tv.notube.activities.ActivityStore;
import tv.notube.activities.ElasticSearchActivityStoreImpl;
import tv.notube.commons.helper.PropertiesHelper;
import tv.notube.commons.helper.es.ElasticSearchConfiguration;
import tv.notube.commons.helper.jedis.DefaultJedisPoolFactory;
import tv.notube.commons.helper.jedis.JedisPoolFactory;
import tv.notube.commons.lupedia.LUpediaNLPEngineImpl;
import tv.notube.commons.model.activity.*;
import tv.notube.profiler.DefaultProfilerImpl;
import tv.notube.profiler.Profiler;
import tv.notube.profiler.ProfilerException;
import tv.notube.profiler.rules.custom.DevNullProfilingRule;
import tv.notube.profiler.rules.custom.TweetProfilingRule;
import tv.notube.profiles.Profiles;
import tv.notube.profiles.ProfilesModule;

import java.util.Properties;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class IndexerModule extends CamelModuleWithMatchingRoutes {

    @Override
    protected void configure() {
        super.configure();
        Properties redisProperties = PropertiesHelper.readFromClasspath("/redis.properties");
        Names.bindProperties(binder(), redisProperties);
        bindInstance("redisProperties", redisProperties);
        bind(JedisPoolFactory.class).to(DefaultJedisPoolFactory.class).asEagerSingleton();
        Properties profilerProperties = PropertiesHelper.readFromClasspath("/profiler.properties");
        try {
            bind(Profiler.class).toInstance(createProfiler(profilerProperties));
        } catch (ProfilerException e) {
            final String errMsg = "Error while building profiler";
            throw new RuntimeException(errMsg, e);
        }

        Properties esProperties = PropertiesHelper.readFromClasspath("/es.properties");
        bindInstance("esConfiguration", ElasticSearchConfiguration.build(esProperties));

        bind(ActivityStore.class).to(ElasticSearchActivityStoreImpl.class);
        bind(IndexerRoute.class);
    }

    private Profiler createProfiler(Properties properties) throws ProfilerException {
        Injector injector = Guice.createInjector(new ProfilesModule());
        Profiles profiles = injector.getInstance(Profiles.class);
        Profiler profiler = new DefaultProfilerImpl(
                profiles,
                new LUpediaNLPEngineImpl(),
                null,
                properties
        );
        profiler.registerRule(Tweet.class, TweetProfilingRule.class);
        // TODO (med) plug the facebook one
        profiler.registerRule(tv.notube.commons.model.activity.Object.class, DevNullProfilingRule.class);
        return profiler;
    }

    @Provides
    @JndiBind("properties")
    PropertiesComponent propertiesComponent() {
        PropertiesComponent pc = new PropertiesComponent();
        pc.setLocation("classpath:indexer.properties");
        return pc;
    }

}
