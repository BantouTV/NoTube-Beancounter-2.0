package io.beancounter.publisher.twitter;

import com.google.inject.Inject;
import io.beancounter.commons.helper.PropertiesHelper;
import io.beancounter.commons.model.Service;
import io.beancounter.commons.model.activity.ResolvedActivity;
import io.beancounter.commons.model.activity.rai.TVEvent;
import io.beancounter.commons.model.activity.Object;
import io.beancounter.commons.model.auth.OAuthAuth;
import io.beancounter.publisher.twitter.adapters.Publisher;
import io.beancounter.publisher.twitter.adapters.TVEventPublisher;
import io.beancounter.usermanager.services.auth.DefaultServiceAuthorizationManager;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.auth.AccessToken;

import java.util.Properties;

/**
 *
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
public class TwitterPublisher implements Processor {

    private static final Logger LOG = LoggerFactory.getLogger(TwitterPublisher.class);

    @Inject
    private Twitter twitter;

    @Override
    public void process(Exchange exchange) throws TwitterPublisherException {
        ResolvedActivity resolvedActivity = exchange.getIn().getBody(ResolvedActivity.class);
        Object object = resolvedActivity.getActivity().getObject();

        OAuthAuth auth = (OAuthAuth) resolvedActivity.getUser().getServices().get("twitter");
        if (auth == null) {
            final String errMessage = "Twitter service not authorized. Do you have the token?";
            LOG.error(errMessage);
            throw new TwitterPublisherException(errMessage, new NullPointerException());
        }
        Properties properties = PropertiesHelper.readFromClasspath("/beancounter.properties");
        Service service = DefaultServiceAuthorizationManager.buildService("twitter", properties);

        setTwitterCredential(twitter, service);
        setAccessToken(twitter, auth);

        Publisher publisher = getPublisher(resolvedActivity.getActivity().getObject());
        Status status = publisher.publish(twitter, resolvedActivity.getActivity().getVerb(), object);

        LOG.debug("Status updated to [" + status.getText() + "]");
    }

    private void setTwitterCredential(Twitter twitter, Service service) throws TwitterPublisherException {
        String apikey;
        try {
            apikey = service.getApikey();
        } catch (NullPointerException e) {
            final String errMessage = "Error while setting OAuth permission for the application. Twitter apikey not found!";
            LOG.error(errMessage);
            throw new TwitterPublisherException(errMessage, e);
        }
        String secret;
        try {
            secret = service.getSecret();
        } catch (NullPointerException e) {
            final String errMessage = "Error while setting OAuth permission for the application. Twitter secret not found!";
            LOG.error(errMessage);
            throw new TwitterPublisherException(errMessage, e);
        }
        twitter.setOAuthConsumer(apikey, secret);
    }

    private void setAccessToken(Twitter twitter, OAuthAuth auth) throws TwitterPublisherException {
        String tokenSession;
        try {
            tokenSession = auth.getSession();
        } catch (NullPointerException e) {
            final String errMessage = "Error while getting the twitter token for user. Session not found!";
            LOG.error(errMessage);
            throw new TwitterPublisherException(errMessage, e);
        }
        String tokenSecret;
        try {
            tokenSecret = auth.getSecret();
        } catch (NullPointerException e) {
            final String errMessage = "Error while getting the twitter token for user. Secret not found!";
            LOG.error(errMessage);
            throw new TwitterPublisherException(errMessage, e);
        }
        AccessToken token = getToken(tokenSession, tokenSecret);
        twitter.setOAuthAccessToken(token);
    }

    AccessToken getToken(String session, String secret) {
        return new AccessToken(session, secret);
    }

    Publisher getPublisher(Object object)
            throws TwitterPublisherException {
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
        // TODO remove when done with RAI
        //prop.put(io.beancounter.commons.model.activity.Object.class.getCanonicalName(), ObjectPublisher.class);
        //prop.put(Comment.class.getCanonicalName(), CommentPublisher.class);
        prop.put(TVEvent.class.getCanonicalName(), TVEventPublisher.class);
        return prop;
    }
}
