package tv.notube.profiler.process;

import java.util.Properties;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.name.Names;

import org.apache.camel.component.properties.PropertiesComponent;
import org.apache.camel.guice.CamelModuleWithMatchingRoutes;
import org.guiceyfruit.jndi.JndiBind;

import tv.notube.commons.cogito.CogitoNLPEngineImpl;
import tv.notube.commons.helper.PropertiesHelper;
import tv.notube.commons.helper.jedis.DefaultJedisPoolFactory;
import tv.notube.commons.helper.jedis.JedisPoolFactory;
import tv.notube.commons.linking.FacebookCogitoLinkingEngine;
import tv.notube.commons.linking.LinkingEngine;
import tv.notube.commons.lupedia.LUpediaNLPEngineImpl;
import tv.notube.commons.model.activity.Tweet;
import tv.notube.commons.model.activity.facebook.Like;
import tv.notube.commons.nlp.NLPEngine;
import tv.notube.profiler.DefaultProfilerImpl;
import tv.notube.profiler.Profiler;
import tv.notube.profiler.ProfilerException;
import tv.notube.profiler.rules.custom.FacebookLikeProfilingRule;
import tv.notube.profiler.rules.custom.GenericObjectProfilingRule;
import tv.notube.profiler.rules.custom.TweetProfilingRule;
import tv.notube.profiles.JedisProfilesImpl;
import tv.notube.profiles.Profiles;

public class ProfilerModule extends CamelModuleWithMatchingRoutes {

    @Override
    protected void configure() {
        super.configure();
        Properties redisProperties = PropertiesHelper.readFromClasspath("/redis.properties");
        Names.bindProperties(binder(), redisProperties);
        bindInstance("redisProperties", redisProperties);
        Properties profilerProperties = PropertiesHelper.readFromClasspath("/profiler.properties");
        bindInstance("profilerProperties", profilerProperties);
        bind(JedisPoolFactory.class).to(DefaultJedisPoolFactory.class).asEagerSingleton();
        bind(Profiles.class).to(JedisProfilesImpl.class);

        // bind NLP and linking engine
        bind(NLPEngine.class).to(LUpediaNLPEngineImpl.class);
        bind(LinkingEngine.class).toInstance(new FacebookCogitoLinkingEngine());

        // bind profiler
        bind(Profiler.class).to(DefaultProfilerImpl.class);

        bind(ProfilerRoute.class);
    }

    @Provides
    @JndiBind("properties")
    PropertiesComponent propertiesComponent() {
        PropertiesComponent pc = new PropertiesComponent();
        pc.setLocation("classpath:profiler.properties");
        return pc;
    }

}
