package io.beancounter.publisher.twitter;

import io.beancounter.commons.helper.PropertiesHelper;
import io.beancounter.commons.model.Service;
import io.beancounter.commons.model.activity.ResolvedActivity;
import io.beancounter.commons.model.activity.rai.Comment;
import io.beancounter.commons.model.activity.rai.TVEvent;
import io.beancounter.commons.model.activity.Object;
import io.beancounter.commons.model.auth.OAuthAuth;
import io.beancounter.publisher.twitter.adapters.CommentPublisher;
import io.beancounter.publisher.twitter.adapters.ObjectPublisher;
import io.beancounter.publisher.twitter.adapters.Publisher;
import io.beancounter.publisher.twitter.adapters.TVEventPublisher;
import io.beancounter.usermanager.services.auth.DefaultServiceAuthorizationManager;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;

import java.util.Properties;

/**
 *
 * @author Enrico Candino ( enrico.candino @ gmail.com )
 */
public class TwitterPublisher implements Processor {

    private static final Logger LOG = LoggerFactory.getLogger(TwitterPublisher.class);

    @Override
    public void process(Exchange exchange) throws TwitterPublisherException {
        ResolvedActivity resolvedActivity = exchange.getIn().getBody(ResolvedActivity.class);
        Object object = resolvedActivity.getActivity().getObject();

        OAuthAuth auth = (OAuthAuth) resolvedActivity.getUser().getServices().get("twitter");
        AccessToken token;
        try {
            token = new AccessToken(auth.getSession(), auth.getSecret());
        } catch (NullPointerException e) {
            final String errMessage = "Twitter service not authorized. Do you have the token?";
            LOG.error(errMessage);
            throw new TwitterPublisherException(errMessage, e);
        }

        TwitterFactory factory = new TwitterFactory();
        Twitter twitter = factory.getInstance();

        Properties properties = PropertiesHelper.readFromClasspath("/beancounter.properties");
        Service service = DefaultServiceAuthorizationManager.buildService("twitter", properties);

        twitter.setOAuthConsumer(service.getApikey(), service.getSecret());
        twitter.setOAuthAccessToken(token);

        Publisher publisher = getPublisher(resolvedActivity.getActivity().getObject());
        Status status = publisher.publish(twitter, resolvedActivity.getActivity().getVerb(), object);

        LOG.debug("Status updated to [" + status.getText() + "]");
    }

    private Publisher getPublisher(Object object)
            throws TwitterPublisherException{
        Class clazz = (Class) getProperties().get(object.getClass().getCanonicalName());
        Publisher publisher;
        try {
            publisher = (Publisher) clazz.newInstance();
        } catch (InstantiationException e) {
            final String errMessage = "Error while instantiating class [" + clazz + "]";
            LOG.error(errMessage);
            throw new TwitterPublisherException(errMessage, e);
        } catch (IllegalAccessException e) {
            final String errMessage = "Error while accessing [" + clazz + "]";
            LOG.error(errMessage);
            throw new TwitterPublisherException(errMessage, e);
        } catch (NullPointerException e) {
            final String errMessage = "Object not supported [" + object.getClass().getCanonicalName() + "]";
            LOG.error(errMessage);
            throw new TwitterPublisherException(errMessage, e);
        }
        return publisher;
    }

    private Properties getProperties() {
        Properties prop = new Properties();
        prop.put(io.beancounter.commons.model.activity.Object.class.getCanonicalName(), ObjectPublisher.class);
        prop.put(Comment.class.getCanonicalName(), CommentPublisher.class);
        prop.put(TVEvent.class.getCanonicalName(), TVEventPublisher.class);
        return prop;
    }
}
