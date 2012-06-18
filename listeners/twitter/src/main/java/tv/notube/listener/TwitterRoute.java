package tv.notube.listener;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;

import tv.notube.crawler.requester.request.twitter.TwitterTweet;
import twitter4j.Status;

public class TwitterRoute extends RouteBuilder {

    public void configure() {

        from("twitter://streaming/filter?type=event&keywords=test&consumerKey={{consumer.key}}&consumerSecret={{consumer.secret}}&accessToken={{access.token}}&accessTokenSecret={{access.token.secret}}")

                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        Status status = exchange.getIn().getBody(Status.class);
                        TwitterTweet twitterTweet = new TweetConverter().convert(status);
                        exchange.getIn().setBody(twitterTweet);
                    }
                })

                .marshal().json(JsonLibrary.Jackson)

                .convertBodyTo(String.class)

                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        System.out.println("sending " + exchange.getIn().getBody());


                    }
                })


        //  .unmarshal().json(JsonLibrary.Jackson, TwitterTweet.class)



        //.to("file:outbox");
                .to("kestrel://{{kestrel.queue.url}}");
//
//        from("kestrel://{{kestrel.queue.url}}?concurrentConsumers=10&waitTimeMs=500")
//                .process(new Processor() {
//                    @Override
//                    public void process(Exchange exchange) throws Exception {
//                        System.out.println("received " + exchange.getIn().getBody());
//                    }
//                });

    }
}

