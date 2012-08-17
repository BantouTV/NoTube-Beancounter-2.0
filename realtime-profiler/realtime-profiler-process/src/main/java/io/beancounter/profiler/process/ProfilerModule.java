package io.beancounter.profiler.process;

import java.util.Properties;

import com.google.inject.Provides;
import com.google.inject.name.Names;

import org.apache.camel.component.properties.PropertiesComponent;
import org.apache.camel.guice.CamelModuleWithMatchingRoutes;
import org.guiceyfruit.jndi.JndiBind;

import io.beancounter.commons.helper.PropertiesHelper;
import io.beancounter.commons.helper.jedis.DefaultJedisPoolFactory;
import io.beancounter.commons.helper.jedis.JedisPoolFactory;
import io.beancounter.commons.linking.FacebookCogitoLinkingEngine;
import io.beancounter.commons.linking.LinkingEngine;
import io.beancounter.commons.lupedia.LUpediaNLPEngineImpl;
import io.beancounter.commons.nlp.NLPEngine;
import io.beancounter.profiler.DefaultProfilerImpl;
import io.beancounter.profiler.Profiler;
import io.beancounter.profiles.JedisProfilesImpl;
import io.beancounter.profiles.Profiles;

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
        pc.setLocation("classpath:beancounter.properties");
        return pc;
    }

}
