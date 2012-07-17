package tv.notube.platform.activities;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import tv.notube.platform.APIResponse;
import tv.notube.platform.AbstractJerseyTestCase;
import tv.notube.platform.responses.ActivitiesPlatformResponse;

import java.io.IOException;
import java.util.UUID;

/**
 * Reference test case for {@link tv.notube.platform.UserService}
 *
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
public class ActivitiesServiceTestCase extends AbstractJerseyTestCase {

    private static String APIKEY;

    protected ActivitiesServiceTestCase() {
        super(9995);
    }

    @BeforeTest
    private void registerApplication() throws IOException {
        APIKEY = registerTestApplication().toString();
    }

    @AfterTest
    private void deregisterTestApplication() throws IOException {
        HttpClient client = new HttpClient();
        String baseQuery = "application/" + APIKEY;
        DeleteMethod deleteMethod = new DeleteMethod(base_uri + baseQuery);
        client.executeMethod(deleteMethod);
    }

    private UUID registerTestApplication() throws IOException {
        String baseQuery = "application/register";
        final String name = "fake_application_name";
        final String description = "This is a test registration!";
        final String email = "fake_mail@test.com";
        final String oauth = "http://fakeUrlOAUTH";
        PostMethod postMethod = new PostMethod(base_uri + baseQuery);
        HttpClient client = new HttpClient();
        postMethod.addParameter("name", name);
        postMethod.addParameter("description", description);
        postMethod.addParameter("email", email);
        postMethod.addParameter("oauthCallback", oauth);
        client.executeMethod(postMethod);
        String responseBody = new String(postMethod.getResponseBody());
        APIResponse actual = fromJson(responseBody, APIResponse.class);
        return UUID.fromString(actual.getObject());
    }

    @Test
    public void testAddActivity() throws IOException {
        final String baseQuery = "activities/add/%s?apikey=%s";
        final String username = "test-user";
        final String activity = "{\"object\":" +
                "{\"type\":\"TWEET\"," +
                "\"text\":\"Just a fake tweet!\"," +
                "\"hashTags\":[\"testingBeancounter\"]," +
                "\"urls\":[\"http://fakeUrlToTest.io\"]," +
                "\"name\":\"tweet_name\"," +
                "\"description\":null," +
                "\"url\":\"http://twitter.com\"}," +
                "\"context\":" +
                "{\"date\":null," +
                "\"service\":null," +
                "\"mood\":null}," +
                "\"verb\":\"TWEET\"}";
        final String query = String.format(
                baseQuery,
                username,
                APIKEY
        );
        PostMethod postMethod = new PostMethod(base_uri + query);
        HttpClient client = new HttpClient();
        postMethod.addParameter("activity", activity);
        int result = client.executeMethod(postMethod);
        String responseBody = new String(postMethod.getResponseBody());
        logger.info("result code: " + result);
        logger.info("response body: " + responseBody);
        Assert.assertNotEquals(responseBody, "");
        Assert.assertEquals(result, HttpStatus.SC_OK, "\"Unexpected result: [" + result + "]");
        APIResponse actual = fromJson(responseBody, APIResponse.class);
        APIResponse expected = new APIResponse(
                null,
                "activity successfully registered",
                "OK"
        );
        Assert.assertEquals(actual.getMessage(), expected.getMessage());
        Assert.assertEquals(actual.getStatus(), expected.getStatus());
        Assert.assertNotNull(actual.getObject());
        Assert.assertNotNull(UUID.fromString(actual.getObject()));
    }

    @Test
    public void testGetAllActivitiesDefault() throws IOException {
        final String baseQuery = "activities/getall/%s?apikey=%s";
        final String username = "test-user";
        final String query = String.format(
                baseQuery,
                username,
                APIKEY
        );
        GetMethod getMethod = new GetMethod(base_uri + query);
        HttpClient client = new HttpClient();
        int result = client.executeMethod(getMethod);
        String responseBody = new String(getMethod.getResponseBody());
        logger.info("result code: " + result);
        logger.info("response body: " + responseBody);
        Assert.assertNotEquals(responseBody, "");
        ActivitiesPlatformResponse actual = fromJson(responseBody, ActivitiesPlatformResponse.class);
        APIResponse expected = new APIResponse(
                null,
                "user 'test-user' activities found. Next: 'http://api.beancounter.io/rest/activities/getall/test-user?page=1&apikey=" + APIKEY + "'",
                "OK"
        );
        Assert.assertEquals(actual.getMessage(), expected.getMessage());
        Assert.assertEquals(actual.getStatus().toString(), expected.getStatus());
        Assert.assertNotNull(actual.getObject());
        Assert.assertEquals(actual.getObject().size(), 20);
    }

    @Test
    public void testGetAllActivitiesNormal() throws IOException {
        final String baseQuery = "activities/getall/%s?page=1&apikey=%s";
        final String username = "test-user";
        final String query = String.format(
                baseQuery,
                username,
                APIKEY
        );
        GetMethod getMethod = new GetMethod(base_uri + query);
        HttpClient client = new HttpClient();
        int result = client.executeMethod(getMethod);
        String responseBody = new String(getMethod.getResponseBody());
        logger.info("result code: " + result);
        logger.info("response body: " + responseBody);
        Assert.assertNotEquals(responseBody, "");
        ActivitiesPlatformResponse actual = fromJson(responseBody, ActivitiesPlatformResponse.class);
        APIResponse expected = new APIResponse(
                null,
                "user 'test-user' activities found. Next: 'http://api.beancounter.io/rest/activities/getall/test-user?page=2&apikey=" + APIKEY + "'",
                "OK"
        );
        Assert.assertEquals(actual.getMessage(), expected.getMessage());
        Assert.assertEquals(actual.getStatus().toString(), expected.getStatus());
        Assert.assertNotNull(actual.getObject());
        Assert.assertEquals(actual.getObject().size(), 20);
    }

    @Test
    public void testGetAllActivitiesMore() throws IOException {
        final String baseQuery = "activities/getall/%s?page=2&apikey=%s";
        final String username = "test-user";
        final String query = String.format(
                baseQuery,
                username,
                APIKEY
        );
        GetMethod getMethod = new GetMethod(base_uri + query);
        HttpClient client = new HttpClient();
        int result = client.executeMethod(getMethod);
        String responseBody = new String(getMethod.getResponseBody());
        logger.info("result code: " + result);
        logger.info("response body: " + responseBody);
        Assert.assertNotEquals(responseBody, "");
        ActivitiesPlatformResponse actual = fromJson(responseBody, ActivitiesPlatformResponse.class);
        APIResponse expected = new APIResponse(
                null,
                "user 'test-user' activities found. Next: 'http://api.beancounter.io/rest/activities/getall/test-user?page=3&apikey=" + APIKEY + "'",
                "OK"
        );
        Assert.assertEquals(actual.getMessage(), expected.getMessage());
        Assert.assertEquals(actual.getStatus().toString(), expected.getStatus());
        Assert.assertNotNull(actual.getObject());
        Assert.assertEquals(actual.getObject().size(), 10);
    }

    @Test
    public void testGetAllActivitiesTooMany() throws IOException {
        final String baseQuery = "activities/getall/%s?page=3&apikey=%s";
        final String username = "test-user";
        final String query = String.format(
                baseQuery,
                username,
                APIKEY
        );
        GetMethod getMethod = new GetMethod(base_uri + query);
        HttpClient client = new HttpClient();
        int result = client.executeMethod(getMethod);
        String responseBody = new String(getMethod.getResponseBody());
        logger.info("result code: " + result);
        logger.info("response body: " + responseBody);
        Assert.assertNotEquals(responseBody, "");
        ActivitiesPlatformResponse actual = fromJson(responseBody, ActivitiesPlatformResponse.class);
        APIResponse expected = new APIResponse(
                null,
                "user 'test-user' activities found. Next: 'http://api.beancounter.io/rest/activities/getall/test-user?page=4&apikey=" + APIKEY + "'",
                "OK"
        );
        Assert.assertEquals(actual.getMessage(), expected.getMessage());
        Assert.assertEquals(actual.getStatus().toString(), expected.getStatus());
        Assert.assertNotNull(actual.getObject());
        Assert.assertEquals(actual.getObject().size(), 0);
    }

    @Test
    public void testGetAllActivitiesDifferentPages() throws IOException {
        final String baseQuery1 = "activities/getall/%s?apikey=%s";
        final String username = "test-user";
        final String query = String.format(
                baseQuery1,
                username,
                APIKEY
        );
        GetMethod getMethod = new GetMethod(base_uri + query);
        HttpClient client = new HttpClient();
        int result = client.executeMethod(getMethod);
        String responseBody1 = new String(getMethod.getResponseBody());
        logger.info("result code: " + result);
        logger.info("response body: " + responseBody1);

        final String baseQuery2 = "activities/getall/%s?page=1&apikey=%s";
        final String query2 = String.format(
                baseQuery2,
                username,
                APIKEY
        );
        getMethod = new GetMethod(base_uri + query2);
        client = new HttpClient();
        int result2 = client.executeMethod(getMethod);
        String responseBody2 = new String(getMethod.getResponseBody());
        logger.info("result code: " + result2);
        logger.info("response body: " + responseBody2);
        ActivitiesPlatformResponse actual1 = fromJson(responseBody1, ActivitiesPlatformResponse.class);
        ActivitiesPlatformResponse actual2 = fromJson(responseBody2, ActivitiesPlatformResponse.class);
        Assert.assertNotEquals(actual1, actual2);
    }

}