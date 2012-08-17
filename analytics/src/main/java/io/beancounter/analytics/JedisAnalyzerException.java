package io.beancounter.analytics;

/**
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
public class JedisAnalyzerException extends Exception {

    public JedisAnalyzerException(String s, Exception e) {
        super(s, e);
    }
}