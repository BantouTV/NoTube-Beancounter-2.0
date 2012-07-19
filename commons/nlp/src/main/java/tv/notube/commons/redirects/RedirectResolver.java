package tv.notube.commons.redirects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;

/**
 *
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
public class RedirectResolver {

    private final static Logger LOGGER = LoggerFactory.getLogger(RedirectResolver.class);

    public static URL resolve(URL url) throws RedirectException {
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
        if(result == 200) {
            return url;
        } else if(result != 301 && result != 302) {
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