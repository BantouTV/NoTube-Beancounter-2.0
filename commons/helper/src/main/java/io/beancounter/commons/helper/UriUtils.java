package io.beancounter.commons.helper;

import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.binary.Base64;

import java.io.UnsupportedEncodingException;

public class UriUtils {

    private static final String ENCODING = "UTF-8";

    private UriUtils() {}

    public static String encodeBase64(String uri) throws EncoderException, UnsupportedEncodingException {
        return Base64.encodeBase64URLSafeString(uri.getBytes(ENCODING));
    }

    public static String decodeBase64(String encodedUri) throws UnsupportedEncodingException {
        return new String(Base64.decodeBase64(encodedUri.getBytes(ENCODING)));
    }
}
