package io.beancounter.profiler.hdfs;

import com.google.inject.Inject;
import io.beancounter.commons.model.UserProfile;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class ProfilerWriterRoute extends RouteBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProfilerWriterRoute.class);

    @Inject
    private ProfileWriter profileWriter;

    // TODO (low) it will be hardcoded until we are not able to make the app key related
    // to that profile available here. Or, alternatively we change our storage model which requires
    // the application identifier.
    private UUID application = UUID.fromString("18b70337-c7f0-4c9b-a38f-1d6dfddc6b22");

    @Override
    public void configure() {
        ProfileWriterShutdownStrategy shutdownStrategy = new ProfileWriterShutdownStrategy();
        shutdownStrategy.setProfileWriter(profileWriter);
        getContext().setShutdownStrategy(shutdownStrategy);
        try {
            profileWriter.init();
        } catch (ProfileWriterException e) {
            LOGGER.error("error while init connection to HDFS", e);
            throw new RuntimeException("error while init connection to HDFS", e);
        }

        errorHandler(deadLetterChannel(errorEndpoint()));

        from(fromKestrel())
                .unmarshal().json(JsonLibrary.Jackson, UserProfile.class)
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        UserProfile userProfile = exchange.getIn().getBody(UserProfile.class);
                        profileWriter.write(application, userProfile);
                        LOGGER.info("successfully updating profile for user [" + userProfile.getUsername() + "]");
                    }
                });
    }

    protected String fromKestrel() {
        return "kestrel://{{kestrel.queue.profiles.url}}?concurrentConsumers=10&waitTimeMs=500";
    }

    protected String errorEndpoint() {
        return "log:" + getClass().getSimpleName() + "?{{camel.log.options.error}}";
    }
}
