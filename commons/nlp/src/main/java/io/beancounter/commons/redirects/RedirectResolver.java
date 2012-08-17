package io.beancounter.commons.redirects;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;

/**
 * This class resolves shorted urls.
 *
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
public class RedirectResolver {

    private final static Logger LOGGER = LoggerFactory.getLogger(RedirectResolver.class);

    public static String resolve(URL url) throws RedirectException {
        HttpURLConnection connection;
        try {
            connection = (HttpURLConnection) url.openConnection(Proxy.NO_PROXY);
        } catch (IOException e) {
            final String errMsg = "Error while opening connection for [" + url + "]";
            LOGGER.error(errMsg, e);
            throw new RedirectException(errMsg, e);
        }
        connection.addRequestProperty("User-Agent", "Mozilla/4.76");
        connection.setInstanceFollowRedirects(false);
        try {
            connection.connect();
        } catch (IOException e) {
            final String errMsg = "Error while connecting to [" + url + "]";
            LOGGER.error(errMsg, e);
            throw new RedirectException(errMsg, e);
        }
        int result;
        try {
            result = connection.getResponseCode();
        } catch (IOException e) {
            final String errMsg = "Error while getting response code for [" + url + "]";
            LOGGER.error(errMsg, e);
            throw new RedirectException(errMsg, e);
        }
        if (result == 200) {
            InputStream in;
            try {
                in = connection.getInputStream();
            } catch (IOException e) {
                connection.disconnect();
                final String errMsg = "Error while getting the input stream for [" + url + "]";
                LOGGER.error(errMsg, e);
                throw new RedirectException(errMsg, e);
            }
            String text;
            try {
                text = IOUtils.toString(in, "UTF-8");
            } catch (IOException e) {
                final String errMsg = "Error while reading the input stream for [" + url + "]";
                LOGGER.error(errMsg, e);
                throw new RedirectException(errMsg, e);
            } finally {
                connection.disconnect();
                try {
                    in.close();
                } catch (IOException e) {
                    final String errMsg = "Error while closing the input stream of [" + url + "]";
                    LOGGER.error(errMsg, e);
                    throw new RedirectException(errMsg, e);
                }
            }
            return text;
        } else if (result != 301 && result != 302) {
            connection.disconnect();
            final String errMsg = "Seem that the redirect for [" + url + "] went wrong [error code: " + result + "]";
            LOGGER.error(errMsg);
            throw new RedirectException(errMsg);
        }
        String location = connection.getHeaderField("Location");
        connection.disconnect();
        URL redirect;
        try {
            redirect = new URL(location);
        } catch (MalformedURLException e) {
            final String errMsg = "Error while creating the new url for the redirect [" + location + "]";
            LOGGER.error(errMsg, e);
            throw new RedirectException(errMsg, e);
        }
        return resolve(redirect);
    }

}