package tv.notube.platform;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;

/**
 * Reference test case for {@link AnalyticsService}.
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class AnalyticsServiceTestCase extends AbstractJerseyTestCase {

    private final static String APIKEY = "APIKEY";

    public AnalyticsServiceTestCase() {
        super(9995);
    }

    @Test
    public void testGetAvailableAnalysis() throws IOException {
        final String baseQuery = "analytics/analyses?apikey=%s";
        final String query = String.format(
                baseQuery,
                APIKEY
        );
        GetMethod getMethod = new GetMethod(base_uri + query);
        HttpClient client = new HttpClient();
        int result = client.executeMethod(getMethod);
        String responseBody = new String(getMethod.getResponseBody());
        logger.info("result code: " + result);
        logger.info("response body: " + responseBody);
        Assert.assertEquals(result, HttpStatus.SC_OK, "\"Unexpected result: [" + result + "]");
    }

    @Test
    public void getAnalysisResult() throws IOException {
        final String baseQuery = "analytics/analysis/%s/%s/%s?param=%s&apikey=%s";
        final String name = "timeframe-analysis";
        final String user = "8c33b0e6-d3cf-4909-b04c-df93056e64a8";
        final String methodName = "getStatistics";
        final String param = "5";
        final String query = String.format(
                baseQuery,
                name,
                user,
                methodName,
                param,
                APIKEY
        );
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

    @Test
    public void getAnalysisResultWithNoParameters() throws IOException {
        final String baseQuery = "analytics/analysis/%s/%s/%s?apikey=%s";
        final String name = "activity-analysis";
        final String user = "8c33b0e6-d3cf-4909-b04c-df93056e64a8";
        final String methodName = "getTotalActivities";

        final String query = String.format(
                baseQuery,
                name,
                user,
                methodName,
                APIKEY
        );

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
