package io.beancounter.activities;

public class InvalidOrderException extends Exception {

    public InvalidOrderException(String message) {
        super(message);
    }
}
