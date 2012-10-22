package io.beancounter.analyser.process;

import com.google.inject.Provides;
import com.google.inject.name.Names;
import io.beancounter.analyser.Analyser;
import io.beancounter.analyser.DefaultInMemoryAnalyserImpl;
import io.beancounter.analyser.manager.AnalysisManager;
import io.beancounter.analyser.manager.InMemoryAnalysisManagerConfig;
import io.beancounter.analyser.manager.InMemoryAnalysisManagerImpl;
import io.beancounter.analyses.Analyses;
import io.beancounter.analyses.JedisAnalysesImpl;
import io.beancounter.commons.helper.PropertiesHelper;
import io.beancounter.commons.helper.jedis.DefaultJedisPoolFactory;
import io.beancounter.commons.helper.jedis.JedisPoolFactory;
import org.apache.camel.component.properties.PropertiesComponent;
import org.apache.camel.guice.CamelModuleWithMatchingRoutes;
import org.guiceyfruit.jndi.JndiBind;

import java.util.Properties;

public class RealTimeAnalyserModule extends CamelModuleWithMatchingRoutes {

    @Override
    protected void configure() {
        super.configure();
        Properties redisProperties = PropertiesHelper.readFromClasspath("/redis.properties");
        Names.bindProperties(binder(), redisProperties);

        bind(JedisPoolFactory.class).to(DefaultJedisPoolFactory.class).asEagerSingleton();
        bind(Analyses.class).to(JedisAnalysesImpl.class);
        bind(AnalysisManager.class).to(InMemoryAnalysisManagerImpl.class);
        bind(Analyser.class).to(DefaultInMemoryAnalyserImpl.class);

        // binding the route
        bind(RealTimeAnalyserRoute.class);
    }

    @Provides
    @JndiBind("properties")
    PropertiesComponent propertiesComponent() {
        PropertiesComponent pc = new PropertiesComponent();
        pc.setLocation("classpath:beancounter.properties");
        return pc;
    }

    @Provides
    InMemoryAnalysisManagerConfig analysisManagerConfig() {
        Properties analyserProperties = PropertiesHelper.readFromClasspath("/analyser.properties");
        return InMemoryAnalysisManagerConfig.build(analyserProperties);
    }
}
