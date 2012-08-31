package io.beancounter.publisher.twitter.adapters;

import io.beancounter.commons.model.activity.Verb;
import io.beancounter.commons.model.activity.rai.TVEvent;
import io.beancounter.publisher.twitter.TwitterPublisherException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

/**
 * @author Enrico Candino ( enrico.candino @ gmail.com )
 */
public class TVEventPublisher implements Publisher<TVEvent> {

    private static final Logger LOG = LoggerFactory.getLogger(TVEventPublisher.class);

    @Override
    public Status publish(Twitter twitter, Verb verb, TVEvent tvEvent) throws TwitterPublisherException {
        String message = getMessage(verb, tvEvent);
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

    private String getMessage(Verb verb, TVEvent tvEvent) {
        String message = "";
        if(verb.equals(Verb.WATCHED)) {
            message += "Just watched at ";
        } else if(verb.equals(Verb.CHECKIN)) {
            message += "Just joined the tv event ";
        }
        message = Trimmer.trim(message + tvEvent.getName(), tvEvent.getUrl(), 3) + " - " + tvEvent.getUrl();
        return message;
    }
}
