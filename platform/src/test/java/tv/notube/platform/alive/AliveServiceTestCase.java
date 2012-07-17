package tv.notube.platform.alive;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.http.HttpStatus;
import org.testng.Assert;
import org.testng.annotations.Test;
import tv.notube.platform.APIResponse;
import tv.notube.platform.AbstractJerseyTestCase;

import java.io.IOException;

/**
 * Reference test case for {@link tv.notube.platform.UserService}
 *
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
public class AliveServiceTestCase extends AbstractJerseyTestCase {

    protected AliveServiceTestCase() {
        super(9995);
    }

    @Test
    private void testCheck() throws IOException {
        HttpClient client = new HttpClient();
        String baseQuery = "alive/check";
        GetMethod getMethod = new GetMethod(base_uri + baseQuery);
        int result = client.executeMethod(getMethod);
        Assert.assertEquals(result, HttpStatus.SC_OK);
        String responseBody = new String(getMethod.getResponseBody());
        Assert.assertNotNull(responseBody);
        Assert.assertNotEquals(responseBody, "");
        logger.info("response: " + responseBody);
        APIResponse actual = fromJson(responseBody, APIResponse.class);
        APIResponse expected = new APIResponse(
                actual.getObject(),
                "system up and running at",
                "OK"
        );
        Assert.assertEquals(actual, expected);
        Assert.assertNotNull(actual.getObject());
        Assert.assertNotNull(Long.parseLong(actual.getObject()));
    }

}