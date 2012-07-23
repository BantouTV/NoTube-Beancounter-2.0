package tv.notube.profiler.process;

import java.util.Properties;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.name.Names;

import org.apache.camel.component.properties.PropertiesComponent;
import org.apache.camel.guice.CamelModuleWithMatchingRoutes;
import org.guiceyfruit.jndi.JndiBind;

import tv.notube.commons.helper.PropertiesHelper;
import tv.notube.commons.helper.jedis.DefaultJedisPoolFactory;
import tv.notube.commons.helper.jedis.JedisPoolFactory;
import tv.notube.commons.linking.FacebookCogitoLinkingEngine;
import tv.notube.commons.lupedia.LUpediaNLPEngineImpl;
import tv.notube.commons.model.activity.Tweet;
import tv.notube.commons.model.activity.facebook.Like;
import tv.notube.profiler.DefaultProfilerImpl;
import tv.notube.profiler.Profiler;
import tv.notube.profiler.ProfilerException;
import tv.notube.profiler.rules.custom.FacebookLikeProfilingRule;
import tv.notube.profiler.rules.custom.GenericObjectProfilingRule;
import tv.notube.profiler.rules.custom.TweetProfilingRule;
import tv.notube.profiles.Profiles;

public class ProfilerModule extends CamelModuleWithMatchingRoutes {

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

        bind(ProfilerRoute.class);
    }

    private Profiler createProfiler(Properties properties) throws ProfilerException {
        Injector injector = Guice.createInjector(new ProfilerModule());
        Profiles profiles = injector.getInstance(Profiles.class);
        Profiler profiler = new DefaultProfilerImpl(
                profiles,
                new LUpediaNLPEngineImpl(),
                new FacebookCogitoLinkingEngine(),
                properties
        );
        profiler.registerRule(Tweet.class, TweetProfilingRule.class);
        profiler.registerRule(tv.notube.commons.model.activity.Object.class, GenericObjectProfilingRule.class);
        profiler.registerRule(Like.class, FacebookLikeProfilingRule.class);
        return profiler;
    }


    @Provides
    @JndiBind("properties")
    PropertiesComponent propertiesComponent() {
        PropertiesComponent pc = new PropertiesComponent();
        pc.setLocation("classpath:profiler.properties");
        return pc;
    }

}
