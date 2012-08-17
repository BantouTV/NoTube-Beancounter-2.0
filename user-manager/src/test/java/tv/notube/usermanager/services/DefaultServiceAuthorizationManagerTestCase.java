package tv.notube.usermanager.services;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import tv.notube.commons.helper.PropertiesHelper;
import tv.notube.commons.model.Service;
import tv.notube.usermanager.services.auth.DefaultServiceAuthorizationManager;

import java.util.Properties;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

public class DefaultServiceAuthorizationManagerTestCase {

    private Properties properties;

    @BeforeMethod
    public void setUp() throws Exception {
        properties = PropertiesHelper.readFromClasspath("/beancounter.properties");
    }

    @Test
    public void validTwitterServiceCanBeBuiltFromProperties() throws Exception {
        Service twitterService = DefaultServiceAuthorizationManager.buildService("twitter", properties);

        assertEquals(twitterService.getName(), "twitter");
        assertEquals(twitterService.getDescription(), properties.getProperty("service.twitter.description"));
        assertEquals(twitterService.getEndpoint().toString(), properties.getProperty("service.twitter.endpoint"));
        assertEquals(twitterService.getSessionEndpoint().toString(), properties.getProperty("service.twitter.session"));
        assertEquals(twitterService.getApikey(), properties.getProperty("service.twitter.apikey"));
        assertEquals(twitterService.getSecret(), properties.getProperty("service.twitter.secret"));
        assertEquals(twitterService.getOAuthCallback().toString(), properties.getProperty("service.twitter.oauthcallback"));
        assertEquals(twitterService.getAtomicOAuthCallback().toString(), properties.getProperty("service.twitter.atomicoauth"));
    }

    @Test
    public void validFacebookServiceCanBeBuiltFromProperties() throws Exception {
        Service facebookService = DefaultServiceAuthorizationManager.buildService("facebook", properties);

        assertEquals(facebookService.getName(), "facebook");
        assertEquals(facebookService.getDescription(), properties.getProperty("service.facebook.description"));
        assertEquals(facebookService.getEndpoint().toString(), properties.getProperty("service.facebook.endpoint"));
        assertNull(facebookService.getSessionEndpoint());
        assertEquals(facebookService.getApikey(), properties.getProperty("service.facebook.apikey"));
        assertEquals(facebookService.getSecret(), properties.getProperty("service.facebook.secret"));
        assertEquals(facebookService.getOAuthCallback().toString(), properties.getProperty("service.facebook.oauthcallback"));
        assertEquals(facebookService.getAtomicOAuthCallback().toString(), properties.getProperty("service.facebook.atomicoauth"));
    }
}
