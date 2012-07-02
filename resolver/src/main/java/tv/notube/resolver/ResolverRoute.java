package tv.notube.resolver;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tv.notube.commons.model.UserProfile;
import tv.notube.commons.model.activity.Activity;
import tv.notube.resolver.jedis.JedisPoolFactory;

import java.io.IOException;
import java.util.Properties;
import java.util.UUID;

/**
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
public class ResolverRoute extends RouteBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResolverRoute.class);

    private JedisUsernameResolver resolver;

    public ResolverRoute() {
        Injector injector = Guice.createInjector(new ResolverModule());
        Properties properties = new Properties();
        try {
            properties.load(getClass().getResourceAsStream("/redis.properties"));
        } catch (IOException e) {
            LOGGER.error("Couldn't load resolver properties", e);
            throw new RuntimeException("Couldn't load resolver properties");
        }
        resolver = new JedisUsernameResolver(
                injector.getInstance(JedisPoolFactory.class),
                properties
        );
    }

    public void configure() {
        //registerActivityConverter();
        from("kestrel://{{kestrel.queue.social.url}}?concurrentConsumers=10&waitTimeMs=500")

                .unmarshal().json(JsonLibrary.Jackson, Activity.class)

                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        LOGGER.debug("Got an activity from the social web.", exchange.getIn().getBody());
                        Activity activity = exchange.getIn().getBody(Activity.class);
                        LOGGER.debug("Resolving username {}.", activity);
                        Activity resolvedActivity = resolver.resolve(
                                exchange.getIn().getBody(Activity.class)
                        );
                        exchange.getOut().setBody(resolvedActivity);
                    }
                })
                .filter(body().isNotNull())
                .to("kestrel://{{kestrel.queue.internal.url}}");
    }


}