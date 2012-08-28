package io.beancounter.platform;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.name.Names;
import com.google.inject.servlet.GuiceServletContextListener;
import com.sun.jersey.api.core.ClasspathResourceConfig;
import com.sun.jersey.guice.JerseyServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
import com.sun.jersey.spi.container.servlet.ServletContainer;
import io.beancounter.commons.model.auth.AuthHandler;
import io.beancounter.usermanager.services.auth.facebook.FacebookAuthHandler;
import io.beancounter.usermanager.services.auth.twitter.TwitterAuthHandler;
import io.beancounter.usermanager.services.auth.twitter.TwitterFactoryWrapper;
import org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import io.beancounter.activities.ActivityStore;
import io.beancounter.activities.ActivityStoreException;
import io.beancounter.activities.ElasticSearchActivityStoreFactory;
import io.beancounter.applications.ApplicationsManager;
import io.beancounter.applications.JedisApplicationsManagerImpl;
import io.beancounter.commons.helper.jedis.DefaultJedisPoolFactory;
import io.beancounter.commons.helper.resolver.Services;
import io.beancounter.commons.helper.PropertiesHelper;
import io.beancounter.commons.helper.jedis.JedisPoolFactory;
import io.beancounter.filter.manager.FilterManager;
import io.beancounter.filter.manager.JedisFilterManager;
import io.beancounter.profiles.JedisProfilesImpl;
import io.beancounter.profiles.Profiles;
import io.beancounter.queues.KestrelQueues;
import io.beancounter.queues.Queues;
import io.beancounter.resolver.JedisResolver;
import io.beancounter.resolver.Resolver;
import io.beancounter.usermanager.JedisUserManagerImpl;
import io.beancounter.usermanager.UserManager;
import io.beancounter.usermanager.services.auth.DefaultServiceAuthorizationManager;
import io.beancounter.usermanager.services.auth.ServiceAuthorizationManager;

import javax.servlet.ServletContextEvent;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author Enrico Candino (enrico.candino@gmail.com)
 */
public class ProductionServiceConfig extends GuiceServletContextListener {

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        try {
            ElasticSearchActivityStoreFactory.getInstance().build().shutDown();
        } catch (ActivityStoreException e) {
            final String errMsg = "Error while closing clint to Elastic Search";
            throw new RuntimeException(errMsg, e);
        }
    }

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
                bind(ActivitiesService.class);
                bind(AliveService.class);
                bind(FilterService.class);
                bind(MyRaiTVService.class);

                // bind Production Implementations
                Properties redisProperties = PropertiesHelper.readFromClasspath("/redis.properties");
                Names.bindProperties(binder(), redisProperties);

                bind(JedisPoolFactory.class).to(DefaultJedisPoolFactory.class).asEagerSingleton();
                bind(TwitterFactoryWrapper.class).in(Singleton.class);

                Properties properties = PropertiesHelper.readFromClasspath("/beancounter.properties");
                Names.bindProperties(binder(), properties);

                io.beancounter.commons.model.Service twitterService = DefaultServiceAuthorizationManager.buildService("twitter", properties);
                io.beancounter.commons.model.Service facebookService = DefaultServiceAuthorizationManager.buildService("facebook", properties);
                bind(io.beancounter.commons.model.Service.class)
                        .annotatedWith(Names.named("service.twitter"))
                        .toInstance(twitterService);
                bind(io.beancounter.commons.model.Service.class)
                        .annotatedWith(Names.named("service.facebook"))
                        .toInstance(facebookService);

                MapBinder<io.beancounter.commons.model.Service, AuthHandler> authHandlerBinder
                        = MapBinder.newMapBinder(binder(), io.beancounter.commons.model.Service.class, AuthHandler.class);
                authHandlerBinder.addBinding(twitterService).to(TwitterAuthHandler.class);
                authHandlerBinder.addBinding(facebookService).to(FacebookAuthHandler.class);

                Services services = Services.build(properties);
                bind(Services.class).toInstance(services);
                bind(Resolver.class).to(JedisResolver.class);
                bind(ApplicationsManager.class).to(JedisApplicationsManagerImpl.class);
                bind(UserManager.class).to(JedisUserManagerImpl.class).asEagerSingleton();
                bind(Profiles.class).to(JedisProfilesImpl.class);
                bind(ActivityStore.class).toInstance(getElasticSearch());
                bind(Queues.class).toInstance(new KestrelQueues(properties));
                bind(FilterManager.class).to(JedisFilterManager.class);
                bind(ServiceAuthorizationManager.class).to(DefaultServiceAuthorizationManager.class);

                // add bindings for Jackson
                bind(JacksonJaxbJsonProvider.class).asEagerSingleton();
                bind(JacksonMixInProvider.class).asEagerSingleton();
                bind(MessageBodyReader.class).to(JacksonJsonProvider.class);
                bind(MessageBodyWriter.class).to(JacksonJsonProvider.class);
                // Route all requests through GuiceContainer
                serve("/rest/*").with(GuiceContainer.class);
                filter("/rest/*").through(GuiceContainer.class, initParams);
            }

            private ActivityStore getElasticSearch() {
                return ElasticSearchActivityStoreFactory.getInstance().build();
            }
        });
    }


}