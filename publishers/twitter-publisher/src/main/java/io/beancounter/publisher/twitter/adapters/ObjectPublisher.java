package io.beancounter.publisher.twitter.adapters;

import io.beancounter.commons.model.activity.Object;
import io.beancounter.commons.model.activity.Verb;
import io.beancounter.publisher.twitter.TwitterPublisherException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

/**
 * @author Enrico Candino ( enrico.candino @ gmail.com )
 */
public class ObjectPublisher implements Publisher<Object> {

    private static final Logger LOG = LoggerFactory.getLogger(ObjectPublisher.class);

    @Override
    public Status publish(Twitter twitter, Verb verb, Object object) throws TwitterPublisherException {
        String message = Trimmer.trim(object.getDescription(), object.getUrl(), 3) + " - " + object.getUrl().toString();
        Status status;
        try {
            status = twitter.updateStatus(message);
        } catch (TwitterException e) {
            final String errMessage = "Error while updating the status to [" + message + "]";
            LOG.error(errMessage);
            throw new TwitterPublisherException(errMessage, e);
        }
        return status;
    }
}
