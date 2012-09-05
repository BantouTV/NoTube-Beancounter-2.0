package io.beancounter.publisher.twitter.adapters;

import io.beancounter.commons.model.activity.Verb;
import io.beancounter.publisher.twitter.TwitterPublisherException;
import twitter4j.Status;
import twitter4j.Twitter;

/**
 *
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
public interface Publisher<T> {

    public Status publish(Twitter twitter, Verb verb, T t) throws TwitterPublisherException;

}
