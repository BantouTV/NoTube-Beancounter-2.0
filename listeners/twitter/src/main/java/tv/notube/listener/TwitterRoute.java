package tv.notube.listener;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tv.notube.commons.model.activity.Activity;
import tv.notube.listener.model.TwitterTweet;
import twitter4j.Status;

public class TwitterRoute extends RouteBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(TwitterRoute.class);

    public void configure() {
        from("twitter://streaming/filter?keywords=London&type=event&consumerKey={{consumer.key}}&consumerSecret={{consumer.secret}}&accessToken={{access.token}}&accessTokenSecret={{access.token.secret}}")

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
                .to("kestrel://{{kestrel.queue.url}}");
    }
}

