package tv.notube.resolver;

import com.google.inject.Inject;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tv.notube.commons.model.activity.Activity;
import tv.notube.commons.model.activity.ResolvedActivity;

import java.util.UUID;

/**
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
public class ResolverRoute extends RouteBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResolverRoute.class);

    @Inject
    private JedisUsernameResolver resolver;

    public void configure() {
        from("kestrel://{{kestrel.queue.social.url}}?concurrentConsumers=10&waitTimeMs=500")

                .convertBodyTo(String.class)
                .unmarshal().json(JsonLibrary.Jackson, Activity.class)
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        Activity activity = exchange.getIn().getBody(Activity.class);
                        LOGGER.debug("Resolving username {}.", activity);
                        UUID userId = resolver.resolveUsername(activity);
                        if(userId == null) {
                            exchange.getOut().setBody(
                                    null
                            );
                        } else {
                            exchange.getOut().setBody(
                                    new ResolvedActivity(userId, activity)
                            );
                        }
                    }
                })
                .filter(body().isNotNull())
                .marshal().json(JsonLibrary.Jackson)
                .convertBodyTo(String.class)
                .to("kestrel://{{kestrel.queue.internal.url}}");
    }
}