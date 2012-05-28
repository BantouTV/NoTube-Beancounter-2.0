package tv.notube.platform.analytics;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.testng.Assert;
import org.testng.annotations.Test;
import tv.notube.platform.AbstractJerseyTestCase;

import java.io.IOException;

/**
 * Reference test case for {@link tv.notube.platform.AnalyticsService}.
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
        Assert.assertEquals(responseBody, "{\"object\":[{\"name\":\"test-analysis-1\",\"className\":\"tv.notube.platform.analytics.FakeOne\",\"query\":{\"complete\":false},\"description\":\"fake analysis 1\",\"resultClassName\":\"tv.notube.platform.analytics.FakeOneResult\",\"methodDescriptions\":[{\"name\":\"getFakeSomething\",\"parameterTypes\":[],\"description\":\"the same fake method with no parameters\"},{\"name\":\"getFakeSomething\",\"parameterTypes\":[\"java.lang.Integer\"],\"description\":\"just a fake method\"}]},{\"name\":\"test-analysis-2\",\"className\":\"com.test.fake.Second\",\"query\":{\"complete\":false},\"description\":\"fake analysis 2\",\"resultClassName\":\"com.test.fake.second.Result\",\"methodDescriptions\":[{\"name\":\"getAnotherFakeSomething\",\"parameterTypes\":[\"java.lang.String\",\"java.lang.Boolean\"],\"description\":\"just a another fake method\"}]}],\"message\":\"analysis found\",\"status\":\"OK\"}");
    }

    @Test
    public void testGetAnalysisDescription() {
        // TODO: test with asserts method getAnalysisDescripton
        Assert.assertTrue(false);
    }

    @Test
    public void testGetAnalysisResult() throws IOException {
        final String baseQuery = "analytics/analysis/%s/%s/%s?param=%s&apikey=%s";
        final String name = "test-analysis-1";
        final String user = "8c33b0e6-d3cf-4909-b04c-df93056e64a8";
        final String methodName = "getFakeSomething";
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
        logger.info("query: [" + query + "]");
        GetMethod getMethod = new GetMethod(base_uri + query);
        HttpClient client = new HttpClient();
        int result = client.executeMethod(getMethod);
        String responseBody = new String(getMethod.getResponseBody());
        logger.info("result code: " + result);
        logger.info("method: " + getMethod.getName() + " at uri: " + base_uri + query);
        logger.info("response body: " + responseBody);
        Assert.assertEquals(result, HttpStatus.SC_OK, "\"Unexpected result: [" + result + "]");
        Assert.assertEquals(responseBody, "{\"object\":\"hey, [5] is your fake.\",\"message\":\"analysis result\",\"status\":\"OK\"}");
    }

    @Test
    public void testGetAnalysisResultShouldBeReturnError() throws IOException {
        final String baseQuery = "analytics/analysis/%s/%s/%s?param=%s&apikey=%s";
        final String name = "test-analysis-1";
        final String user = "8c33b0e6-d3cf-4909-b04c-df93056e64a8";
        final String methodName = "thisMethodDoesNotExist";
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
        logger.info("query: [" + query + "]");
        GetMethod getMethod = new GetMethod(base_uri + query);
        HttpClient client = new HttpClient();
        int result = client.executeMethod(getMethod);
        String responseBody = new String(getMethod.getResponseBody());
        logger.info("result code: " + result);
        logger.info("method: " + getMethod.getName() + " at uri: " + base_uri + query);
        logger.info("response body: " + responseBody);
        Assert.assertEquals(result, HttpStatus.SC_INTERNAL_SERVER_ERROR, "Unexpected result: [" + result + "]");
        Assert.assertEquals(responseBody, "{\"object\":\"method not found\",\"message\":\"Analysis result method not found\",\"status\":\"NOK\"}");
    }

    @Test
    public void getAnalysisResultWithNoParameters() throws IOException {
        final String baseQuery = "analytics/analysis/%s/%s/%s?apikey=%s";
        final String name = "test-analysis-1";
        final String user = "8c33b0e6-d3cf-4909-b04c-df93056e64a8";
        final String methodName = "getFakeSomething";
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
        Assert.assertEquals(result, HttpStatus.SC_OK, "Unexpected result: [" + result + "]");
        Assert.assertEquals(responseBody, "{\"object\":\"hey, no parameters here.\",\"message\":\"analysis result\",\"status\":\"OK\"}");
    }
}