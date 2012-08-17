package io.beancounter.listener.facebook;

import org.codehaus.jackson.map.ObjectMapper;
import org.testng.Assert;
import org.testng.annotations.Test;
import io.beancounter.listener.facebook.core.model.FacebookNotification;

import java.io.IOException;

/**
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
public class FacebookNotificationParsingTest {

    @Test
    public void testParsing() throws IOException {
        String json = "{\"object\":\"user\",\"entry\":[{\"uid\":1335845740,\"changed_fields\":[\"name\",\"picture\"],\"time\":232323},{\"uid\":1234,\"changed_fields\":[\"friends\"],\"time\":232325}]}";
        ObjectMapper mapper = new ObjectMapper();
        FacebookNotification notification = mapper.readValue(json, FacebookNotification.class);
        Assert.assertNotNull(notification);
    }

}