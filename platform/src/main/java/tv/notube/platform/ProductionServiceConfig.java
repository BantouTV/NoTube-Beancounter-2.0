package tv.notube.platform;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.guice.JerseyServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
import tv.notube.analytics.Analyzer;
import tv.notube.analytics.DefaultAnalyzerImpl;
import tv.notube.applications.ApplicationsManager;
import tv.notube.applications.DefaultApplicationsManagerImpl;
import tv.notube.platform.AnalyticsService;

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
                bind(AnalyticsService.class);
                bind(ApplicationsManager.class).to(DefaultApplicationsManagerImpl.class);
                bind(Analyzer.class).to(DefaultAnalyzerImpl.class);
                // Route all requests through GuiceContainer
                serve("/*").with(GuiceContainer.class);
                filter("/*").through(GuiceContainer.class, initParams);
            }
        });
    }
}