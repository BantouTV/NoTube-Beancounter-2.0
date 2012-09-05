package io.beancounter.publisher.twitter.adapters;

import java.net.URL;

/**
 * @author Enrico Candino ( enrico.candino @ gmail.com )
 */
public class Trimmer {

    private static final int MAX_TWEET_LENGTH = 140;
    private static final int MAX_HTTP_URL_LENGTH = 20;
    private static final int MAX_HTTPS_URL_LENGTH = 21;

    /**
     * Trims the text given to fit the Tweet length (140 chars)
     *
     * @param text       the text in input
     * @param url        the url that will be in the Tweet, if present
     * @param addedChars the characters eventually added after
     */
    public static String trim(String text, URL url, int addedChars) {
        int shortenedUrlLength = 0;

        if (url.getProtocol().equals("http")) {
            shortenedUrlLength = MAX_HTTP_URL_LENGTH;
        } else if (url.getProtocol().equals("https")) {
            shortenedUrlLength = MAX_HTTPS_URL_LENGTH;
        }

        int maxCommentLength = MAX_TWEET_LENGTH - shortenedUrlLength - addedChars;

        String trimmed;
        if (text.length() > maxCommentLength) {
            trimmed = text.substring(0, maxCommentLength - 3);
            trimmed += "...";
        } else {
            return text;
        }
        return trimmed;
    }

    /**
     * Trims the text given to fit the Tweet length (140 chars)
     *
     * @param text the text in input
     * @param url  the url that will be in the Tweet, if present
     */
    public static String trim(String text, URL url) {
        return trim(text, url, 0);
    }

}