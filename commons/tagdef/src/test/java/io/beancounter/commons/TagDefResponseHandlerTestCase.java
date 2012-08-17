package io.beancounter.commons;

import junit.framework.Assert;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.testng.annotations.Test;
import io.beancounter.commons.tagdef.TagDefResponse;
import io.beancounter.commons.tagdef.handler.TagDefResponseHandler;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
public class TagDefResponseHandlerTestCase {

    @Test
    public void testHandleResponse() throws IOException, URISyntaxException {

        final String url = "http://api.tagdef.com/ff.json";
        HttpClient client = new DefaultHttpClient();
        HttpGet get = new HttpGet(url);
        HttpResponse response = client.execute(get);

        TagDefResponseHandler handler = new TagDefResponseHandler();
        TagDefResponse tagDefResponse = handler.handleResponse(response);

        Assert.assertNotNull(tagDefResponse);
        Assert.assertTrue(tagDefResponse.getDefs().size() > 0);
        Assert.assertNotNull(tagDefResponse.getDefs().get(0).getText());
        Assert.assertNotNull(tagDefResponse.getDefs().get(0).getUrl());
        Assert.assertEquals(tagDefResponse.getStatus(), TagDefResponse.Status.OK);
    }

}