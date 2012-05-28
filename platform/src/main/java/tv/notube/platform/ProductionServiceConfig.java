package tv.notube.platform;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.guice.JerseyServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
import org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import tv.notube.analytics.Analyzer;
import tv.notube.analytics.DefaultAnalyzerImpl;
import tv.notube.applications.ApplicationsManager;
import tv.notube.applications.DefaultApplicationsManagerImpl;
import tv.notube.crawler.Crawler;
import tv.notube.crawler.ParallelCrawlerImpl;
import tv.notube.profiler.DefaultProfilerImpl;
import tv.notube.profiler.Profiler;
import tv.notube.profiler.storage.KVProfileStoreImpl;
import tv.notube.profiler.storage.ProfileStore;
import tv.notube.usermanager.DefaultUserManagerImpl;
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
                initParams.put(PackagesResourceConfig.PROPERTY_PACKAGES, "tv.notube.platform");
                // add REST services
                bind(AnalyticsService.class);
                bind(ApplicationService.class);
                bind(UserService.class);
                // bind Production Implementations
                bind(ApplicationsManager.class).to(DefaultApplicationsManagerImpl.class);
                bind(Analyzer.class).to(DefaultAnalyzerImpl.class);
                bind(UserManager.class).to(DefaultUserManagerImpl.class);
                bind(ProfileStore.class).to(KVProfileStoreImpl.class);
                bind(Crawler.class).to(ParallelCrawlerImpl.class);
                bind(Profiler.class).to(DefaultProfilerImpl.class);
                // add bindings for Jackson
                bind(JacksonJaxbJsonProvider.class).asEagerSingleton();
                bind(MessageBodyReader.class).to(JacksonJsonProvider.class);
                bind(MessageBodyWriter.class).to(JacksonJsonProvider.class);
                // Route all requests through GuiceContainer
                serve("/*").with(GuiceContainer.class);
                filter("/*").through(GuiceContainer.class, initParams);
            }
        });
    }
}