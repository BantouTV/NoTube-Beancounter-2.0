package io.beancounter.jmspublisher.process;

import io.beancounter.commons.model.notifies.Notify;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.jms.core.JmsTemplate;

import io.beancounter.commons.model.activity.ResolvedActivity;
import it.rainet.portal.cms.client.integration.lightstreamer.LightstreamerDTO;

import java.io.IOException;

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

                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        String bodyStr = exchange.getIn().getBody(String.class);
                        Object obj;
                        try {
                            obj = tryParse(bodyStr, Notify.class);
                        } catch (IOException e) {
                            // probably it's not a Notify, try with a ResolvedActivity
                            obj = tryParse(bodyStr, ResolvedActivity.class);
                        }
                        String originalBody = exchange.getIn().getHeader(
                                ORIGINAL_BODY_HEADER,
                                String.class
                        );
                        LightstreamerDTO lightstreamerDTO = null;
                        if (obj instanceof Notify) {
                            Notify notify = (Notify) obj;
                            lightstreamerDTO = activityToJmsConverter.wrapNotifyInExternalObject(notify, originalBody);
                        }
                        if (obj instanceof ResolvedActivity) {
                            ResolvedActivity ra = (ResolvedActivity) obj;
                            lightstreamerDTO = activityToJmsConverter.wrapResolvedActivityInExternalObject(
                                    ra,
                                    originalBody
                            );
                        }
                        exchange.getOut().setBody(lightstreamerDTO);
                    }

                    private <T> T tryParse(String bodyStr, Class<T> aClass) throws IOException {
                        ObjectMapper mapper = new ObjectMapper();
                        return mapper.readValue(bodyStr, aClass);
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