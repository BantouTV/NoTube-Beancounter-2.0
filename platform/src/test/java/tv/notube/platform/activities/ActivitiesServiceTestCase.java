package tv.notube.platform.activities;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import tv.notube.commons.model.activity.Activity;
import tv.notube.commons.model.activity.ResolvedActivity;
import tv.notube.commons.model.activity.Tweet;
import tv.notube.commons.model.activity.Verb;
import tv.notube.commons.model.activity.rai.TVEvent;
import tv.notube.platform.APIResponse;
import tv.notube.platform.AbstractJerseyTestCase;
import tv.notube.platform.responses.ResolvedActivitiesPlatformResponse;
import tv.notube.platform.responses.ResolvedActivityPlatformResponse;
import tv.notube.platform.responses.StringPlatformResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

/**
 * Reference test case for {@link tv.notube.platform.ActivitiesService}
 *
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
public class ActivitiesServiceTestCase extends AbstractJerseyTestCase {

    private static String APIKEY;

    protected ActivitiesServiceTestCase() {
        super(9995);
    }

    @BeforeMethod
    public void registerApp() throws Exception {
        APIKEY = registerTestApplication().toString();
    }

    @AfterMethod
    public void deregisterTestApplication() throws IOException {
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
        assertNotEquals(responseBody, "");
        assertEquals(result, HttpStatus.SC_OK, "\"Unexpected result: [" + result + "]");
        APIResponse actual = fromJson(responseBody, APIResponse.class);
        APIResponse expected = new APIResponse(
                null,
                "activity successfully registered",
                "OK"
        );
        assertEquals(actual.getMessage(), expected.getMessage());
        assertEquals(actual.getStatus(), expected.getStatus());
        assertNotNull(actual.getObject());
        assertNotNull(UUID.fromString(actual.getObject()));
    }

    @Test
    public void getSingleActivity() throws IOException {
        UUID activityId = UUID.randomUUID();
        String baseQuery = "activities/%s?apikey=%s";
        String query = String.format(
                baseQuery,
                activityId.toString(),
                APIKEY
        );

        GetMethod getMethod = new GetMethod(base_uri + query);
        HttpClient client = new HttpClient();

        int result = client.executeMethod(getMethod);
        String responseBody = new String(getMethod.getResponseBody());
        logger.info("result code: " + result);
        logger.info("response body: " + responseBody);
        assertNotEquals(responseBody, "");

        ResolvedActivityPlatformResponse actual = fromJson(responseBody, ResolvedActivityPlatformResponse.class);

        assertEquals(actual.getMessage(), "activity with id [" + activityId + "] found");
        assertEquals(actual.getStatus().toString(), "OK");

        ResolvedActivity activity = actual.getObject();
        assertNotNull(activity);
        assertEquals(activity.getActivity().getId(), activityId);
    }

    @Test
    public void getNonExistentSingleActivity() throws IOException {
        UUID activityId = UUID.fromString("0ad77722-1338-4c32-9209-5b952530959a");
        String baseQuery = "activities/%s?apikey=%s";
        String query = String.format(
                baseQuery,
                activityId.toString(),
                APIKEY
        );

        GetMethod getMethod = new GetMethod(base_uri + query);
        HttpClient client = new HttpClient();

        int result = client.executeMethod(getMethod);
        String responseBody = new String(getMethod.getResponseBody());
        logger.info("result code: " + result);
        logger.info("response body: " + responseBody);
        assertNotEquals(responseBody, "");

        ResolvedActivityPlatformResponse actual = fromJson(responseBody, ResolvedActivityPlatformResponse.class);

        assertEquals(actual.getMessage(), "no activity with id [" + activityId + "]");
        assertEquals(actual.getStatus().toString(), "OK");
        assertNull(actual.getObject());
    }

    @Test
    public void getSingleActivityWithInvalidApiKeyShouldRespondWithError() throws IOException {
        UUID activityId = UUID.randomUUID();
        String baseQuery = "activities/%s?apikey=%s";
        String query = String.format(
                baseQuery,
                activityId.toString(),
                "123456789abcdef-invalid"
        );

        GetMethod getMethod = new GetMethod(base_uri + query);
        HttpClient client = new HttpClient();

        int result = client.executeMethod(getMethod);
        String responseBody = new String(getMethod.getResponseBody());
        logger.info("result code: " + result);
        logger.info("response body: " + responseBody);
        assertNotEquals(responseBody, "");

        StringPlatformResponse actual = fromJson(responseBody, StringPlatformResponse.class);

        assertEquals(actual.getMessage(), "Your apikey is not well formed");
        assertEquals(actual.getStatus().toString(), "NOK");
    }

    @Test
    public void getSingleActivityWithWrongParametersShouldRespondWithError() throws IOException {
        UUID activityId = UUID.randomUUID();
        String baseQuery = "activities/%s";
        String query = String.format(
                baseQuery,
                activityId.toString()
        );

        GetMethod getMethod = new GetMethod(base_uri + query);
        HttpClient client = new HttpClient();

        int result = client.executeMethod(getMethod);
        String responseBody = new String(getMethod.getResponseBody());
        logger.info("result code: " + result);
        logger.info("response body: " + responseBody);
        assertNotEquals(responseBody, "");

        StringPlatformResponse actual = fromJson(responseBody, StringPlatformResponse.class);

        assertEquals(actual.getMessage(), "Error while checking parameters");
        assertEquals(actual.getStatus().toString(), "NOK");
    }

    @Test
    public void hideAnExistingVisibleActivity() throws Exception {
        UUID activityId = UUID.randomUUID();
        String baseQuery = "activities/%s/visible/%s?apikey=%s";
        String query = String.format(
                baseQuery,
                activityId.toString(),
                "false",
                APIKEY
        );

        PutMethod getMethod = new PutMethod(base_uri + query);
        HttpClient client = new HttpClient();

        int result = client.executeMethod(getMethod);
        String responseBody = new String(getMethod.getResponseBody());
        logger.info("result code: " + result);
        logger.info("response body: " + responseBody);
        assertNotEquals(responseBody, "");

        StringPlatformResponse actual = fromJson(responseBody, StringPlatformResponse.class);
        assertEquals(actual.getMessage(), "activity [" + activityId + "] visibility has been modified to [false]");
        assertEquals(actual.getStatus().toString(), "OK");
    }

    @Test
    public void unhideAnExistingInvisibleActivity() throws Exception {
        UUID activityId = UUID.randomUUID();
        String baseQuery = "activities/%s/visible/%s?apikey=%s";
        String query = String.format(
                baseQuery,
                activityId.toString(),
                "true",
                APIKEY
        );

        PutMethod getMethod = new PutMethod(base_uri + query);
        HttpClient client = new HttpClient();

        int result = client.executeMethod(getMethod);
        String responseBody = new String(getMethod.getResponseBody());
        logger.info("result code: " + result);
        logger.info("response body: " + responseBody);
        assertNotEquals(responseBody, "");

        StringPlatformResponse actual = fromJson(responseBody, StringPlatformResponse.class);
        assertEquals(actual.getMessage(), "activity [" + activityId + "] visibility has been modified to [true]");
        assertEquals(actual.getStatus().toString(), "OK");
    }

    @Test
    public void hidingANonExistentActivityShouldRespondWithAnError() throws Exception {
        UUID activityId = UUID.fromString("0ad77722-1338-4c32-9209-5b952530959a");
        String baseQuery = "activities/%s/visible/%s?apikey=%s";
        String query = String.format(
                baseQuery,
                activityId.toString(),
                "false",
                APIKEY
        );

        PutMethod getMethod = new PutMethod(base_uri + query);
        HttpClient client = new HttpClient();

        int result = client.executeMethod(getMethod);
        String responseBody = new String(getMethod.getResponseBody());
        logger.info("result code: " + result);
        logger.info("response body: " + responseBody);
        assertNotEquals(responseBody, "");

        StringPlatformResponse actual = fromJson(responseBody, StringPlatformResponse.class);
        assertEquals(actual.getMessage(), "Error modifying the visibility of activity with id [" + activityId + "]");
        assertEquals(actual.getStatus().toString(), "NOK");
    }

    @Test
    public void testGetAllActivitiesDescendingDefault() throws IOException {
        final String baseQuery = "activities/all/%s?apikey=%s";
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
        assertNotEquals(responseBody, "");

        ResolvedActivitiesPlatformResponse actual = fromJson(responseBody, ResolvedActivitiesPlatformResponse.class);
        APIResponse expected = new APIResponse(
                null,
                "user 'test-user' activities found.",
                "OK"
        );

        assertEquals(actual.getMessage(), expected.getMessage());
        assertEquals(actual.getStatus().toString(), expected.getStatus());

        List<ResolvedActivity> activities = new ArrayList<ResolvedActivity>(actual.getObject());
        assertNotNull(activities);
        assertEquals(activities.size(), 20);
        for (int i = 0; i < activities.size(); i++) {
            Tweet tweet = (Tweet) activities.get(i).getActivity().getObject();
            assertEquals(tweet.getText(), "Fake text #" + i);
        }
    }

    @Test
    public void testGetAllActivitiesDescendingNormal() throws IOException {
        final String baseQuery = "activities/all/%s?page=1&apikey=%s";
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
        assertNotEquals(responseBody, "");

        ResolvedActivitiesPlatformResponse actual = fromJson(responseBody, ResolvedActivitiesPlatformResponse.class);
        APIResponse expected = new APIResponse(
                null,
                "user 'test-user' activities found.",
                "OK"
        );

        assertEquals(actual.getMessage(), expected.getMessage());
        assertEquals(actual.getStatus().toString(), expected.getStatus());

        List<ResolvedActivity> activities = new ArrayList<ResolvedActivity>(actual.getObject());
        assertNotNull(activities);
        assertEquals(activities.size(), 20);

        int i = 20;
        for (ResolvedActivity activity : activities) {
            Tweet tweet = (Tweet) activity.getActivity().getObject();
            assertEquals(tweet.getText(), "Fake text #" + i++);
        }
    }

    @Test
    public void testGetAllActivitiesDescendingMore() throws IOException {
        final String baseQuery = "activities/all/%s?page=2&order=%s&apikey=%s";
        final String username = "test-user";
        final String query = String.format(
                baseQuery,
                username,
                "desc",
                APIKEY
        );

        GetMethod getMethod = new GetMethod(base_uri + query);
        HttpClient client = new HttpClient();

        int result = client.executeMethod(getMethod);
        String responseBody = new String(getMethod.getResponseBody());
        logger.info("result code: " + result);
        logger.info("response body: " + responseBody);
        assertNotEquals(responseBody, "");

        ResolvedActivitiesPlatformResponse actual = fromJson(responseBody, ResolvedActivitiesPlatformResponse.class);
        APIResponse expected = new APIResponse(
                null,
                "user 'test-user' activities found.",
                "OK"
        );

        assertEquals(actual.getMessage(), expected.getMessage());
        assertEquals(actual.getStatus().toString(), expected.getStatus());

        List<ResolvedActivity> activities = new ArrayList<ResolvedActivity>(actual.getObject());
        assertNotNull(activities);
        assertEquals(activities.size(), 10);

        int i = 40;
        for (ResolvedActivity activity : activities) {
            Tweet tweet = (Tweet) activity.getActivity().getObject();
            assertEquals(tweet.getText(), "Fake text #" + i++);
        }
    }

    @Test
    public void testGetAllActivitiesDescendingTooMany() throws IOException {
        final String baseQuery = "activities/all/%s?page=3&apikey=%s";
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
        assertNotEquals(responseBody, "");

        ResolvedActivitiesPlatformResponse actual = fromJson(responseBody, ResolvedActivitiesPlatformResponse.class);
        APIResponse expected = new APIResponse(
                null,
                "user 'test-user' has no more activities.",
                "OK"
        );

        assertEquals(actual.getMessage(), expected.getMessage());
        assertEquals(actual.getStatus().toString(), expected.getStatus());
        assertNotNull(actual.getObject());
        assertEquals(actual.getObject().size(), 0);
    }

    @Test
    public void getAllActivitiesForUserWithNoActivities() throws IOException {
        final String baseQuery = "activities/all/%s?apikey=%s";
        final String username = "user-with-no-activities";
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
        assertNotEquals(responseBody, "");

        ResolvedActivitiesPlatformResponse actual = fromJson(responseBody, ResolvedActivitiesPlatformResponse.class);
        APIResponse expected = new APIResponse(
                null,
                "user 'user-with-no-activities' has no activities.",
                "OK"
        );

        assertEquals(actual.getMessage(), expected.getMessage());
        assertEquals(actual.getStatus().toString(), expected.getStatus());
        assertNotNull(actual.getObject());
        assertEquals(actual.getObject().size(), 0);
    }

    @Test
    public void testGetAllActivitiesAscendingDefault() throws IOException {
        final String baseQuery = "activities/all/%s?apikey=%s&order=%s";
        final String username = "test-user";
        final String query = String.format(
                baseQuery,
                username,
                APIKEY,
                "asc"
        );

        GetMethod getMethod = new GetMethod(base_uri + query);
        HttpClient client = new HttpClient();

        int result = client.executeMethod(getMethod);
        String responseBody = new String(getMethod.getResponseBody());
        logger.info("result code: " + result);
        logger.info("response body: " + responseBody);
        assertNotEquals(responseBody, "");

        ResolvedActivitiesPlatformResponse actual = fromJson(responseBody, ResolvedActivitiesPlatformResponse.class);
        APIResponse expected = new APIResponse(
                null,
                "user '" + username + "' activities found.",
                "OK"
        );

        assertEquals(actual.getMessage(), expected.getMessage());
        assertEquals(actual.getStatus().toString(), expected.getStatus());

        List<ResolvedActivity> activities = new ArrayList<ResolvedActivity>(actual.getObject());
        assertNotNull(activities);
        assertEquals(activities.size(), 20);
        for (int i = 0; i < activities.size(); i++) {
            Tweet tweet = (Tweet) activities.get(i).getActivity().getObject();
            assertEquals(tweet.getText(), "Fake text #" + (49 - i));
        }
    }

    @Test
    public void testGetAllActivitiesAscendingNormal() throws IOException {
        final String baseQuery = "activities/all/%s?page=1&order=%s&apikey=%s";
        final String username = "test-user";
        final String query = String.format(
                baseQuery,
                username,
                "asc",
                APIKEY
        );

        GetMethod getMethod = new GetMethod(base_uri + query);
        HttpClient client = new HttpClient();

        int result = client.executeMethod(getMethod);
        String responseBody = new String(getMethod.getResponseBody());
        logger.info("result code: " + result);
        logger.info("response body: " + responseBody);
        assertNotEquals(responseBody, "");

        ResolvedActivitiesPlatformResponse actual = fromJson(responseBody, ResolvedActivitiesPlatformResponse.class);
        APIResponse expected = new APIResponse(
                null,
                "user 'test-user' activities found.",
                "OK"
        );

        assertEquals(actual.getMessage(), expected.getMessage());
        assertEquals(actual.getStatus().toString(), expected.getStatus());

        List<ResolvedActivity> activities = new ArrayList<ResolvedActivity>(actual.getObject());
        assertNotNull(activities);
        assertEquals(activities.size(), 20);

        int i = 20;
        for (ResolvedActivity activity : activities) {
            Tweet tweet = (Tweet) activity.getActivity().getObject();
            assertEquals(tweet.getText(), "Fake text #" + (49 - i++));
        }
    }

    @Test
    public void testGetAllActivitiesAscendingMore() throws IOException {
        final String baseQuery = "activities/all/%s?page=2&order=%s&apikey=%s";
        final String username = "test-user";
        final String query = String.format(
                baseQuery,
                username,
                "asc",
                APIKEY
        );

        GetMethod getMethod = new GetMethod(base_uri + query);
        HttpClient client = new HttpClient();

        int result = client.executeMethod(getMethod);
        String responseBody = new String(getMethod.getResponseBody());
        logger.info("result code: " + result);
        logger.info("response body: " + responseBody);
        assertNotEquals(responseBody, "");

        ResolvedActivitiesPlatformResponse actual = fromJson(responseBody, ResolvedActivitiesPlatformResponse.class);
        APIResponse expected = new APIResponse(
                null,
                "user 'test-user' activities found.",
                "OK"
        );

        assertEquals(actual.getMessage(), expected.getMessage());
        assertEquals(actual.getStatus().toString(), expected.getStatus());

        List<ResolvedActivity> activities = new ArrayList<ResolvedActivity>(actual.getObject());
        assertNotNull(activities);
        assertEquals(activities.size(), 10);

        int i = 40;
        for (ResolvedActivity activity : activities) {
            Tweet tweet = (Tweet) activity.getActivity().getObject();
            assertEquals(tweet.getText(), "Fake text #" + (49 - i++));
        }
    }

    @Test
    public void getAllUserActivitiesWithInvalidSortOrderParameterReturnsErrorResponse() throws Exception {
        final String baseQuery = "activities/all/%s?order=%s&apikey=%s";
        final String username = "test-user";
        final String order = "invalid-order";
        final String query = String.format(
                baseQuery,
                username,
                order,
                APIKEY
        );

        GetMethod getMethod = new GetMethod(base_uri + query);
        HttpClient client = new HttpClient();

        int result = client.executeMethod(getMethod);
        String responseBody = new String(getMethod.getResponseBody());
        logger.info("result code: " + result);
        logger.info("response body: " + responseBody);
        assertNotEquals(responseBody, "");

        ResolvedActivitiesPlatformResponse actual = fromJson(responseBody, ResolvedActivitiesPlatformResponse.class);
        APIResponse expected = new APIResponse(
                null,
                order + " is not a valid sort order.",
                "NOK"
        );

        assertEquals(actual.getMessage(), expected.getMessage());
        assertEquals(actual.getStatus().toString(), expected.getStatus());
        assertEquals(actual.getObject(), expected.getObject());
    }

    // TODO (low): Not sure if we need this test anymore.
    @Test
    public void testGetAllActivitiesDifferentPages() throws IOException {
        final String baseQuery1 = "activities/all/%s?apikey=%s";
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

        final String baseQuery2 = "activities/all/%s?page=1&apikey=%s";
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

        ResolvedActivitiesPlatformResponse actual1 = fromJson(responseBody1, ResolvedActivitiesPlatformResponse.class);
        ResolvedActivitiesPlatformResponse actual2 = fromJson(responseBody2, ResolvedActivitiesPlatformResponse.class);
        assertNotEquals(actual1, actual2);
    }

    @Test
    public void searchForCustomActivity() throws Exception {
        final String baseQuery = "activities/search?path=%s&value=%s&order=%s&apikey=%s";
        final String query = String.format(
                baseQuery,
                "type",
                "RAI-CONTENT-ITEM",
                "desc",
                APIKEY
        );

        GetMethod getMethod = new GetMethod(base_uri + query);
        HttpClient client = new HttpClient();

        int result = client.executeMethod(getMethod);
        String responseBody = new String(getMethod.getResponseBody());
        logger.info("result code: " + result);
        logger.info("response body: " + responseBody);
        assertNotEquals(responseBody, "");

        ResolvedActivitiesPlatformResponse actual = fromJson(responseBody, ResolvedActivitiesPlatformResponse.class);
        APIResponse expected = new APIResponse(
                null,
                "search for [type=RAI-CONTENT-ITEM] found activities.",
                "OK"
        );

        assertEquals(actual.getMessage(), expected.getMessage());
        assertEquals(actual.getStatus().toString(), expected.getStatus());

        List<ResolvedActivity> activities = new ArrayList<ResolvedActivity>(actual.getObject());
        assertNotNull(activities);
        assertEquals(activities.size(), 1);

        TVEvent tvEvent = (TVEvent) activities.get(0).getActivity().getObject();
        assertEquals(tvEvent.getName(), "Euro 2012");
    }

    @Test
    public void searchForAllTweetsMostRecentFirst() throws Exception {
        int i = 0;
        int page = 0;
        int tweetCount = 0;

        while (true) {
            String baseQuery = "activities/search?path=%s&value=%s&page=%d&apikey=%s";
            String query = String.format(
                    baseQuery,
                    "type",
                    Verb.TWEET.name(),
                    page++,
                    APIKEY
            );

            GetMethod getMethod = new GetMethod(base_uri + query);
            HttpClient client = new HttpClient();

            int result = client.executeMethod(getMethod);
            String responseBody = new String(getMethod.getResponseBody());
            logger.info("result code: " + result);
            logger.info("response body: " + responseBody);
            assertNotEquals(responseBody, "");

            ResolvedActivitiesPlatformResponse actual = fromJson(responseBody, ResolvedActivitiesPlatformResponse.class);

            if (actual.getObject().isEmpty()) {
                APIResponse expected = new APIResponse(
                        null,
                        "search for [type=TWEET] found no more activities.",
                        "OK"
                );
                assertEquals(actual.getStatus().toString(), expected.getStatus());
                assertEquals(actual.getMessage(), expected.getMessage());
                assertNotNull(actual.getObject());
                assertEquals(actual.getObject().size(), 0);
                break;
            }

            APIResponse expected = new APIResponse(
                    null,
                    "search for [type=TWEET] found activities.",
                    "OK"
            );

            assertEquals(actual.getMessage(), expected.getMessage());
            assertEquals(actual.getStatus().toString(), expected.getStatus());

            List<ResolvedActivity> activities = new ArrayList<ResolvedActivity>(actual.getObject());
            for (ResolvedActivity activity : activities) {
                Tweet tweet = (Tweet) activity.getActivity().getObject();
                assertEquals(tweet.getText(), "Fake text #" + i++);
            }
            tweetCount += activities.size();
        }

        assertEquals(tweetCount, 50);
    }

    @Test
    public void searchForAllTweetsEarliestFirst() throws Exception {
        int i = 0;
        int page = 0;
        int tweetCount = 0;

        while (true) {
            String baseQuery = "activities/search?path=%s&value=%s&page=%d&order=%s&apikey=%s";
            String query = String.format(
                    baseQuery,
                    "type",
                    Verb.TWEET.name(),
                    page++,
                    "asc",
                    APIKEY
            );

            GetMethod getMethod = new GetMethod(base_uri + query);
            HttpClient client = new HttpClient();

            int result = client.executeMethod(getMethod);
            String responseBody = new String(getMethod.getResponseBody());
            logger.info("result code: " + result);
            logger.info("response body: " + responseBody);
            assertNotEquals(responseBody, "");

            ResolvedActivitiesPlatformResponse actual = fromJson(responseBody, ResolvedActivitiesPlatformResponse.class);

            if (actual.getObject().isEmpty()) {
                APIResponse expected = new APIResponse(
                        null,
                        "search for [type=TWEET] found no more activities.",
                        "OK"
                );
                assertEquals(actual.getStatus().toString(), expected.getStatus());
                assertEquals(actual.getMessage(), expected.getMessage());
                assertNotNull(actual.getObject());
                assertEquals(actual.getObject().size(), 0);
                break;
            }

            APIResponse expected = new APIResponse(
                    null,
                    "search for [type=TWEET] found activities.",
                    "OK"
            );

            assertEquals(actual.getMessage(), expected.getMessage());
            assertEquals(actual.getStatus().toString(), expected.getStatus());

            List<ResolvedActivity> activities = new ArrayList<ResolvedActivity>(actual.getObject());
            for (ResolvedActivity activity : activities) {
                Tweet tweet = (Tweet) activity.getActivity().getObject();
                assertEquals(tweet.getText(), "Fake text #" + (49 - i++));
            }
            tweetCount += activities.size();
        }

        assertEquals(tweetCount, 50);
    }

    @Test
    public void searchingWithInvalidSortOrderParameterReturnsErrorResponse() throws Exception {
        final String baseQuery = "activities/search?path=%s&value=%s&order=%s&apikey=%s";
        final String order = "invalid-order";
        final String query = String.format(
                baseQuery,
                "type",
                "TWEET",
                order,
                APIKEY
        );

        GetMethod getMethod = new GetMethod(base_uri + query);
        HttpClient client = new HttpClient();

        int result = client.executeMethod(getMethod);
        String responseBody = new String(getMethod.getResponseBody());
        logger.info("result code: " + result);
        logger.info("response body: " + responseBody);
        assertNotEquals(responseBody, "");

        ResolvedActivitiesPlatformResponse actual = fromJson(responseBody, ResolvedActivitiesPlatformResponse.class);
        APIResponse expected = new APIResponse(
                null,
                order + " is not a valid sort order.",
                "NOK"
        );

        assertEquals(actual.getMessage(), expected.getMessage());
        assertEquals(actual.getStatus().toString(), expected.getStatus());
        assertEquals(actual.getObject(), expected.getObject());
    }

    @Test
    public void searchWithWildcardsShouldFail() throws Exception {
        final String baseQuery = "activities/search?path=%s&value=%s&apikey=%s";
        final String query = String.format(
                baseQuery,
                "*",
                "*",
                APIKEY
        );

        GetMethod getMethod = new GetMethod(base_uri + query);
        HttpClient client = new HttpClient();

        int result = client.executeMethod(getMethod);
        String responseBody = new String(getMethod.getResponseBody());
        logger.info("result code: " + result);
        logger.info("response body: " + responseBody);
        assertNotEquals(responseBody, "");

        ResolvedActivitiesPlatformResponse actual = fromJson(responseBody, ResolvedActivitiesPlatformResponse.class);
        ResolvedActivitiesPlatformResponse expected = new ResolvedActivitiesPlatformResponse(
                ResolvedActivitiesPlatformResponse.Status.NOK,
                "Wildcard searches are not allowed.",
                null
        );

        assertEquals(actual.getStatus(), expected.getStatus());
        assertEquals(actual.getMessage(), expected.getMessage());
        assertEquals(actual.getObject(), expected.getObject());
    }

    @Test
    public void testCustomActivityContentItem() throws IOException {
        final String baseQuery = "activities/add/%s?apikey=%s";
        final String username = "test-user";
        final String activity = "{\n" +
                "    \"verb\": \"WATCHED\",\n" +
                "    \"object\": {\n" +
                "        \"type\": \"RAI-CONTENT-ITEM\",\n" +
                "        \"url\": \"http://www.rai.tv/dl/RaiTV/programmi/media/ContentItem-17efdae2-c803-4411-aac9-f6185bdf13de.html\",\n" +
                "        \"name\": \"test-name\",\n" +
                "        \"description\": \"test-description\",\n" +
                "        \"id\": \"17efdae2-c803-4411-aac9-f6185bdf13de\"\n" +
                "    },\n" +
                "    \"context\": {\n" +
                "        \"date\": 1342456531059,\n" +
                "        \"service\": \"rai.tv\",\n" +
                "        \"mood\": null,\n" +
                "        \"username\": \"dpalmisano\"\n" +
                "    }\n" +
                "}";
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
        assertNotEquals(responseBody, "");
        assertEquals(result, HttpStatus.SC_OK, "\"Unexpected result: [" + result + "]");
        APIResponse actual = fromJson(responseBody, APIResponse.class);
        APIResponse expected = new APIResponse(
                null,
                "activity successfully registered",
                "OK"
        );
        assertEquals(actual.getMessage(), expected.getMessage());
        assertEquals(actual.getStatus(), expected.getStatus());
    }

    @Test
    public void testCustomActivityTvEvent() throws IOException {
        final String baseQuery = "activities/add/%s?apikey=%s";
        final String username = "test-user";
        final String activity = "{\n" +
                "    \"verb\": \"CHECKIN\",\n" +
                "    \"object\": {\n" +
                "        \"type\": \"RAI-TV-EVENT\",\n" +
                "        \"url\": \"http://www.rai.tv/dl/RaiTV/programmi/media/EventItem-17efdae2-c803-4411-aac9f6185bdf13de.html\",\n" +
                "        \"name\": \"test-name\",\n" +
                "        \"description\": \"test-description\",\n" +
                "        \"id\": \"17efdae2-c803-4411-aac9-f6185bdf13de\"\n" +
                "    },\n" +
                "    \"context\": {\n" +
                "        \"date\": 1342456531059,\n" +
                "        \"service\": \"rai.tv\",\n" +
                "        \"mood\": null,\n" +
                "        \"username\": \"dpalmisano\"\n" +
                "    }\n" +
                "}";
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
        assertNotEquals(responseBody, "");
        assertEquals(result, HttpStatus.SC_OK, "\"Unexpected result: [" + result + "]");
        APIResponse actual = fromJson(responseBody, APIResponse.class);
        APIResponse expected = new APIResponse(
                null,
                "activity successfully registered",
                "OK"
        );
        assertEquals(actual.getMessage(), expected.getMessage());
        assertEquals(actual.getStatus(), expected.getStatus());
    }

    @Test
    public void testCustomActivityComment() throws IOException {
        final String baseQuery = "activities/add/%s?apikey=%s";
        final String username = "test-user";
        final String activity = "{\n" +
                "    \"verb\": \"COMMENT\",\n" +
                "    \"object\": {\n" +
                "        \"type\": \"RAI-TV-COMMENT\",\n" +
                "        \"url\": \"http://www.rai.tv/dl/RaiTV/programmi/media/Comment-17efdae2-c803-4411-aac9f6185bdf13de.html\",\n" +
                "        \"name\": \"test-name\",\n" +
                "        \"description\": \"test-description\",\n" +
                "        \"text\": \"this is a text of a comment\",\n" +
                "        \"inReplyTo\": \"17efdae2-c803-4411-aac9-f6185bdf13de\"\n" +
                "    },\n" +
                "    \"context\": {\n" +
                "        \"date\": 1342456531059,\n" +
                "        \"service\": \"rai.tv\",\n" +
                "        \"mood\": null,\n" +
                "        \"username\": \"dpalmisano\"\n" +
                "    }\n" +
                "}";
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
        assertNotEquals(responseBody, "");
        assertEquals(result, HttpStatus.SC_OK, "\"Unexpected result: [" + result + "]");
        APIResponse actual = fromJson(responseBody, APIResponse.class);
        APIResponse expected = new APIResponse(
                null,
                "activity successfully registered",
                "OK"
        );
        assertEquals(actual.getMessage(), expected.getMessage());
        assertEquals(actual.getStatus(), expected.getStatus());
    }

}