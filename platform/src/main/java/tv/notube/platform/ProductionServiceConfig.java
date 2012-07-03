package tv.notube.platform;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.name.Names;
import com.google.inject.servlet.GuiceServletContextListener;
import com.sun.jersey.api.core.ClasspathResourceConfig;
import com.sun.jersey.guice.JerseyServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
import com.sun.jersey.spi.container.servlet.ServletContainer;
import org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import tv.notube.activities.ActivityStore;
import tv.notube.activities.ActivityStoreException;
import tv.notube.activities.ElasticSearchActivityStoreFactory;
import tv.notube.applications.ApplicationsManager;
import tv.notube.applications.JedisApplicationsManagerImpl;
import tv.notube.applications.jedis.DefaultJedisPoolFactory;
import tv.notube.crawler.Crawler;
import tv.notube.crawler.ParallelCrawlerImpl;
import tv.notube.crawler.requester.MockRequester;
import tv.notube.crawler.requester.Requester;
import tv.notube.profiles.JedisProfilesImpl;
import tv.notube.profiles.Profiles;
import tv.notube.queues.KestrelQueues;
import tv.notube.queues.Queues;
import tv.notube.usermanager.JedisUserManagerImpl;
import tv.notube.usermanager.UserManager;
import tv.notube.usermanager.services.auth.DefaultServiceAuthorizationManager;
import tv.notube.usermanager.services.auth.ServiceAuthorizationManager;
import tv.notube.usermanager.services.auth.ServiceAuthorizationManagerException;
import tv.notube.usermanager.services.auth.facebook.FacebookAuthHandler;
import tv.notube.usermanager.services.auth.twitter.TwitterAuthHandler;

import javax.servlet.ServletContextEvent;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 *
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
                // bind Production Implementations
                bind(tv.notube.applications.jedis.JedisPoolFactory.class).to(DefaultJedisPoolFactory.class).asEagerSingleton();
                bind(tv.notube.profiles.jedis.JedisPoolFactory.class).to(tv.notube.profiles.jedis.DefaultJedisPoolFactory.class).asEagerSingleton();
                bind(tv.notube.usermanager.jedis.JedisPoolFactory.class).to(tv.notube.usermanager.jedis.DefaultJedisPoolFactory.class).asEagerSingleton();
                bind(ServiceAuthorizationManager.class).toInstance(getServiceAuthorizationManager());
                // loading Redis properties from redis.properties file
                Names.bindProperties(binder(), getRedisProperties());
                bind(ApplicationsManager.class).to(JedisApplicationsManagerImpl.class);
                bind(UserManager.class).to(JedisUserManagerImpl.class).asEagerSingleton();
                bind(Profiles.class).to(JedisProfilesImpl.class);
                bind(Crawler.class).to(ParallelCrawlerImpl.class);
                bind(Requester.class).to(MockRequester.class);
                bind(ActivityStore.class).toInstance(getElasticSearch());
                bind(Queues.class).toInstance(getKestrelQueue());
                // add bindings for Jackson
                bind(JacksonJaxbJsonProvider.class).asEagerSingleton();
                bind(JacksonMixInProvider.class).asEagerSingleton();
                bind(MessageBodyReader.class).to(JacksonJsonProvider.class);
                bind(MessageBodyWriter.class).to(JacksonJsonProvider.class);
                // Route all requests through GuiceContainer
                serve("/rest/*").with(GuiceContainer.class);
                filter("/rest/*").through(GuiceContainer.class, initParams);
            }

            private Properties getRedisProperties() {
                Properties properties = new Properties();
                try {
                    properties.load(getClass().getResourceAsStream("/redis.properties"));
                } catch (IOException e) {
                    final String errMsg = "Error while reading Redis configuration";
                    throw new RuntimeException(errMsg, e);
                }
                return properties;
            }

            private ActivityStore getElasticSearch() {
                return ElasticSearchActivityStoreFactory.getInstance().build();
            }

            //TODO (hardcoded)
            private Queues getKestrelQueue() {
                Properties properties = new Properties();
                properties.setProperty("host", "localhost");
                properties.setProperty("port", "22133");
                properties.setProperty("queue", "internal-activities");
                return new KestrelQueues(properties);
            }

            private ServiceAuthorizationManager getServiceAuthorizationManager() {
                ServiceAuthorizationManager sam = new DefaultServiceAuthorizationManager();
                tv.notube.commons.model.Service twitter = new tv.notube.commons.model.Service("twitter");
                twitter.setDescription("Twitter service");
                try {
                    twitter.setEndpoint(
                            new URL("https://api.twitter.com/1/statuses/user_timeline.json")
                    );
                    twitter.setSessionEndpoint(new URL("https://api.twitter.com/oauth/request_token"));
                } catch (MalformedURLException e) {
                    // com'on.
                }
                // TODO (really high) this must be configurable
                twitter.setApikey("Vs9UkC1ZhE3pT9P4JwbA");
                twitter.setSecret("BRDzw6MFJB3whzmm1rWlzjsD5LoXJmlmYT40lhravRs");
                try {
                    sam.addHandler(
                            twitter,
                            new TwitterAuthHandler(twitter)
                    );
                } catch (ServiceAuthorizationManagerException e) {
                    final String errMsg = "error while adding twitter to this stuff";
                    throw new RuntimeException(errMsg, e);
                }

                tv.notube.commons.model.Service facebook =
                        new tv.notube.commons.model.Service("facebook");
                facebook.setDescription("Facebook Service");
                try {
                    facebook.setEndpoint(new URL("https://graph.facebook.com/me/likes&amp;limit=15"));
                } catch (MalformedURLException e) {
                    // com'on.
                }
                facebook.setApikey("313412168683100");
                facebook.setSecret("cc040c3b120491bcec98498dd81fc2a5");
                try {
                    // TODO (high) this mess needs to be configured
                    facebook.setOAuthCallback(new URL
                            ("http://api.beancounter.io/rest/user/oauth/callback/facebook/"));
                } catch (MalformedURLException e) {
                    // com'on.
                }
                try {
                    sam.addHandler(
                            facebook, new FacebookAuthHandler(facebook)
                    );
                } catch (ServiceAuthorizationManagerException e) {
                    final String errMsg = "error while adding facebook to this stuff";
                    throw new RuntimeException(errMsg, e);
                }
                return sam;
            }
        });
    }
}