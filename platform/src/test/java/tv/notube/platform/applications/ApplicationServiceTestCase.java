package tv.notube.platform.applications;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.DefaultHttpParams;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.http.params.HttpParams;
import org.testng.Assert;
import org.testng.annotations.Test;
import tv.notube.platform.AbstractJerseyTestCase;

import java.io.IOException;

/**
 *
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
public class ApplicationServiceTestCase extends AbstractJerseyTestCase {

    public ApplicationServiceTestCase() {
        super(9995);
    }

    @Test
    public void testRegister() throws IOException {
        final String baseQuery = "application/register";
        final String name = "Fake_Name";
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
        Assert.assertEquals(responseBody, "{\"object\":\"APIKEY\",\"message\":\"Application 'Fake_Name' successfully registered\",\"status\":\"OK\"}");
    }

    @Test
    public void testDeregister() throws IOException {
        final String baseQuery = "application/Fake_Name";
        DeleteMethod deleteMethod = new DeleteMethod(base_uri + baseQuery);
        HttpClient client = new HttpClient();
        int result = client.executeMethod(deleteMethod);
        String responseBody = new String(deleteMethod.getResponseBody());
        logger.info("result code: " + result);
        logger.info("response body: " + responseBody);
        Assert.assertEquals(result, HttpStatus.SC_OK, "\"Unexpected result: [" + result + "]");
        Assert.assertEquals(responseBody, "{\"message\":\"Application 'Fake_Name' successfully removed\",\"status\":\"OK\"}");
    }

}