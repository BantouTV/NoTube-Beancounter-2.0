package io.beancounter.publisher.twitter;

/**
 *
 * @author Enrico Candino ( enrico.candino @ gmail.com )
 */
public class TwitterPublisherException extends Exception {

    public TwitterPublisherException(String message, Exception e) {
        super(message, e);
    }
}
