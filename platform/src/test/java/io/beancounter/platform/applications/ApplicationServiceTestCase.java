package io.beancounter.platform.applications;

import io.beancounter.platform.PlatformResponse;
import io.beancounter.platform.responses.ApplicationPlatformResponse;
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
        ApplicationPlatformResponse actual = fromJson(responseBody, ApplicationPlatformResponse.class);
        Assert.assertNotNull(actual);
        ApplicationPlatformResponse expected = new ApplicationPlatformResponse(
                PlatformResponse.Status.OK,
                "Application '" + name + "' successfully registered",
                actual.getObject()
        );
        Assert.assertEquals(actual.getStatus(), expected.getStatus());
        Assert.assertEquals(actual.getMessage(), expected.getMessage());
        Assert.assertEquals(actual.getObject(), expected.getObject());
        final UUID applicationKey = actual.getObject().getAdminKey();
        baseQuery = "application/" + applicationKey;
        DeleteMethod deleteMethod = new DeleteMethod(base_uri + baseQuery);
        result = client.executeMethod(deleteMethod);
        responseBody = new String(deleteMethod.getResponseBody());
        logger.info("result code: " + result);
        logger.info("response body: " + responseBody);
        Assert.assertEquals(result, HttpStatus.SC_OK, "\"Unexpected result: [" + result + "]");
        actual = fromJson(responseBody, ApplicationPlatformResponse.class);
        Assert.assertNotNull(actual);
        expected = new ApplicationPlatformResponse(
                PlatformResponse.Status.OK,
                "Application with api key'" + applicationKey + "' successfully removed",
                null
        );
        Assert.assertEquals(actual.getStatus(), expected.getStatus());
        Assert.assertEquals(actual.getMessage(), expected.getMessage());
        Assert.assertEquals(actual.getObject(), expected.getObject());
    }

}