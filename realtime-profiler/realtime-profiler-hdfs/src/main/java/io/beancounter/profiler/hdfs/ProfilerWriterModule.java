package io.beancounter.profiler.hdfs;

import com.google.inject.Provides;
import org.apache.camel.component.properties.PropertiesComponent;
import org.apache.camel.guice.CamelModuleWithMatchingRoutes;
import org.guiceyfruit.jndi.JndiBind;

import java.util.Properties;

public class ProfilerWriterModule extends CamelModuleWithMatchingRoutes {

    @Override
    protected void configure() {}

    @Provides
    @JndiBind("properties")
    PropertiesComponent propertiesComponent() {
        PropertiesComponent pc = new PropertiesComponent();
        pc.setLocation("classpath:beancounter.properties");
        return pc;
    }

}
