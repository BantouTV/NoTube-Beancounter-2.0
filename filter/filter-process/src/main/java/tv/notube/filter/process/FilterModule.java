package tv.notube.filter.process;

import java.util.Properties;

import com.google.inject.Provides;
import com.google.inject.name.Names;

import org.apache.camel.component.properties.PropertiesComponent;
import org.apache.camel.guice.CamelModuleWithMatchingRoutes;
import org.guiceyfruit.jndi.JndiBind;

import tv.notube.commons.helper.PropertiesHelper;
import tv.notube.filter.FilterService;
import tv.notube.filter.InMemoryFilterServiceImpl;
import tv.notube.filter.manager.FilterManager;
import tv.notube.filter.manager.InMemoryFilterManager;

public class FilterModule extends CamelModuleWithMatchingRoutes {

    @Override
    protected void configure() {
        super.configure();
        Properties redisProperties = PropertiesHelper.readFromClasspath("/redis.properties");
        Names.bindProperties(binder(), redisProperties);
        bindInstance("redisProperties", redisProperties);
        bind(FilterService.class).to(InMemoryFilterServiceImpl.class);
        bind(FilterManager.class).to(InMemoryFilterManager.class);
        bind(FilterRoute.class);
    }

    @Provides
    @JndiBind("properties")
    PropertiesComponent propertiesComponent() {
        PropertiesComponent pc = new PropertiesComponent();
        pc.setLocation("classpath:filter.properties");
        return pc;
    }

}
