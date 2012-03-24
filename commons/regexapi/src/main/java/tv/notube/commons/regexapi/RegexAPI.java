package tv.notube.commons.regexapi;

import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import tv.notube.commons.regexapi.handlers.RegexAPIResponseHandler;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Main library class, implementing various methods
 * to access the <a href="http://dev.notu.be/2012/02/enricher_dev">regex based apis</a>
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class RegexAPI {

    private final static String CONCEPTS = "http://dev.notu.be/2012/02/enricher_dev/lookup?type=concept";

    private final static String ENTITIES = "http://dev.notu.be/2012/02/enricher_dev/lookup?type=entity";

    private final static String WEB_CONCEPTS = "http://dev.notu.be/2012/02/enricher_dev/lookup?useurl=true&type=concept";

    private final static String WEB_ENTITIES = "http://dev.notu.be/2012/02/enricher_dev/lookup?type=entity";

    private HttpClient httpClient;

    private String apikey;

    private Boolean stanbol = false;//true to use stanbol

    public RegexAPI(String apikey) {
        httpClient = new DefaultHttpClient();
        this.apikey = apikey;
    }

    public RegexAPIResponse getRankedConcept(String text) throws
            RegexAPIException {
        if (text == null) {
            throw new IllegalArgumentException("Parameter text cannot be " +
                    "null");
        }
        HttpPost method = new HttpPost(String.format(CONCEPTS, apikey));
        ResponseHandler<RegexAPIResponse> aarh = new
                RegexAPIResponseHandler(RegexAPIResponseHandler.Type.CONCEPTS);

        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
        nameValuePairs.add(new BasicNameValuePair(
                "text",
                text)
        );
        if(stanbol){
          nameValuePairs.add(new BasicNameValuePair(
                "stanbol",
                "true")
          );
        }
        try {
            method.setEntity(new UrlEncodedFormEntity(nameValuePairs));
        } catch (UnsupportedEncodingException e) {
            final String errMsg = "Encoding not supported";
            throw new RegexAPIException(errMsg, e);
        }

        try {
            return httpClient.execute(method, aarh);
        } catch (IOException e) {
            final String errMsg = "Error while calling RegexAPI";
            throw new RegexAPIException(errMsg, e);
        } finally {
            httpClient.getConnectionManager().closeExpiredConnections();
        }
    }

    public RegexAPIResponse getNamedEntities(String text) throws RegexAPIException {
        if (text == null) {
            throw new IllegalArgumentException("Parameter text cannot be " +
                    "null");
        }
        HttpPost method = new HttpPost(String.format(ENTITIES, apikey));
        ResponseHandler<RegexAPIResponse> aarh = new
                RegexAPIResponseHandler(RegexAPIResponseHandler.Type.ENTITIES);

        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
        nameValuePairs.add(new BasicNameValuePair(
                "text",
                text));
        nameValuePairs.add(new BasicNameValuePair(
                "type",
                "entity")
        );
        if(stanbol){
          nameValuePairs.add(new BasicNameValuePair(
                "stanbol",
                "true")
          );
        }
        try {
            method.setEntity(new UrlEncodedFormEntity(nameValuePairs));
        } catch (UnsupportedEncodingException e) {
            final String errMsg = "Encoding not supported";
            throw new RegexAPIException(errMsg, e);
        }

        try {
            return httpClient.execute(method, aarh);
        } catch (IOException e) {
            final String errMsg = "Error while calling RegexAPI";
            throw new RegexAPIException(errMsg, e);
        } finally {
            httpClient.getConnectionManager().closeExpiredConnections();
        }
    }

    public RegexAPIResponse getRankedConcept(URL url) throws RegexAPIException {
        if (url == null) {
            throw new IllegalArgumentException("Parameter text cannot be " +
                    "null");
        }
        HttpPost method = new HttpPost(String.format(WEB_CONCEPTS, apikey));
        ResponseHandler<RegexAPIResponse> aarh = new
                RegexAPIResponseHandler(RegexAPIResponseHandler.Type.CONCEPTS);

        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
        nameValuePairs.add(new BasicNameValuePair(
                "url",
                url.toString())
        );
        if(stanbol){
          nameValuePairs.add(new BasicNameValuePair(
                "stanbol",
                "true")
          );
        }
        try {
            method.setEntity(new UrlEncodedFormEntity(nameValuePairs));
        } catch (UnsupportedEncodingException e) {
            final String errMsg = "Encoding not supported";
            throw new RegexAPIException(errMsg, e);
        }

        try {
            return httpClient.execute(method, aarh);
        } catch (IOException e) {
            final String errMsg = "Error while calling RegexAPI";
            throw new RegexAPIException(errMsg, e);
        } finally {
            httpClient.getConnectionManager().closeExpiredConnections();
        }
    }

    public RegexAPIResponse getNamedEntities(URL url) throws RegexAPIException {
        if (url == null) {
            throw new IllegalArgumentException("Parameter text cannot be " +
                    "null");
        }
        HttpPost method = new HttpPost(String.format(WEB_ENTITIES, apikey));
        ResponseHandler<RegexAPIResponse> aarh = new
                RegexAPIResponseHandler(RegexAPIResponseHandler.Type.ENTITIES);

        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
        nameValuePairs.add(new BasicNameValuePair(
                "url",
                url.toString())
        );
        nameValuePairs.add(new BasicNameValuePair(
                "type",
                "entity")
        );
        if(stanbol){
          nameValuePairs.add(new BasicNameValuePair(
                "stanbol",
                "true")
          );
        }
        try {
            method.setEntity(new UrlEncodedFormEntity(nameValuePairs));
        } catch (UnsupportedEncodingException e) {
            final String errMsg = "Encoding not supported";
            throw new RegexAPIException(errMsg, e);
        }
        try {
            return httpClient.execute(method, aarh);
        } catch (IOException e) {
            final String errMsg = "Error while calling RegexAPI";
            throw new RegexAPIException(errMsg, e);
        } finally {
            httpClient.getConnectionManager().closeExpiredConnections();
        }
    }
}
