package io.beancounter.commons.model.activity;

/**
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class ActivityBuilderException extends Exception {

    public ActivityBuilderException(String message, Exception e) {
        super(message, e);
    }
}
