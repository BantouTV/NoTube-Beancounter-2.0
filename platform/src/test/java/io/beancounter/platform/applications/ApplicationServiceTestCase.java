package io.beancounter.platform.applications;

import io.beancounter.platform.APIResponse;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.testng.Assert;
import org.testng.annotations.Test;
import io.beancounter.platform.AbstractJerseyTestCase;

import java.io.IOException;
import java.util.UUID;

/**
 * Reference test class for {@link io.beancounter.platform.ApplicationService}.
 *
 * @author Enrico Candino ( enrico.candino@gmail.com )
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class ApplicationServiceTestCase extends AbstractJerseyTestCase {

    public ApplicationServiceTestCase() {
        super(9995);
    }

    @Test
    public void testRegisterAndDeregister() throws IOException {
        // register application
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
        int result = client.executeMethod(postMethod);
        String responseBody = new String(postMethod.getResponseBody());
        logger.info("result code: " + result);
        logger.info("response body: " + responseBody);
        Assert.assertEquals(result, HttpStatus.SC_OK, "\"Unexpected result: [" + result + "]");
        APIResponse actual = fromJson(responseBody, APIResponse.class);
        Assert.assertNotNull(actual);
        APIResponse expected = new APIResponse(
                actual.getObject(),
                "Application '" + name + "' successfully registered",
                "OK"
        );
        Assert.assertEquals(actual, expected);
        final UUID applicationKey =UUID.fromString(actual.getObject());
        baseQuery = "application/" + applicationKey;
        DeleteMethod deleteMethod = new DeleteMethod(base_uri + baseQuery);
        result = client.executeMethod(deleteMethod);
        responseBody = new String(deleteMethod.getResponseBody());
        logger.info("result code: " + result);
        logger.info("response body: " + responseBody);
        Assert.assertEquals(result, HttpStatus.SC_OK, "\"Unexpected result: [" + result + "]");
        actual = fromJson(responseBody, APIResponse.class);
        Assert.assertNotNull(actual);
        expected = new APIResponse(
                null,
                "Application with api key '" + applicationKey + "' successfully removed",
                "OK"
        );
        Assert.assertEquals(actual, expected);
    }

    @Test
    public void testDeregisterNotExistingApplication() throws IOException {
        final UUID applicationKey = UUID.randomUUID();
        String baseQuery = "application/" + applicationKey;
        HttpClient client = new HttpClient();
        DeleteMethod deleteMethod = new DeleteMethod(base_uri + baseQuery);
        int result = client.executeMethod(deleteMethod);
        String responseBody = new String(deleteMethod.getResponseBody());
        logger.info("result code: " + result);
        logger.info("response body: " + responseBody);
        Assert.assertEquals(result, HttpStatus.SC_INTERNAL_SERVER_ERROR, "\"Unexpected result: [" + result + "]");
        APIResponse actual = fromJson(responseBody, APIResponse.class);
        Assert.assertNotNull(actual);
        APIResponse expected = new APIResponse(
                null,
                "Application with api key '" + applicationKey + "' not found",
                "NOK"
        );
        Assert.assertEquals(actual, expected);
    }

}