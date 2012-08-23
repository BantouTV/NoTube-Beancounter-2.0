package io.beancounter.jmspublisher.process;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.jms.core.JmsTemplate;

import io.beancounter.commons.model.activity.ResolvedActivity;
import it.rainet.portal.cms.client.integration.lightstreamer.LightstreamerDTO;

public class JmsPublisherRoute extends RouteBuilder {
    private final String ORIGINAL_BODY_HEADER = "OriginalBody";

    private ActivityToJmsConverter activityToJmsConverter;
    private JmsPublisher jmsPublisher;

    public JmsPublisherRoute(ActivityToJmsConverter activityToJmsConverter, JmsPublisher jmsPublisher) {
        this.activityToJmsConverter = activityToJmsConverter;
        this.jmsPublisher = jmsPublisher;
    }

    public void configure() {
        errorHandler(deadLetterChannel(errorEndpoint()));

        from(fromEndpoint())

                .setHeader(ORIGINAL_BODY_HEADER, body())

                .unmarshal().json(JsonLibrary.Jackson, ResolvedActivity.class)

                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        ResolvedActivity body = exchange.getIn().getBody(ResolvedActivity.class);
                        String originalBody = exchange.getIn().getHeader(ORIGINAL_BODY_HEADER, String.class);
                        LightstreamerDTO lightstreamerDTO = activityToJmsConverter.wrapInExternalObject(body,
                                originalBody);
                        exchange.getOut().setBody(lightstreamerDTO);
                    }
                })

                .filter(body().isNotNull())

                .to("log:jmsPublisher?showAll=true&multiline=true&level=DEBUG")

                .bean(jmsPublisher);
    }

    protected String fromEndpoint() {
        return "kestrel://{{kestrel.queue.jms.url}}";
    }

    protected String errorEndpoint() {
        return "log:" + getClass().getSimpleName() + "?{{camel.log.options.error}}";
    }
}