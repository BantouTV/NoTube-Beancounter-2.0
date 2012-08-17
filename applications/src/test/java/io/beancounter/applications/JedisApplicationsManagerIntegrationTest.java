package io.beancounter.applications;

import com.google.inject.Guice;
import com.google.inject.Injector;
import junit.framework.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import redis.clients.jedis.JedisPool;
import io.beancounter.applications.model.Application;
import io.beancounter.commons.helper.jedis.JedisPoolFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

/**
 * Reference test case for {@link JedisApplicationsManagerImpl}.
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class JedisApplicationsManagerIntegrationTest {

    private ApplicationsManager applicationsManager;

    @BeforeClass
    public void setUp() {
        Injector injector = Guice.createInjector(new ApplicationsModule());
        applicationsManager = injector.getInstance(ApplicationsManager.class);
        JedisPool pool = injector.getInstance(JedisPoolFactory.class).build();
        pool.getResource().flushAll();
    }

    @Test
    public void testRegisterAndDeregisterApplication()
            throws MalformedURLException, ApplicationsManagerException {
        final String name = "test-app";
        final String description = "a test app";
        final String email = "t@test.com";
        final URL oAuth = new URL("http://fake.com/oauth");
        UUID key = applicationsManager.registerApplication(
                name,
                description,
                email,
                oAuth
        );
        Application actual = applicationsManager.getApplicationByApiKey(key);
        Assert.assertNotNull(actual);
        Assert.assertEquals(actual.getApiKey(), key);
        Assert.assertEquals(actual.getName(), name);
        Assert.assertEquals(actual.getDescription(), description);
        Assert.assertEquals(actual.getCallback(), oAuth);

        Assert.assertTrue(applicationsManager.isAuthorized(
                key,
                ApplicationsManager.Action.CREATE,
                ApplicationsManager.Object.USER)
        );

        applicationsManager.deregisterApplication(key);

        actual = applicationsManager.getApplicationByApiKey(key);
        Assert.assertNull(actual);
    }
}