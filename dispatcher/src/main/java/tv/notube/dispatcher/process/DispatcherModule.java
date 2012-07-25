package tv.notube.dispatcher.process;

import java.util.Properties;

import com.google.inject.Provides;
import com.google.inject.name.Names;

import org.apache.camel.component.properties.PropertiesComponent;
import org.apache.camel.guice.CamelModuleWithMatchingRoutes;
import org.guiceyfruit.jndi.JndiBind;

import tv.notube.commons.helper.PropertiesHelper;

public class DispatcherModule extends CamelModuleWithMatchingRoutes {

    @Override
    protected void configure() {
        super.configure();
        Properties properties = PropertiesHelper.readFromClasspath("/beancounter.properties");
        Names.bindProperties(binder(), properties);
        bind(DispatcherRoute.class);
    }

    @Provides
    @JndiBind("properties")
    PropertiesComponent propertiesComponent() {
        PropertiesComponent pc = new PropertiesComponent();
        pc.setLocation("classpath:beancounter.properties");
        return pc;
    }
}