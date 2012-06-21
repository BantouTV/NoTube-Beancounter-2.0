package tv.notube.platform;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;
import com.sun.jersey.api.core.ClasspathResourceConfig;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.guice.JerseyServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
import com.sun.jersey.spi.container.servlet.ServletContainer;
import org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import tv.notube.applications.ApplicationsManager;
import tv.notube.applications.MockApplicationsManager;
import tv.notube.crawler.Crawler;
import tv.notube.crawler.ParallelCrawlerImpl;
import tv.notube.crawler.requester.MockRequester;
import tv.notube.crawler.requester.Requester;
import tv.notube.profiles.MockProfiles;
import tv.notube.profiles.Profiles;
import tv.notube.usermanager.MockUserManager;
import tv.notube.usermanager.UserManager;

import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Enrico Candino (enrico.candino@gmail.com)
 */
public class ProductionServiceConfig extends GuiceServletContextListener {

    @Override
    protected Injector getInjector() {
        return Guice.createInjector(new JerseyServletModule() {
            @Override
            protected void configureServlets() {
                Map<String, String> initParams = new HashMap<String, String>();
                initParams.put(
                        ServletContainer.RESOURCE_CONFIG_CLASS,
                        ClasspathResourceConfig.class.getName()
                );
                // add REST services
                bind(ApplicationService.class);
                bind(UserService.class);
                // bind Production Implementations
                bind(ApplicationsManager.class).to(MockApplicationsManager.class);
                bind(UserManager.class).to(MockUserManager.class);
                bind(Profiles.class).to(MockProfiles.class);
                bind(Crawler.class).to(ParallelCrawlerImpl.class);
                bind(Requester.class).to(MockRequester.class);
                // add bindings for Jackson
                bind(JacksonJaxbJsonProvider.class).asEagerSingleton();
                bind(MessageBodyReader.class).to(JacksonJsonProvider.class);
                bind(MessageBodyWriter.class).to(JacksonJsonProvider.class);
                // Route all requests through GuiceContainer
                serve("/rest/*").with(GuiceContainer.class);
                filter("/rest/*").through(GuiceContainer.class, initParams);
            }
        });
    }
}