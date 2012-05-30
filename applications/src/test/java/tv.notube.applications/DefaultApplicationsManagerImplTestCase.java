package tv.notube.applications;

import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import tv.notube.commons.storage.kvs.KVStore;
import tv.notube.commons.storage.kvs.mybatis.MyBatisKVStore;
import tv.notube.commons.storage.model.fields.serialization.SerializationManager;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;
import java.util.UUID;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class DefaultApplicationsManagerImplTestCase {

    private ApplicationsManager applicationsManager;

    private final static String applicationName = "n-screen";

    @BeforeTest(enabled = false)
    public void setUp() throws MalformedURLException, ApplicationsManagerException {
        Properties properties = new Properties();
        properties.setProperty("url", "jdbc:mysql://localhost:3306/kvs");
        properties.setProperty("username", "kvs");
        properties.setProperty("password", "kvs");
        KVStore kVStore = new MyBatisKVStore(
                properties,
                new SerializationManager()
        );
        applicationsManager = new DefaultApplicationsManagerImpl(kVStore);
    }

    @AfterTest(enabled = false)
    public void tearDown() throws ApplicationsManagerException {
        applicationsManager.deregisterApplication(applicationName);
    }

    @Test(enabled = false)
    public void testCRUD() throws MalformedURLException,
            ApplicationsManagerException {
        Application application = getApplication(applicationName);
        String apiKey = applicationsManager.registerApplication(application);

        Application actual = applicationsManager.getApplication(applicationName);
        Assert.assertNotNull(actual);
        Assert.assertEquals(actual, application);

        actual = applicationsManager.getApplicationByApiKey(apiKey);
        Assert.assertEquals(actual, application);

        applicationsManager.deregisterApplication(applicationName);
        actual = applicationsManager.getApplication(applicationName);
        Assert.assertNull(actual);
    }

    @Test(enabled = false)
    public void testPermissions() throws ApplicationsManagerException,
            MalformedURLException {
        UUID resourceId = UUID.randomUUID();

        Application application = getApplication(applicationName);
        String apiKey = applicationsManager.registerApplication(application);

        Assert.assertTrue(applicationsManager.isAuthorized(apiKey));

        Permission permission = new Permission(resourceId);
        permission.setPermission(Permission.Action.RETRIEVE, true);
        applicationsManager.grantPermission(applicationName, permission);

        Assert.assertFalse(applicationsManager.isAuthorized(
                apiKey,
                resourceId,
                Permission.Action.DELETE
        ));

        applicationsManager.grantPermission(
                applicationName,
                resourceId,
                Permission.Action.DELETE
        );

        Assert.assertTrue(applicationsManager.isAuthorized(
                apiKey,
                resourceId,
                Permission.Action.DELETE
        ));

        applicationsManager.deregisterApplication(applicationName);
        Application actual = applicationsManager.getApplication(applicationName);
        Assert.assertNull(actual);
    }

    private Application getApplication(String applicationName) throws MalformedURLException {
         Application application = new Application(
                applicationName,
                "BBC n-screen application",
                "libby@bbc.co.uk"
        );
        application.setOAuthCallback(new URL("http://example.com/callback"));
        application.setWebsite(new URL("http://bbc.co.uk"));
        return application;
    }


}
