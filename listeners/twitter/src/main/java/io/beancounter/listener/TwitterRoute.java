package io.beancounter.listener;

import java.util.List;

import com.google.inject.Inject;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.beancounter.commons.model.activity.Activity;
import io.beancounter.listener.model.TwitterTweet;
import io.beancounter.resolver.Resolver;
import io.beancounter.resolver.ResolverException;
import twitter4j.Status;

/**
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 *
 */
public class TwitterRoute extends RouteBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(TwitterRoute.class);

    public static final int TWITTER_MAX_USERID_INDEX = 4999;

    public static final String SERVICE = "twitter";

    private static String userIds;

    private static final String TWITTER_ENDPOINT_PATTERN =
            "twitter://streaming/filter?type=event&userIds=%s&consumerKey={{consumer.key}}&consumerSecret={{consumer.secret}}&accessToken={{access.token}}&accessTokenSecret={{access.token.secret}}";

    @Inject
    private Resolver resolver;

    public void configure() {
        errorHandler(deadLetterChannel(errorEndpoint()));

        from(fromRegisterChannel()).process(
                new Processor() {
                    public void process(Exchange exchange) throws Exception {
                        String userIdentifier = exchange.getIn().getBody(String.class);
                        LOGGER.info("registering a new user with id [{}] to listen to", userIdentifier);
                        userIds = userIds + "," + userIdentifier;
                        String endpointUri;
                        endpointUri = String.format(TWITTER_ENDPOINT_PATTERN, userIds);
                        RouteDefinition routeDef = getContext().getRouteDefinition("route2");
                        routeDef.getInputs().get(0).setUri(endpointUri);
                        getContext().removeRoute("route2");
                        getContext().addRouteDefinition(routeDef);
                        getContext().stop();
                        getContext().start();
                    }
                }
        );

        from(fromEndpoint())
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        LOGGER.debug("Got a tweet for processing: {}", exchange.getIn().getBody());

                        Status status = exchange.getIn().getBody(Status.class);
                        TwitterTweet twitterTweet = new TweetConverter().convert(status);
                        Activity activity = new TwitterTweetConverter().convert(twitterTweet);
                        exchange.getIn().setBody(activity);
                    }
                })

                .marshal().json(JsonLibrary.Jackson)

                .convertBodyTo(String.class)

                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        LOGGER.debug("Sending twitterTweet to the queue. {} ", exchange.getIn().getBody());
                    }
                })
                .to(toEndpoint());
    }

    protected String errorEndpoint() {
        return "log:" + getClass().getSimpleName() + "?{{camel.log.options.error}}";
    }

    protected String toEndpoint() {
        return "kestrel://{{kestrel.queue.social.url}}";
    }

    protected String fromRegisterChannel() {
        // TODO (med) this should be configurable and not embedded
        return "redis://localhost:6379?command=SUBSCRIBE&channels=register&serializer=#serializer";
    }

    protected String fromEndpoint() {
        userIds = getUserIds();
        return "twitter://streaming/filter?type=event&userIds=" + userIds
                + "&consumerKey={{consumer.key}}&consumerSecret={{consumer.secret}}&accessToken={{access.token}}&accessTokenSecret={{access.token.secret}}";
    }

    private String getUserIds() {
        List<String> userIds;
        try {
            userIds = resolver.getUserIdsFor(SERVICE, 0, TWITTER_MAX_USERID_INDEX);
        } catch (ResolverException e) {
            throw new RuntimeException(e);
        }
        return listToString(userIds);
    }

    private String listToString(List<String> userIds) {
        StringBuilder builder = new StringBuilder();
        for (String userId : userIds) {
            builder.append(userId).append(",");
        }
        if (builder.length() > 0) {
            return builder.substring(0, builder.length() - 1);
        }
        // this should act as a not existent user
        return "6235245";
    }
}

