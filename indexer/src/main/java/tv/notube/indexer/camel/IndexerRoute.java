package tv.notube.indexer.camel;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.TypeConversionException;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.support.TypeConverterSupport;

import tv.notube.activities.ActivityStore;
import tv.notube.activities.ElasticSearchActivityStoreFactory;
import tv.notube.commons.model.activity.Activity;
import tv.notube.crawler.requester.ServiceResponseException;
import tv.notube.crawler.requester.request.twitter.TwitterTweet;
import tv.notube.indexer.ActivityServiceImpl;
import tv.notube.indexer.TwitterTweetConverter;

public class IndexerRoute extends RouteBuilder {


    private final ActivityServiceImpl activityService;

    public IndexerRoute() {
        ActivityStore activityStore = ElasticSearchActivityStoreFactory.getInstance().build();
        activityService = new ActivityServiceImpl(activityStore);

        getContext().getTypeConverterRegistry()
                .addTypeConverter(Activity.class, TwitterTweet.class, new TypeConverterSupport() {

                    @Override
                    public <T> T convertTo(Class<T> tClass, Exchange exchange, Object o)
                            throws TypeConversionException {

                        TwitterTweet tweet = (TwitterTweet)o;

                        Activity activity = null;
                        try {
                            activity = new TwitterTweetConverter().convert(tweet);
                        } catch (ServiceResponseException e) {
                            throw new TypeConversionException(TwitterTweet.class, Activity.class, e);
                        }
                        return (T)activity;
                    }
                });
    }


    public void configure() {

        from("kestrel://{{kestrel.queue.url}}?concurrentConsumers=10&waitTimeMs=500")

                .unmarshal().json(JsonLibrary.Jackson, TwitterTweet.class)

                .convertBodyTo(Activity.class)

                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {

                        System.out.println("received a tweet " + exchange.getIn().getBody());

                        activityService.store(exchange.getIn().getBody(Activity.class));

                    }
                })
        ;
    }
}
