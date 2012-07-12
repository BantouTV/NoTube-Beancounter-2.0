package tv.notube.platform.alive;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.testng.Assert;
import org.testng.annotations.Test;
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
        int response = client.executeMethod(getMethod);
        Assert.assertEquals(response, 200);
        String responseBody = new String(getMethod.getResponseBody());
        Assert.assertNotNull(responseBody);
        logger.info("response: " + responseBody);
    }

}