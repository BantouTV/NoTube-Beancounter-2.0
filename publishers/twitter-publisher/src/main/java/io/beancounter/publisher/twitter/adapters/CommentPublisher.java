package io.beancounter.publisher.twitter.adapters;

import io.beancounter.commons.model.activity.Verb;
import io.beancounter.commons.model.activity.rai.Comment;
import io.beancounter.publisher.twitter.TwitterPublisherException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

/**
 * @author Enrico Candino ( enrico.candino @ gmail.com )
 */
public class CommentPublisher implements Publisher<Comment> {

    private static final Logger LOG = LoggerFactory.getLogger(CommentPublisher.class);

    @Override
     public Status publish(Twitter twitter, Verb verb, Comment comment) throws TwitterPublisherException {
        Status status;
        String message = getMessage(comment);
        try {
            status = twitter.updateStatus(message);
        } catch (TwitterException e) {
            final String errMessage = "Error while updating the status to [" + message + "]";
            LOG.error(errMessage);
            throw new TwitterPublisherException(errMessage, e);
        }
        return status;
    }

    private String getMessage(Comment comment) {
        return "" + Trimmer.trim(comment.getText(), comment.getUrl(), 3) + " - " + comment.getUrl().toString();
    }
}