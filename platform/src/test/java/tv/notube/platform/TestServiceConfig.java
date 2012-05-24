package tv.notube.platform;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.guice.JerseyServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
import tv.notube.analytics.Analyzer;
import tv.notube.platform.ApplicationService;
import tv.notube.platform.analytics.MockAnalyzer;
import tv.notube.applications.ApplicationsManager;
import tv.notube.platform.AnalyticsService;
import tv.notube.platform.applications.MockApplicationsManager;
import tv.notube.platform.user.MockUserManager;
import tv.notube.usermanager.UserManager;

import java.util.HashMap;
import java.util.Map;

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
                initParams.put(PackagesResourceConfig.PROPERTY_PACKAGES, "tv.notube.platform");
                // add REST services
                bind(AnalyticsService.class);
                bind(ApplicationService.class);
                bind(UserService.class);
                // add bindings to mockups
                bind(ApplicationsManager.class).to(MockApplicationsManager.class);
                bind(Analyzer.class).to(MockAnalyzer.class);
                bind(UserManager.class).to(MockUserManager.class);
                // Route all requests through GuiceContainer
                serve("/*").with(GuiceContainer.class);
                filter("/*").through(GuiceContainer.class, initParams);
            }
        });
    }

}
