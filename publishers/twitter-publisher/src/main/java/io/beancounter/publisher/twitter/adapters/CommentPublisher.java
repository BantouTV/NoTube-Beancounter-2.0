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
        try {
            status = twitter.updateStatus(getMessage(comment));
        } catch (TwitterException e) {
            final String errMessage = "Error while updating the status to [" + comment.getText() + "]";
            LOG.error(errMessage);
            throw new TwitterPublisherException(errMessage, e);
        }
        return status;
    }

    private String getMessage(Comment comment) {
        String message = "Just commented on " + comment.getUrl().toString();
        message += "\n\"" + comment.getText() + "\" - " + comment.getUrl().toString();
        return message;
    }
}