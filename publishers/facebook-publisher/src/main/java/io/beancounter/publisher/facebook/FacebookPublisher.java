package io.beancounter.publisher.facebook;

import com.restfb.types.FacebookType;
import io.beancounter.commons.model.activity.*;
import io.beancounter.commons.model.activity.rai.TVEvent;
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
        String token;
        try {
            token = resolvedActivity.getUser().getServices().get("facebook").getSession();
        } catch (NullPointerException e) {
            final String errMessage = "Facebook service not authorized. Do you have the token?";
            LOG.error(errMessage);
            throw new FacebookPublisherException(errMessage, e);
        }

        Publisher publisher = getPublisher(activity.getObject());

        // Publish activity on user's FB feed. Should be configurable.
        FacebookType response = publisher.publishActivity(token, activity.getVerb(), activity.getObject());

        LOG.debug("Published message ID: " + response.getId());
    }

    Publisher getPublisher(io.beancounter.commons.model.activity.Object object)
            throws FacebookPublisherException{
        Class clazz = (Class) getProperties().get(object.getClass().getCanonicalName());
        Publisher publisher;
        try {
            publisher = (Publisher) clazz.newInstance();
        } catch (InstantiationException e) {
            final String errMessage = "Error while instantiating class [" + clazz + "]";
            LOG.error(errMessage);
            throw new FacebookPublisherException(errMessage, e);
        } catch (IllegalAccessException e) {
            final String errMessage = "Error while accessing [" + clazz + "]";
            LOG.error(errMessage);
            throw new FacebookPublisherException(errMessage, e);
        } catch (NullPointerException e) {
            final String errMessage = "Object not supported [" + object.getClass().getCanonicalName() + "]";
            LOG.error(errMessage);
            throw new FacebookPublisherException(errMessage, e);
        }
        return publisher;
    }

    private Properties getProperties() {
        Properties prop = new Properties();
        // TODO remove when done with RAI
        //prop.put(io.beancounter.commons.model.activity.Object.class.getCanonicalName(), ObjectPublisher.class);
        //prop.put(Comment.class.getCanonicalName(), CommentPublisher.class);
        prop.put(TVEvent.class.getCanonicalName(), TVEventPublisher.class);
        return prop;
    }
}
