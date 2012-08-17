package io.beancounter.platform;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.name.Names;
import com.google.inject.servlet.GuiceServletContextListener;
import com.sun.jersey.guice.JerseyServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
import org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import io.beancounter.activities.ActivityStore;
import io.beancounter.platform.activities.MockActivityStore;
import io.beancounter.applications.MockApplicationsManager;
import io.beancounter.applications.ApplicationsManager;
import io.beancounter.commons.helper.PropertiesHelper;
import io.beancounter.filter.manager.FilterManager;
import io.beancounter.filter.manager.InMemoryFilterManager;
import io.beancounter.profiles.MockProfiles;
import io.beancounter.profiles.Profiles;
import io.beancounter.queues.MockQueues;
import io.beancounter.queues.Queues;
import io.beancounter.usermanager.MockUserManager;
import io.beancounter.usermanager.UserManager;

import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 *
 * @author Enrico Candino (enrico.candino@gmail.com)
 */
public class TestServiceConfig extends GuiceServletContextListener {

    @Override
    protected Injector getInjector() {
        return Guice.createInjector(new JerseyServletModule() {
            @Override
            protected void configureServlets() {
                Map<String, String> initParams = new HashMap<String, String>();
                // add bindings to mockups
                bind(ApplicationsManager.class).to(MockApplicationsManager.class).asEagerSingleton();
                bind(UserManager.class).to(MockUserManager.class);
                bind(Profiles.class).to(MockProfiles.class);
                bind(ActivityStore.class).to(MockActivityStore.class).asEagerSingleton();
                bind(Queues.class).to(MockQueues.class);
                bind(FilterManager.class).to(InMemoryFilterManager.class).asEagerSingleton();
                // add REST services
                bind(ApplicationService.class);
                bind(UserService.class);
                bind(ActivitiesService.class);

                Properties properties = PropertiesHelper.readFromClasspath("/beancounter.properties");
                Names.bindProperties(binder(), properties);

                bind(AliveService.class);
                bind(FilterService.class);
                // add bindings for Jackson
                bind(JacksonJaxbJsonProvider.class).asEagerSingleton();
                bind(JacksonMixInProvider.class).asEagerSingleton();
                bind(MessageBodyReader.class).to(JacksonJsonProvider.class);
                bind(MessageBodyWriter.class).to(JacksonJsonProvider.class);
                // Route all requests through GuiceContainer
                serve("/*").with(GuiceContainer.class);
                filter("/*").through(GuiceContainer.class, initParams);
            }
        });
    }

}