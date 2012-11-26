package io.beancounter.listeners;

import io.beancounter.commons.model.activity.Activity;
import io.beancounter.commons.model.activity.Tweet;
import io.beancounter.listeners.model.TwitterTweet;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import twitter4j.Status;

public class TwitterEarRoute extends RouteBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(TwitterEarRoute.class);

    public void configure() {
        //errorHandler(deadLetterChannel("file:logs/error"));

        from("twitter://streaming/filter?type=event&keywords=primarie&consumerKey={{consumer.key}}&consumerSecret={{consumer.secret}}&accessToken={{access.token}}&accessTokenSecret={{access.token.secret}}")

                .process(new Processor() {
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
                    public void process(Exchange exchange) throws Exception {
                        LOGGER.debug("Sending twitterTweet to the queue. {} ", exchange.getIn().getBody());
                    }
                })

                .to("kestrel://{{kestrel.queue.url}}");

    }
}
