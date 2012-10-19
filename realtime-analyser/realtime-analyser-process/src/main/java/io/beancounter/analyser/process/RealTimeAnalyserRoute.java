package io.beancounter.analyser.process;

import com.google.inject.Inject;
import io.beancounter.analyser.Analyser;
import io.beancounter.commons.model.UserProfile;
import io.beancounter.commons.model.activity.ResolvedActivity;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;

public class RealTimeAnalyserRoute extends RouteBuilder {

    @Inject
    private Analyser analyser;

    @Override
    public void configure() throws Exception {
        errorHandler(deadLetterChannel(errorEndpoint()));

        from(activitiesEndpoint())
                .convertBodyTo(String.class)
                .unmarshal().json(JsonLibrary.Jackson, ResolvedActivity.class)
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        ResolvedActivity activity = exchange.getIn().getBody(ResolvedActivity.class);
                        analyser.analyse(activity.getActivity());
                    }
                });

        from(profilesEndpoint())
                .convertBodyTo(String.class)
                .unmarshal().json(JsonLibrary.Jackson, UserProfile.class)
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        UserProfile profile = exchange.getIn().getBody(UserProfile.class);
                        analyser.analyse(profile);
                    }
                });
    }

    protected String activitiesEndpoint() {
        return "kestrel://{{kestrel.queue.analyser.activities.url}}";
    }

    protected String profilesEndpoint() {
        return "kestrel://{{kestrel.queue.analyser.profiles.url}}";
    }

    protected String errorEndpoint() {
        return "log:" + getClass().getSimpleName() + "?{{camel.log.options.error}}";
    }
}
