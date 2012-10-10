package io.beancounter.profiler.hdfs;

import com.google.inject.Provides;
import io.beancounter.commons.helper.PropertiesHelper;
import org.apache.camel.component.properties.PropertiesComponent;
import org.apache.camel.guice.CamelModuleWithMatchingRoutes;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hdfs.DistributedFileSystem;
import org.guiceyfruit.jndi.JndiBind;

import java.util.Properties;

public class ProfilerWriterModule extends CamelModuleWithMatchingRoutes {

    @Override
    protected void configure() {
        super.configure();
        Properties properties = PropertiesHelper.readFromClasspath("/beancounter.properties");
        // TODO: Get the hadoop namenode URI from the properties and pass it
        // to the HDFSProfileWriter
        bind(DistributedFileSystem.class).toInstance(new DistributedFileSystem());
        bind(Configuration.class).toInstance(new Configuration());
        bind(ProfileWriter.class).to(HDFSProfileWriter.class);
        bind(ProfilerWriterRoute.class);
    }

    @Provides
    @JndiBind("properties")
    PropertiesComponent propertiesComponent() {
        PropertiesComponent pc = new PropertiesComponent();
        pc.setLocation("classpath:beancounter.properties");
        return pc;
    }

}
