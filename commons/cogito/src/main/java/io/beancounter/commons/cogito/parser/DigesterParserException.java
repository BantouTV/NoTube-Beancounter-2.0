package io.beancounter.commons.cogito.parser;

/**
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
public class DigesterParserException extends Exception {

    public DigesterParserException(String message, Exception e) {
        super(message, e);
    }
}