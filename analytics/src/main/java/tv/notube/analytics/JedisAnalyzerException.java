package tv.notube.analytics;

import java.io.IOException;

/**
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
public class JedisAnalyzerException extends Exception {

    public JedisAnalyzerException(String s, Exception e) {
        super(s, e);
    }
}