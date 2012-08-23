package io.beancounter.publisher.twitter;

import io.beancounter.commons.model.activity.ResolvedActivity;
import io.beancounter.commons.model.auth.OAuthAuth;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;

/**
 *
 * @author Enrico Candino ( enrico.candino @ gmail.com )
 */
public class TwitterPublisher implements Processor {

    private static final Logger LOG = LoggerFactory.getLogger(TwitterPublisher.class);

    @Override
    public void process(Exchange exchange) throws TwitterPublisherException {
        ResolvedActivity resolvedActivity = exchange.getIn().getBody(ResolvedActivity.class);

        OAuthAuth auth = (OAuthAuth) resolvedActivity.getUser().getServices().get("twitter");
        AccessToken token = new AccessToken(auth.getSession(), auth.getSecret());

        TwitterFactory factory = new TwitterFactory();
        Twitter twitter = factory.getInstance();
        twitter.setOAuthConsumer("{{consumer.key}}", "{{consumer.secret}}");
        twitter.setOAuthAccessToken(token);

        String message = "Hello world!";

        Status status;
        try {
            status = twitter.updateStatus(message);
        } catch (TwitterException e) {
            final String errMessage = "Error while updating the status to [" + message + "]";
            LOG.error(errMessage);
            throw new TwitterPublisherException(errMessage, e);
        }

        LOG.debug("Status updated to [" + status.getText() + "]");
    }
}
