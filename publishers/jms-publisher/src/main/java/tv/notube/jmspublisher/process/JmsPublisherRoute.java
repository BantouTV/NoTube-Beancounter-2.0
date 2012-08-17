package tv.notube.jmspublisher.process;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import it.rainet.portal.cms.client.integration.lightstreamer.LightstreamerDTO;
import tv.notube.commons.model.activity.ResolvedActivity;

public class JmsPublisherRoute extends RouteBuilder {
    private final String ORIGINAL_BODY_HEADER = "OriginalBody";

    private ActivityToJmsConverter activityToJmsConverter;
    private JmsTemplate jmsTemplate;

    public JmsPublisherRoute(ActivityToJmsConverter activityToJmsConverter, JmsTemplate jmsTemplate) {
        this.activityToJmsConverter = activityToJmsConverter;
        this.jmsTemplate = jmsTemplate;
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
                        LightstreamerDTO lightstreamerDTO = activityToJmsConverter
                                .wrapInExternalObject(body, originalBody);
                        exchange.getOut().setBody(lightstreamerDTO);
                    }
                })

                .filter(body().isNotNull())

                .to("log:jmsPublisher?showAll=true&multiline=true&level=DEBUG")

                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        final LightstreamerDTO dto = exchange.getIn().getBody(LightstreamerDTO.class);

                        try {

                            jmsTemplate.send(new MessageCreator() {
                                public Message createMessage(Session session) throws JMSException {
                                    log.debug("Creating Lightstreamer message [" + dto + "]");
                                    return session.createObjectMessage(dto);
                                }
                            });
                        } catch (Exception e) {
                            log.error("Error sending jms message", e);
                            throw e;
                        }
                    }
                })

                .to("log:jmsPublisher?showAll=true&multiline=true&level=DEBUG")

        ;
    }

//    protected String toEndpoint() {
//        return "jms:topic:{{topic}}?testConnectionOnStartup=true";
//    }

    protected String fromEndpoint() {
        return "kestrel://{{kestrel.queue.jms.url}}";
    }

    protected String errorEndpoint() {
        return "log:" + getClass().getSimpleName() + "?{{camel.log.options.error}}";
    }
}