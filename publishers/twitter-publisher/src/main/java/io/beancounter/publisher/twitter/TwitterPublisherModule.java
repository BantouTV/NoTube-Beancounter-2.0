package io.beancounter.publisher.twitter;

import com.google.inject.Provides;
import io.beancounter.commons.helper.PropertiesHelper;
import io.beancounter.commons.model.Service;
import io.beancounter.usermanager.services.auth.DefaultServiceAuthorizationManager;
import org.apache.camel.component.properties.PropertiesComponent;
import org.apache.camel.guice.CamelModuleWithMatchingRoutes;
import org.guiceyfruit.jndi.JndiBind;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;

import java.util.Properties;

/**
 *
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
public class TwitterPublisherModule extends CamelModuleWithMatchingRoutes {

    @Override
    protected void configure() {
        super.configure();

        Properties properties = PropertiesHelper.readFromClasspath("/beancounter.properties");
        Service service = DefaultServiceAuthorizationManager.buildService("twitter", properties);

        TwitterFactory factory = new TwitterFactory();
        Twitter twitter = factory.getInstance();
        twitter.setOAuthConsumer(service.getApikey(), service.getSecret());
        bind(Twitter.class).toInstance(twitter);

        bind(TwitterPublisher.class);
        bind(TwitterPublisherRoute.class);
    }

    @Provides
    @JndiBind("properties")
    PropertiesComponent propertiesComponent() {
        PropertiesComponent pc = new PropertiesComponent();
        pc.setLocation("classpath:beancounter.properties");
        return pc;
    }
}
