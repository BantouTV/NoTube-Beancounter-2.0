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
        Application application = applicationsManager.registerApplication(
                name,
                description,
                email,
                oAuth
        );
        Application actualConsumer = applicationsManager.getApplicationByApiKey(application.getConsumerKey());
        Assert.assertNotNull(actualConsumer);
        Assert.assertNotNull(actualConsumer.getConsumerKey());
        Assert.assertNotNull(actualConsumer.getAdminKey());
        Assert.assertEquals(actualConsumer.getName(), name);
        Assert.assertEquals(actualConsumer.getDescription(), description);
        Assert.assertEquals(actualConsumer.getCallback(), oAuth);

        Application actualAdmin = applicationsManager.getApplicationByApiKey(application.getAdminKey());
        Assert.assertEquals(actualConsumer, actualAdmin);

        Assert.assertTrue(applicationsManager.isAuthorized(
                actualConsumer.getConsumerKey(),
                ApplicationsManager.Action.CREATE,
                ApplicationsManager.Object.USER)
        );

        applicationsManager.deregisterApplication(actualConsumer.getConsumerKey());

        actualConsumer = applicationsManager.getApplicationByApiKey(actualConsumer.getConsumerKey());
        Assert.assertNull(actualConsumer);
    }
}