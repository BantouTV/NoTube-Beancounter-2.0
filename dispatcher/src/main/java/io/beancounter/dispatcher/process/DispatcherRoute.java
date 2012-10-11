package io.beancounter.dispatcher.process;

import java.util.HashSet;
import java.util.Set;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DispatcherRoute extends RouteBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(DispatcherRoute.class);

    @Inject
    @Named("kestrel.queue.dispatcher.prefix")
    String queuePrefix;

    @Inject
    @Named("kestrel.queue.dispatcher.queues")
    String queueNames;

    public void configure() {
        errorHandler(deadLetterChannel(errorEndpoint()));

        from(fromEndpoint())
                .multicast().parallelProcessing()
                .to(toEndpoints());

        //for monitoring the process
        from("jetty:http://0.0.0.0:34590/dispatcher/ping")
                .transform(constant("PONG\n"));
    }

    protected String[] toEndpoints() {
        LOGGER.debug("dispatching to queues: {}", queueNames);
        String[] queues = queueNames.split(",");
        Set<String> urls = new HashSet<String>();
        for (String queue : queues) {
            urls.add(queuePrefix + queue);
        }
        return urls.toArray(new String[0]);
    }

    protected String fromEndpoint() {
        return "kestrel://{{kestrel.queue.internal.url}}";
    }

    protected String errorEndpoint() {
        return "log:" + getClass().getSimpleName() + "?{{camel.log.options.error}}";
    }
}