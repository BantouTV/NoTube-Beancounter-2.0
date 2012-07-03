package tv.notube.resolver;

import com.google.inject.Inject;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tv.notube.commons.model.activity.Activity;

import java.util.Properties;

/**
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
public class ResolverRoute extends RouteBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResolverRoute.class);

    @Inject
    private JedisUsernameResolver resolver;

    public void configure() {
        //              ?concurrentConsumers=10&waitTimeMs=500
        from("kestrel://{{kestrel.queue.social.url}}")

                .convertBodyTo(String.class)

                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        LOGGER.debug("manually unmarshalling");
                        String jsonActivity;
                        try {
                            jsonActivity = exchange.getIn().getBody(
                                    String.class
                            );
                        } catch (Exception e) {
                            LOGGER.debug("YYY1");
                            LOGGER.debug("exception1", e);
                            e.printStackTrace();
                            throw e;
                        }
                        LOGGER.debug("jsonactivity {}", jsonActivity);
                        ObjectMapper mapper;
                        try {
                            LOGGER.debug("creating mapper");
                                mapper = new ObjectMapper();
                            LOGGER.debug("mapper created");
                        } catch (Exception e) {
                            LOGGER.debug("YYY3");
                            LOGGER.debug("exception3", e);
                            e.printStackTrace();
                            throw e;
                        }
                        Activity actual;
                        try {
                            LOGGER.debug("unmarshalling");
                         actual = mapper.readValue(jsonActivity, Activity.class);
                            LOGGER.debug("unmarshalled");
                        } catch (Exception e) {
                            LOGGER.debug("YYY2");
                            LOGGER.debug("exception2", e);
                            e.printStackTrace();
                            throw e;
                        }
                        LOGGER.debug("XXXX {}", actual);
                    }
                })


                .unmarshal().json(JsonLibrary.Jackson, Activity.class)

                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        LOGGER.debug("hi, just after the unmarshal");
                        //To change body of implemented methods use File | Settings | File Templates.
                    }
                })

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