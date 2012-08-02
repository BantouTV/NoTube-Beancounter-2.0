package tv.notube.usermanager;

import com.restfb.json.JsonObject;
import com.restfb.json.JsonTokener;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * This test is just an utility to parse small data from <i>Facebook</i> responses.
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class RestfbJsonParseTestCase {

    @Test
    public void testParse() {
        final String picture = "{\"data\":{\"url\":\"https:\\/\\/fbcdn-profile-a.akamaihd.net\\/hprofile-ak-snc4\\/369384_1268423252_18774130_q.jpg\",\"is_silhouette\":false}}";
        JsonObject jsonObject = new JsonObject(new JsonTokener(picture));
        String actualUrl = jsonObject.getJsonObject("data").getString("url");
        Assert.assertEquals(actualUrl, "https://fbcdn-profile-a.akamaihd.net/hprofile-ak-snc4/369384_1268423252_18774130_q.jpg");
    }

}
