package io.beancounter.platform.custom;

import io.beancounter.platform.AbstractJerseyTestCase;
import io.beancounter.platform.responses.AtomicSignUpResponse;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.http.HttpStatus;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;

/**
 * Reference test case for {@link io.beancounter.platform.UserService}
 *
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
public class MyRaiTVServiceServiceTestCase extends AbstractJerseyTestCase {

    protected MyRaiTVServiceServiceTestCase() {
        super(9995);
    }

    @Test
    private void testLogin() throws IOException {
        HttpClient client = new HttpClient();
        String baseQuery = "rai/login";
        PostMethod postMethod = new PostMethod(base_uri + baseQuery);
        postMethod.addParameter("username", "Elder82");
        postMethod.addParameter("password", "innuendo");
        int result = client.executeMethod(postMethod);
        Assert.assertEquals(result, HttpStatus.SC_OK);
        String responseBody = new String(postMethod.getResponseBody());
        Assert.assertNotNull(responseBody);
        Assert.assertNotEquals(responseBody, "");
        logger.info("response: " + responseBody);
        AtomicSignUpResponse actual = fromJson(responseBody, AtomicSignUpResponse.class);
        AtomicSignUpResponse expected = new AtomicSignUpResponse(
                AtomicSignUpResponse.Status.OK,
                "user with user name [Elder82] logged in with service [myRai]",
                actual.getObject()
        );
        Assert.assertNotNull(actual.getObject());
        Assert.assertEquals(actual.getObject().getUserId(), expected.getObject().getUserId());
        Assert.assertEquals(actual.getObject().getIdentifier(), expected.getObject().getIdentifier());
        Assert.assertEquals(actual.getObject().getService(), expected.getObject().getService());
    }

    @Test
    private void testLoginWithAuth() throws IOException {
        HttpClient client = new HttpClient();
        String baseQuery = "rai/login/auth";
        PostMethod postMethod = new PostMethod(base_uri + baseQuery);
        postMethod.addParameter("username", "Elder82");
        postMethod.addParameter("token", "ccaf3525c44da2a241c263b7d1f75879");
        int result = client.executeMethod(postMethod);
        Assert.assertEquals(result, HttpStatus.SC_OK);
        String responseBody = new String(postMethod.getResponseBody());
        Assert.assertNotNull(responseBody);
        Assert.assertNotEquals(responseBody, "");
        logger.info("response: " + responseBody);
        AtomicSignUpResponse actual = fromJson(responseBody, AtomicSignUpResponse.class);
        AtomicSignUpResponse expected = new AtomicSignUpResponse(
                AtomicSignUpResponse.Status.OK,
                "user with user name [Elder82] logged in with service [myRai]",
                actual.getObject()
        );
        Assert.assertNotNull(actual.getObject());
        Assert.assertEquals(actual.getObject().getUserId(), expected.getObject().getUserId());
        Assert.assertEquals(actual.getObject().getIdentifier(), expected.getObject().getIdentifier());
        Assert.assertEquals(actual.getObject().getService(), expected.getObject().getService());
    }

}