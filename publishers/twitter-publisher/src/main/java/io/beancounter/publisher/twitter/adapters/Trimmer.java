package io.beancounter.publisher.twitter.adapters;

import java.net.URL;

/**
 * @author Enrico Candino ( enrico.candino @ gmail.com )
 */
public class Trimmer {

    private static final int MAX_TWEET_LENGTH = 140;
    private static final int MAX_HTTP_URL_LENGTH = 20;
    private static final int MAX_HTTPS_URL_LENGTH = 21;

    public static String trim(String text, URL url) {
        int urlLength = url.toString().length();
        int shortenedUrlLength = 0;
        if (urlLength > 20) {
            if (url.getProtocol().equals("http")) {
                shortenedUrlLength = MAX_HTTP_URL_LENGTH;
            } else if (url.getProtocol().equals("https")) {
                shortenedUrlLength = MAX_HTTPS_URL_LENGTH;
            }
        } else {
            shortenedUrlLength = urlLength;
        }

        int maxCommentLength = MAX_TWEET_LENGTH - shortenedUrlLength;

        String trimmed;
        if (text.length() > maxCommentLength) {
            trimmed = text.substring(0, maxCommentLength - 3);
            trimmed += "...";
        } else {
            return text;
        }
        return trimmed;
    }

}