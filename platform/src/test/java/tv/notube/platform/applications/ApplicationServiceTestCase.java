package tv.notube.platform.applications;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
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
        final String name = "name";
        final String description = "description";
        final String email = "fake_mail@test.com";
        final String oauth = "OAUTH";
        PostMethod postMethod = new PostMethod(base_uri + baseQuery);
        HttpClient client = new HttpClient();
        postMethod.addParameter(name,"Fake Name");
        postMethod.addParameter(description,"This is a test registration!");
        postMethod.addParameter(email,"fake_mail@test.com");
        postMethod.addParameter(oauth,"OAUTH");
        int result = client.executeMethod(postMethod);
        String responseBody = new String(postMethod.getResponseBody());
        logger.info("result code: " + result);
        logger.info("response body: " + responseBody);
        Assert.assertEquals(result, HttpStatus.SC_OK, "\"Unexpected result: [" + result + "]");
    }

    @Test
    public void testDeregister() {
        Assert.assertTrue(false);
    }

}