package tv.notube.platform.user;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.codehaus.jackson.map.ObjectMapper;
import org.testng.Assert;
import org.testng.annotations.Test;
import tv.notube.commons.model.User;
import tv.notube.commons.model.UserProfile;
import tv.notube.commons.tests.Tests;
import tv.notube.commons.tests.TestsBuilder;
import tv.notube.commons.tests.TestsException;
import tv.notube.platform.APIResponse;
import tv.notube.platform.AbstractJerseyTestCase;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.LinkedHashMap;
import java.util.UUID;

/**
 * Reference test case for {@link tv.notube.platform.UserService}
 *
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
public class UserServiceTestCase extends AbstractJerseyTestCase {

    private Tests tests = TestsBuilder.getInstance().build();

    private static String APIKEY;

    protected UserServiceTestCase() {
        super(9995);
    }

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
    public void testSignUp() throws IOException {
        APIKEY = registerTestApplication().toString();
        final String baseQuery = "user/register?apikey=%s";
        final String name = "Fake_Name";
        final String surname = "Fake_Surname";
        final String username = "missing-user";
        final String password = "abc";
        final String query = String.format(
                baseQuery,
                APIKEY
        );
        PostMethod postMethod = new PostMethod(base_uri + query);
        HttpClient client = new HttpClient();
        postMethod.addParameter("name", name);
        postMethod.addParameter("surname", surname);
        postMethod.addParameter("username", username);
        postMethod.addParameter("password", password);
        int result = client.executeMethod(postMethod);
        String responseBody = new String(postMethod.getResponseBody());
        logger.info("result code: " + result);
        logger.info("response body: " + responseBody);
        Assert.assertNotEquals(responseBody, "");
        Assert.assertEquals(result, HttpStatus.SC_OK, "\"Unexpected result: [" + result + "]");
        Assert.assertEquals(responseBody.substring(0, 11), "{\"object\":\"");
        Assert.assertEquals(responseBody.substring(47), "\",\"message\":\"user successfully registered\",\"status\":\"OK\"}");
        deregisterTestApplication();
    }

    @Test
    public void testSignUpRandom() throws IOException, TestsException, URISyntaxException {
        APIKEY = registerTestApplication().toString();

        User user = tests.build(User.class).getObject();

        String baseQuery = "user/register?apikey=%s";
        String name = user.getName();
        String surname = user.getSurname();
        String username = "missing-user";
        String password = user.getPassword();
        String query = String.format(
                baseQuery,
                APIKEY
        );
        PostMethod postMethod = new PostMethod(base_uri + query);
        HttpClient client = new HttpClient();
        postMethod.addParameter("name", name);
        postMethod.addParameter("surname", surname);
        postMethod.addParameter("username", username);
        postMethod.addParameter("password", password);
        int result = client.executeMethod(postMethod);
        String responseBody = new String(postMethod.getResponseBody());
        logger.info("result code: " + result);
        logger.info("response body: " + responseBody);
        Assert.assertNotEquals(responseBody, "");
        Assert.assertEquals(result, HttpStatus.SC_OK, "\"Unexpected result: [" + result + "]");
        Assert.assertEquals(responseBody.substring(0, 11), "{\"object\":\"");
        Assert.assertEquals(responseBody.substring(47), "\",\"message\":\"user successfully registered\",\"status\":\"OK\"}");
        deregisterTestApplication();
    }

    @Test
    public void testSignUpExistingUser() throws IOException {
        APIKEY = registerTestApplication().toString();

        final String baseQuery = "user/register?apikey=%s";
        final String name = "Fake_Name";
        final String surname = "Fake_Surname";
        final String username = "test-user";
        final String password = "abc";
        final String query = String.format(
                baseQuery,
                APIKEY
        );
        PostMethod postMethod = new PostMethod(base_uri + query);
        HttpClient client = new HttpClient();
        postMethod.addParameter("name", name);
        postMethod.addParameter("surname", surname);
        postMethod.addParameter("username", username);
        postMethod.addParameter("password", password);
        int result = client.executeMethod(postMethod);
        String responseBody = new String(postMethod.getResponseBody());
        logger.info("result code: " + result);
        logger.info("response body: " + responseBody);
        Assert.assertNotEquals(responseBody, "");
        Assert.assertEquals(result, HttpStatus.SC_INTERNAL_SERVER_ERROR, "\"Unexpected result: [" + result + "]");
        Assert.assertEquals(responseBody, "{\"message\":\"username [test-user] is already taken\",\"status\":\"NOK\"}");
        deregisterTestApplication();
    }

    @Test
    public void testGetUser() throws IOException {
        APIKEY = registerTestApplication().toString();

        final String baseQuery = "user/%s?apikey=%s";
        final String name = "test-user";
        final String query = String.format(
                baseQuery,
                name,
                APIKEY
        );
        GetMethod getMethod = new GetMethod(base_uri + query);
        HttpClient client = new HttpClient();
        int result = client.executeMethod(getMethod);
        String responseBody = new String(getMethod.getResponseBody());
        logger.info("result code: " + result);
        logger.info("response body: " + responseBody);
        Assert.assertNotEquals(responseBody, "");
        Assert.assertEquals(result, HttpStatus.SC_OK, "\"Unexpected result: [" + result + "]");
        ObjectMapper mapper = new ObjectMapper();
        LinkedHashMap response = (LinkedHashMap<String, Object>)mapper.readValue(responseBody, Object.class);
        Assert.assertNotNull(response);
        deregisterTestApplication();
    }
    
    @Test
    public void testGetUserMissingUser() throws IOException {
        APIKEY = registerTestApplication().toString();

        final String baseQuery = "user/%s?apikey=%s";
        final String name = "missing-user";
        final String query = String.format(
                baseQuery,
                name,
                APIKEY
        );
        GetMethod getMethod = new GetMethod(base_uri + query);
        HttpClient client = new HttpClient();
        int result = client.executeMethod(getMethod);
        String responseBody = new String(getMethod.getResponseBody());
        logger.info("result code: " + result);
        logger.info("response body: " + responseBody);
        Assert.assertNotEquals(responseBody, "");
        Assert.assertEquals(result, HttpStatus.SC_INTERNAL_SERVER_ERROR, "\"Unexpected result: [" + result + "]");
        Assert.assertEquals(responseBody, "{\"message\":\"user [missing-user] not found\",\"status\":\"NOK\"}");
        deregisterTestApplication();
    }

    // TODO (mid) waiting for the API for pushing activities
    @Test
    public void testGetActivities() throws IOException {
        APIKEY = registerTestApplication().toString();

        final String baseQuery = "user/%s/activities?apikey=%s";
        final String name = "test-user";
        final String query = String.format(
                baseQuery,
                name,
                APIKEY
        );
        GetMethod getMethod = new GetMethod(base_uri + query);
        HttpClient client = new HttpClient();
        int result = client.executeMethod(getMethod);
        String responseBody = new String(getMethod.getResponseBody());
        logger.info("result code: " + result);
        logger.info("response body: " + responseBody);
        Assert.assertEquals(result, HttpStatus.SC_OK, "\"Unexpected result: [" + result + "]");
        deregisterTestApplication();
    }

    @Test
    public void testGetActivitiesMissingUser() throws IOException {
        APIKEY = registerTestApplication().toString();

        final String baseQuery = "user/%s/activities?apikey=%s";
        final String name = "missing-user";
        final String query = String.format(
                baseQuery,
                name,
                APIKEY
        );
        GetMethod getMethod = new GetMethod(base_uri + query);
        HttpClient client = new HttpClient();
        int result = client.executeMethod(getMethod);
        String responseBody = new String(getMethod.getResponseBody());
        logger.info("result code: " + result);
        logger.info("response body: " + responseBody);
        Assert.assertEquals(result, HttpStatus.SC_INTERNAL_SERVER_ERROR, "\"Unexpected result: [" + result + "]");
        Assert.assertEquals(responseBody, "{\"message\":\"user with username [missing-user] not found\",\"status\":\"NOK\"}");
        deregisterTestApplication();
    }

    @Test
    public void testDeleteUser() throws IOException {
        APIKEY = registerTestApplication().toString();

        final String baseQuery = "user/%s?apikey=%s";
        final String name = "test-user";
        final String query = String.format(
                baseQuery,
                name,
                APIKEY
        );
        DeleteMethod deleteMethod = new DeleteMethod(base_uri + query);
        HttpClient client = new HttpClient();
        int result = client.executeMethod(deleteMethod);
        String responseBody = new String(deleteMethod.getResponseBody());
        logger.info("result code: " + result);
        logger.info("response body: " + responseBody);
        Assert.assertNotEquals(responseBody, "");
        Assert.assertEquals(result, HttpStatus.SC_OK, "\"Unexpected result: [" + result + "]");
        Assert.assertEquals(responseBody, "{\"message\":\"user with username [test-user] deleted\",\"status\":\"OK\"}");
        deregisterTestApplication();
    }

    @Test
    public void testAuthenticate() throws IOException {
        APIKEY = registerTestApplication().toString();

        final String baseQuery = "user/%s/authenticate?apikey=%s";
        final String username = "test-user";
        final String password = "abc";
        final String query = String.format(
                baseQuery,
                username,
                APIKEY
        );
        PostMethod postMethod = new PostMethod(base_uri + query);
        HttpClient client = new HttpClient();
        postMethod.addParameter("password", password);
        int result = client.executeMethod(postMethod);
        String responseBody = new String(postMethod.getResponseBody());
        logger.info("result code: " + result);
        logger.info("response body: " + responseBody);
        Assert.assertNotEquals(responseBody, "");
        Assert.assertEquals(result, HttpStatus.SC_OK, "\"Unexpected result: [" + result + "]");
        Assert.assertEquals(responseBody, "{\"message\":\"user [test-user] authenticated\",\"status\":\"OK\"}");
        APIResponse actual = fromJson(responseBody, APIResponse.class);
        APIResponse expected = new APIResponse(
                null,
                "user [" + username + "] authenticated",
                "OK"
        );
        Assert.assertEquals(actual, expected);
        deregisterTestApplication();
    }

    @Test
    public void testGetOAuthToken() throws IOException {
        APIKEY = registerTestApplication().toString();

        final String baseQuery = "user/oauth/token/%s/%s?redirect=%s";
        final String service = "fake-service-1";
        final String username = "test-user";
        final String redirect = "http://testurl.com/";
        final String query = String.format(
                baseQuery,
                service,
                username,
                redirect
        );
        GetMethod getMethod = new GetMethod(base_uri + query);
        HttpClient client = new HttpClient();
        int result = client.executeMethod(getMethod);
        String responseBody = new String(getMethod.getResponseBody());
        logger.info("result code: " + result);
        logger.info("response body: " + responseBody);
        Assert.assertEquals(responseBody, "");
        Assert.assertEquals(result, HttpStatus.SC_OK, "\"Unexpected result: [" + result + "]");
        deregisterTestApplication();
    }

    @Test
    public void testHandleFacebookAuthCallback() throws IOException {
        APIKEY = registerTestApplication().toString();

        final String baseQuery = "user/oauth/callback/facebook/%s?code=%s";
        final String username = "test-user";
        final String code = "OAUTH-VERIFIER";
        final String query = String.format(
                baseQuery,
                username,
                code
        );
        GetMethod getMethod = new GetMethod(base_uri + query);
        HttpClient client = new HttpClient();
        int result = client.executeMethod(getMethod);
        String responseBody = new String(getMethod.getResponseBody());
        logger.info("result code: " + result);
        logger.info("response body: " + responseBody);
        Assert.assertEquals(responseBody, "");
        Assert.assertEquals(result, HttpStatus.SC_OK, "\"Unexpected result: [" + result + "]");
        deregisterTestApplication();
    }

    @Test
    public void testHandleOAuthCallback() throws IOException {
        APIKEY = registerTestApplication().toString();

        final String baseQuery = "user/oauth/callback/%s/%s?oauth_token=%s&oauth_verifier=%s";
        final String service = "fake-service-1";
        final String username = "test-user";
        final String oauthToken = "OAUTH-TOKEN";
        final String oauthVerifier = "OAUTH-VERIFIER";
        final String query = String.format(
                baseQuery,
                service,
                username,
                oauthToken,
                oauthVerifier
        );
        GetMethod getMethod = new GetMethod(base_uri + query);
        HttpClient client = new HttpClient();
        int result = client.executeMethod(getMethod);
        String responseBody = new String(getMethod.getResponseBody());
        logger.info("result code: " + result);
        logger.info("response body: " + responseBody);
        Assert.assertEquals(responseBody, "");
        Assert.assertEquals(result, HttpStatus.SC_OK, "\"Unexpected result: [" + result + "]");
        deregisterTestApplication();
    }

    @Test
    public void testHandleAuthCallback() throws IOException {
        APIKEY = registerTestApplication().toString();

        final String baseQuery = "user/auth/callback/%s/%s/%s?token=%s";
        final String service = "fake-service-1";
        final String username = "test-user";
        final String redirect = "testurl.com";
        final String token = "TOKEN";
        final String query = String.format(
                baseQuery,
                service,
                username,
                redirect,
                token
        );
        GetMethod getMethod = new GetMethod(base_uri + query);
        HttpClient client = new HttpClient();
        int result = client.executeMethod(getMethod);
        String responseBody = new String(getMethod.getResponseBody());
        logger.info("result code: " + result);
        logger.info("response body: " + responseBody);
        Assert.assertEquals(responseBody, "");
        Assert.assertEquals(result, HttpStatus.SC_OK, "\"Unexpected result: [" + result + "]");
        deregisterTestApplication();
    }

    @Test
    public void testRemoveSource() throws IOException {
        APIKEY = registerTestApplication().toString();

        final String baseQuery = "user/source/%s/%s?apikey=%s";
        final String username = "test-user";
        final String service = "fake-service-1";
        final String query = String.format(
                baseQuery,
                username,
                service,
                APIKEY
        );
        DeleteMethod deleteMethod = new DeleteMethod(base_uri + query);
        HttpClient client = new HttpClient();
        int result = client.executeMethod(deleteMethod);
        String responseBody = new String(deleteMethod.getResponseBody());
        logger.info("result code: " + result);
        logger.info("response body: " + responseBody);
        Assert.assertNotEquals(responseBody, "");
        Assert.assertEquals(result, HttpStatus.SC_OK, "\"Unexpected result: [" + result + "]");
        Assert.assertEquals(responseBody, "{\"message\":\"service [fake-service-1] removed from user [test-user]\",\"status\":\"OK\"}");
        deregisterTestApplication();
    }

    @Test
    public void testRemoveSourceWithNotExistingUser() throws IOException {
        APIKEY = registerTestApplication().toString();

        final String baseQuery = "user/source/%s/%s?apikey=%s";
        final String username = "missing-user";
        final String service = "fake-service-1";
        final String query = String.format(
                baseQuery,
                username,
                service,
                APIKEY
        );
        DeleteMethod deleteMethod = new DeleteMethod(base_uri + query);
        HttpClient client = new HttpClient();
        int result = client.executeMethod(deleteMethod);
        String responseBody = new String(deleteMethod.getResponseBody());
        logger.info("result code: " + result);
        logger.info("response body: " + responseBody);
        Assert.assertNotEquals(responseBody, "");
        Assert.assertEquals(result, HttpStatus.SC_INTERNAL_SERVER_ERROR, "\"Unexpected result: [" + result + "]");
        Assert.assertEquals(responseBody, "{\"message\":\"User [missing-user] not found!\",\"status\":\"NOK\"}");
        deregisterTestApplication();
    }

    // TODO (mid) review this test, we need to store the profile first
    @Test
    public void testGetProfile() throws IOException {
        APIKEY = registerTestApplication().toString();

        final String baseQuery = "user/%s/profile?apikey=%s";
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
        Assert.assertEquals(result, HttpStatus.SC_INTERNAL_SERVER_ERROR, "\"Unexpected result: [" + result + "]");
        deregisterTestApplication();
    }

    @Test( enabled = false )
    public void testForceUserProfiling() throws IOException {
        APIKEY = registerTestApplication().toString();

        final String baseQuery = "user/%s/profile/update?apikey=%s";
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
        Assert.assertEquals(result, HttpStatus.SC_OK, "\"Unexpected result: [" + result + "]");
        Assert.assertEquals(responseBody, "{\"message\":\"profile updated for [test-user]\",\"status\":\"OK\"}");
        deregisterTestApplication();
    }

    @Test( enabled = false )
    public void testGetProfilingStatus() throws IOException {
        APIKEY = registerTestApplication().toString();

        final String baseQuery = "user/%s/profile/status?apikey=%s";
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
        Assert.assertEquals(result, HttpStatus.SC_OK, "\"Unexpected result: [" + result + "]");
        Assert.assertEquals(responseBody, "{\"message\":\"[test-user] TEST-PROFILING-STATUS\",\"status\":\"OK\"}");
        deregisterTestApplication();
    }


    @Test( enabled = false )
    public void testGetProfilingStatusEmptyUsername() throws IOException {
        APIKEY = registerTestApplication().toString();

        final String baseQuery = "user/%s/profile/status?apikey=%s";
        final String username = "";
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
        Assert.assertEquals(result, HttpStatus.SC_NOT_FOUND, "\"Unexpected result: [" + result + "]");
        Assert.assertEquals(responseBody, "");
        deregisterTestApplication();
    }

    @Test( enabled = false )
    public void testGetProfilePie() throws IOException {

        String json = "{\"visibility\":\"PUBLIC\",\"username\":null,\"userId\":\"c5e1d997-a9fb-4488-8764-6e08269409c6\",\"interests\":[{\"resource\":\"http://dbpedia.org/resource/Lauren_London\",\"visible\":true,\"weight\":0.09459727806562604,\"activities\":[\"cb2addc4-950c-4e28-8c19-57fc53879383\",\"0bd89978-2415-4ea1-b3b0-da285a20128c\"]},{\"resource\":\"http://dbpedia.org/resource/Hey_%28band%29\",\"visible\":true,\"weight\":0.03575612534852659,\"activities\":[\"0f09ce34-2da6-4d0c-b312-8b74de1699a3\"]},{\"resource\":\"http://dbpedia.org/resource/South_East_%28London_sub_region%29\",\"visible\":true,\"weight\":0.034977805118625395,\"activities\":[\"8867d82e-9788-4fdf-8a3e-d3210eae54bb\"]},{\"resource\":\"http://dbpedia.org/resource/Hugh_Laurie\",\"visible\":true,\"weight\":0.024602610189249773,\"activities\":[\"a1b03cbf-cd03-40d8-ac77-1224c93bb082\"]},{\"resource\":\"http://dbpedia.org/resource/Forged_%28film%29\",\"visible\":true,\"weight\":0.04522886991318023,\"activities\":[\"d25ae31b-7da3-429c-a4e2-50592b13b920\"]},{\"resource\":\"http://dbpedia.org/resource/London\",\"visible\":true,\"weight\":0.6429545770402437,\"activities\":[\"5c741225-4fc2-4545-bdfc-ba023d7683ad\",\"59712d61-d336-4a03-b360-2d3e540b4695\",\"c7cb42b5-39e5-47b0-bd31-e94216b7bfb0\",\"cdbcea67-294c-4919-9b2e-bca7a53e6f99\",\"a15ab456-433e-4241-968f-f957e8133246\",\"8bb55ee0-6e0a-43e2-a3ab-1c0dae485699\",\"23ba40ce-cc42-4ef1-a86f-52c54e8b1e84\",\"4d7195c7-0448-4fa0-a591-8e1d28cb5e00\",\"d47047e5-854c-46e4-8160-169534fb0152\",\"e1efde7b-8ce4-488b-8838-08776f801275\",\"8867d82e-9788-4fdf-8a3e-d3210eae54bb\"]},{\"resource\":\"http://dbpedia.org/resource/Partner_%282008_film%29\",\"visible\":true,\"weight\":0.024602610189249773,\"activities\":[\"a1b03cbf-cd03-40d8-ac77-1224c93bb082\"]},{\"resource\":\"http://dbpedia.org/resource/Survival_%28Doctor_Who%29\",\"visible\":true,\"weight\":0.05135236018564906,\"activities\":[\"cde637e3-5630-4ea8-aa70-7757ea30e19d\"]},{\"resource\":\"http://dbpedia.org/resource/Partner_%282007_film%29\",\"visible\":true,\"weight\":0.024602610189249773,\"activities\":[\"a1b03cbf-cd03-40d8-ac77-1224c93bb082\"]},{\"resource\":\"http://dbpedia.org/resource/Sweeney_Todd_%28band%29\",\"visible\":true,\"weight\":0.021325153760399763,\"activities\":[\"8bb55ee0-6e0a-43e2-a3ab-1c0dae485699\"]}]}";
        ObjectMapper mapper = new ObjectMapper();
        UserProfile up = mapper.readValue(json, UserProfile.class);

        APIKEY = registerTestApplication().toString();

        final String baseQuery = "user/%s/profile/pie?apikey=%s";
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
        System.out.println(responseBody);

    }

}