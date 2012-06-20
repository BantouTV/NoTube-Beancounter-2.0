package tv.notube.indexer.camel;

import java.util.Properties;
import java.util.UUID;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.TypeConversionException;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.support.TypeConverterSupport;

import tv.notube.activities.ActivityStore;
import tv.notube.activities.ElasticSearchActivityStoreFactory;
import tv.notube.commons.lupedia.LUpediaNLPEngineImpl;
import tv.notube.commons.model.Interest;
import tv.notube.commons.model.UserProfile;
import tv.notube.commons.model.activity.Activity;
import tv.notube.commons.model.activity.Tweet;
import tv.notube.crawler.requester.ServiceResponseException;
import tv.notube.crawler.requester.request.twitter.TwitterTweet;
import tv.notube.indexer.ActivityServiceImpl;
import tv.notube.indexer.TwitterTweetConverter;
import tv.notube.profiler.DefaultProfilerImpl;
import tv.notube.profiler.Profiler;
import tv.notube.profiler.ProfilerException;
import tv.notube.profiler.rules.custom.TweetProfilingRule;
import tv.notube.profiles.MockProfiles;

public class IndexerRoute extends RouteBuilder {


    private final ActivityServiceImpl activityService;
    private final Profiler profiler;

    public IndexerRoute() {
        ActivityStore activityStore = ElasticSearchActivityStoreFactory.getInstance().build();
        activityService = new ActivityServiceImpl(activityStore);

        try {
            profiler = createProfiler();
        } catch (ProfilerException e) {
            throw new RuntimeException(e);
        }
    }


    public void configure() {

        registerActivityConverter();


        from("kestrel://{{kestrel.queue.url}}?concurrentConsumers=10&waitTimeMs=500")

                .unmarshal().json(JsonLibrary.Jackson, TwitterTweet.class)

                .convertBodyTo(Activity.class)

                .multicast().parallelProcessing().to("direct:es", "direct:profiler");


        from("direct:es")
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        System.out.println("indexing activity " + exchange.getIn().getBody());
                        activityService.store(exchange.getIn().getBody(Activity.class));
                    }
                })

        ;

        from("direct:profiler")

                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {

                        System.out.println("profiling activity " + exchange.getIn().getBody());

                        UserProfile profile = profiler.profile(
                                UUID.fromString(String.valueOf("12345678-1234-1234-1234-123456789ab")),
                                exchange.getIn().getBody(Activity.class));
                        System.out.println();
                        System.out.println("========================================================");
                        for (Interest i : profile.getInterests()) {
                            System.out.println(i.getResource() + " | " + i.getWeight());
                        }
                        System.out.println("========================================================");
                        System.out.println();
                    }
                })


        ;
    }


    private void registerActivityConverter() {
        getContext().getTypeConverterRegistry()
                .addTypeConverter(Activity.class, TwitterTweet.class, new TypeConverterSupport() {

                    @Override
                    public <T> T convertTo(Class<T> tClass, Exchange exchange, Object o)
                            throws TypeConversionException {

                        TwitterTweet tweet = (TwitterTweet)o;

                        try {
                            return (T)new TwitterTweetConverter().convert(tweet);
                        } catch (ServiceResponseException e) {
                            throw new TypeConversionException(TwitterTweet.class, Activity.class, e);
                        }
                    }
                });
    }


    private Profiler createProfiler() throws ProfilerException {
        Properties properties = new Properties();
        // look into hashtags definition
        properties.setProperty("tagdef.enable", "true");
        // tweets are more important than other
        properties.setProperty("verb.multiplier.TWEET", "100");
        // profiles are made only of top 5 interests
        properties.setProperty("interest.limit", String.valueOf(500));
        Profiler profiler = new DefaultProfilerImpl(
                new MockProfiles(),
                new LUpediaNLPEngineImpl(),
                null,
                properties
        );
        profiler.registerRule(Tweet.class, TweetProfilingRule.class);
        return profiler;
    }
}
