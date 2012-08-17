package io.beancounter.publisher.facebook;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import io.beancounter.commons.model.activity.ResolvedActivity;

public class FacebookPublisherRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        errorHandler(deadLetterChannel(errorEndpoint()));

        from(fromEndpoint())
                .unmarshal().json(JsonLibrary.Jackson, ResolvedActivity.class)
                .process(facebookPublisher());
    }

    protected String fromEndpoint() {
        return "kestrel://{{kestrel.queue.facebook.url}}";
    }

    protected String errorEndpoint() {
        return "log:" + getClass().getSimpleName() + "?{{camel.log.options.error}}";
    }

    protected FacebookPublisher facebookPublisher() {
        return new FacebookPublisher();
    }
}
