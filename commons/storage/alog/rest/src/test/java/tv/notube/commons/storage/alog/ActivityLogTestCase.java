package tv.notube.commons.storage.alog;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.UUID;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class ActivityLogTestCase extends AbstractJerseyTestCase {

    public ActivityLogTestCase() {
        super(9995);
    }

    @Test(enabled = false)
    public void testGetActivities() throws IOException {
        final String baseQuery = "activities/%s";
        final String owner = "recommender";
        final String from = "1322158200000";
        final String to =   "1322179700000";
        String q = "integer.newRecommendations = 0";
        q = URLEncoder.encode(q, "UTF-8");

        final String query = String.format(baseQuery, owner) + "?" + "from="
                + from + "&" + "to=" + to + "&" + "q=" + q;

        // Perform GET
        GetMethod getMethod = new GetMethod(base_uri + query);
        HttpClient client = new HttpClient();
        int result = client.executeMethod(getMethod);
        String responseBody = new String(getMethod.getResponseBody());
        logger.info("result code: " + result);
        logger.info("method: " + getMethod.getName() + " at uri: " + base_uri + query);
        logger.info("response body: " + responseBody);
        assert result == HttpStatus.SC_OK : "Unexpected result: \n" + result;
    }

    @Test(enabled = false)
    public void testGetActivitiesWithNoQuery() throws IOException {
        final String baseQuery = "activities/%s";
        final String owner = "recommender";
        final String from = "1322158200000";
        final String to =   "1322179700000";

        final String query = String.format(baseQuery, owner) + "?" + "from="
                + from + "&" + "to=" + to;

        // Perform GET
        GetMethod getMethod = new GetMethod(base_uri + query);
        HttpClient client = new HttpClient();
        int result = client.executeMethod(getMethod);
        String responseBody = new String(getMethod.getResponseBody());
        logger.info("result code: " + result);
        logger.info("method: " + getMethod.getName() + " at uri: " + base_uri + query);
        logger.info("response body: " + responseBody);
        assert result == HttpStatus.SC_OK : "Unexpected result: \n" + result;
    }

    @Test(enabled = false)
    public void testGetFields() throws IOException {
        final String baseQuery = "activities/fields/%s";
        final UUID activityID = UUID.fromString("0921bffc-1759-41d8-9b2c-726f50a3c511");

        final String query = String.format(baseQuery, activityID);

        // Perform GET
        GetMethod getMethod = new GetMethod(base_uri + query);
        HttpClient client = new HttpClient();
        int result = client.executeMethod(getMethod);
        String responseBody = new String(getMethod.getResponseBody());
        logger.info("result code: " + result);
        logger.info("method: " + getMethod.getName() + " at uri: " + base_uri + query);
        logger.info("response body: " + responseBody);
        assert result == HttpStatus.SC_OK : "Unexpected result: \n" + result;
    }
}
