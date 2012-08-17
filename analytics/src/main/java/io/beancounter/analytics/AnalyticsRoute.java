package io.beancounter.analytics;

import com.google.inject.Inject;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.beancounter.commons.model.UserProfile;

import java.util.Map;

/**
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
public class AnalyticsRoute extends RouteBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(AnalyticsRoute.class);

    @Inject
    private JedisAnalyzer analyzer;

    @Override
    public void configure() {

        from("kestrel://{{kestrel.queue.analytics}}")

                .convertBodyTo(String.class)
                .unmarshal().json(JsonLibrary.Jackson, UserProfile.class)

                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        UserProfile profile = (UserProfile) exchange.getIn().getBody();
                        Map<String, Double> newInterests = analyzer.findNewInterests(profile);
                        try {
                            analyzer.updateTrends(profile.getUserId(), newInterests);
                        } catch (JedisAnalyzerException e) {
                            final String errMsg = "Error while analyzing profile for user [" + profile.getUserId() + "]";
                            LOGGER.error(errMsg, e);
                        }
                    }
                });

    }
}