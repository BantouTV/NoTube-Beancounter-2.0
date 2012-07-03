package tv.notube.indexer.camel;

import java.util.Properties;
import java.util.UUID;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tv.notube.activities.ActivityStore;
import tv.notube.activities.ElasticSearchActivityStoreFactory;
import tv.notube.commons.lupedia.LUpediaNLPEngineImpl;
import tv.notube.commons.model.UserProfile;
import tv.notube.commons.model.activity.Activity;
import tv.notube.commons.model.activity.Tweet;
import tv.notube.indexer.ActivityServiceImpl;
import tv.notube.profiler.DefaultProfilerImpl;
import tv.notube.profiler.Profiler;
import tv.notube.profiler.ProfilerException;
import tv.notube.profiler.rules.custom.TweetProfilingRule;
import tv.notube.profiles.Profiles;
import tv.notube.profiles.ProfilesModule;

/**
 *
 */
public class IndexerRoute extends RouteBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(IndexerRoute.class);

    private final ActivityServiceImpl activityService;

    private final Profiler profiler;

    public IndexerRoute() {
        ActivityStore activityStore = ElasticSearchActivityStoreFactory.getInstance().build();
        activityService = new ActivityServiceImpl(activityStore);
        try {
            profiler = createProfiler();
        } catch (ProfilerException e) {
            throw new RuntimeException(
                    "Error while creating the Profiler",
                    e
            );
        }
    }

    public void configure() {
        //registerActivityConverter();
        from("kestrel://{{kestrel.queue.internal.url}}?concurrentConsumers=10&waitTimeMs=500")

                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        LOGGER.debug("Got a tweet from queue.", exchange.getIn().getBody());
                    }
                })
                //.unmarshal().json(JsonLibrary.Jackson, TwitterTweet.class)
                .unmarshal().json(JsonLibrary.Jackson, Activity.class)
                //.convertBodyTo(Activity.class)
                .multicast().parallelProcessing().to("direct:es", "direct:profiler");

        from("direct:es")
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        LOGGER.debug("Storing activity {} to ES.", exchange.getIn().getBody(Activity.class));
                        activityService.store(exchange.getIn().getBody(Activity.class));
                    }
                });

        from("direct:profiler")
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        Activity activity = exchange.getIn().getBody(Activity.class);
                        LOGGER.debug("Profiling activity {}.", activity);
                        UserProfile profile = profiler.profile(
                                // TODO (high) this should be replaced with real
                                UUID.fromString("12345678-1234-1234-1234-123456789ab"),
                                exchange.getIn().getBody(Activity.class)
                        );

                    }
                });
    }

    /*
    private void registerActivityConverter() {

        getContext()
                .getTypeConverterRegistry()
                .addTypeConverter(Activity.class, TwitterTweet.class, new TypeConverterSupport() {
                    @Override
                    public <T> T convertTo(Class<T> tClass, Exchange exchange, Object o)
                            throws TypeConversionException {
                        TwitterTweet tweet = (TwitterTweet) o;
                        try {
                            return (T) new TwitterTweetConverter().convert(tweet);
                        } catch (ServiceResponseException e) {
                            throw new TypeConversionException(TwitterTweet.class, Activity.class, e);
                        }
                    }
                });
        getContext().getTypeConverterRegistry()
                .addTypeConverter(
                        Activity.class,
                        TwitterTweet.class,
                        new TypeConverterSupport() {

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
    */

    private Profiler createProfiler() throws ProfilerException {
        Properties properties = new Properties();
        // look into hashtags definition
        properties.setProperty("tagdef.enable", "true");
        // tweets are more important than other
        properties.setProperty("verb.multiplier.TWEET", "100");
        // profiles are made only of top 5 interests
        properties.setProperty("interest.limit", String.valueOf(30));
        Injector injector = Guice.createInjector(new ProfilesModule());
        Profiles profiles = injector.getInstance(Profiles.class);
        Profiler profiler = new DefaultProfilerImpl(
                profiles,
                new LUpediaNLPEngineImpl(),
                null,
                properties
        );
        profiler.registerRule(Tweet.class, TweetProfilingRule.class);
        return profiler;
    }
}
