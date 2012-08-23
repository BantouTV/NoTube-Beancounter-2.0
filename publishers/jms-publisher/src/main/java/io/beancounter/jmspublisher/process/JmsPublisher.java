package io.beancounter.jmspublisher.process;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import it.rainet.portal.cms.client.integration.lightstreamer.LightstreamerDTO;

public class JmsPublisher {
    private static final Logger LOG = LoggerFactory.getLogger(ActivityToJmsConverter.class);

    private final JmsTemplate jmsTemplate;

    public JmsPublisher(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    public void publish(final LightstreamerDTO dto) {

        try {

            jmsTemplate.send(new MessageCreator() {
                public Message createMessage(Session session) throws JMSException {
                    LOG.debug("Creating Lightstreamer message [" + dto + "]");
                    return session.createObjectMessage(dto);
                }
            });
        } catch (Exception e) {
            LOG.error("Error sending jms message", e);
            throw new RuntimeException(e);
        }
    }
}
