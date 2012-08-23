package io.beancounter.publisher.facebook;

import com.restfb.types.FacebookType;
import io.beancounter.commons.model.activity.*;
import io.beancounter.commons.model.activity.rai.Comment;
import io.beancounter.commons.model.activity.rai.TVEvent;
import io.beancounter.publisher.facebook.adapters.CommentPublisher;
import io.beancounter.publisher.facebook.adapters.ObjectPublisher;
import io.beancounter.publisher.facebook.adapters.Publisher;
import io.beancounter.publisher.facebook.adapters.TVEventPublisher;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 *
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
public class FacebookPublisher implements Processor {

    private static final Logger LOG = LoggerFactory.getLogger(FacebookPublisher.class);

    @Override
    public void process(Exchange exchange) throws FacebookPublisherException {
        ResolvedActivity resolvedActivity = exchange.getIn().getBody(ResolvedActivity.class);
        Activity activity = resolvedActivity.getActivity();

        // Get FB user access token.
        String token = resolvedActivity.getUser().getServices().get("facebook").getSession();

        Publisher publisher = getPublisher(activity.getObject());

        // Publish activity on user's FB feed. Should be configurable.
        FacebookType response = publisher.publishActivity(token, activity.getVerb(), activity.getObject());

        LOG.debug("Published message ID: " + response.getId());
    }

    private Publisher getPublisher(io.beancounter.commons.model.activity.Object object)
            throws FacebookPublisherException{
        String className = getProperties().getProperty(object.getClass().getCanonicalName());
        Class clazz;
        try {
            clazz = Class.forName(className);
        } catch (ClassNotFoundException e) {
            final String errMessage = "Class [" + className + "] not found";
            LOG.error(errMessage);
            throw new FacebookPublisherException(errMessage, e);
        }
        Publisher publisher;
        try {
            publisher = (Publisher) clazz.newInstance();
        } catch (InstantiationException e) {
            final String errMessage = "Error while instantiating class [" + className + "]";
            LOG.error(errMessage);
            throw new FacebookPublisherException(errMessage, e);
        } catch (IllegalAccessException e) {
            final String errMessage = "Error while accessing [" + className + "]";
            LOG.error(errMessage);
            throw new FacebookPublisherException(errMessage, e);
        }
        return publisher;
    }

    private Properties getProperties() {
        Properties prop = new Properties();
        prop.put(io.beancounter.commons.model.activity.Object.class.getCanonicalName(), ObjectPublisher.class.getCanonicalName());
        prop.put(Comment.class.getCanonicalName(), CommentPublisher.class.getCanonicalName());
        prop.put(TVEvent.class.getCanonicalName(), TVEventPublisher.class.getCanonicalName());
        return prop;
    }
}
