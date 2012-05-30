package tv.notube.profiler.data.datasources;

import org.joda.time.DateTime;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import tv.notube.commons.model.User;
import tv.notube.commons.model.UserActivities;
import tv.notube.profiler.data.DataSource;
import tv.notube.profiler.data.DataSourceException;
import tv.notube.profiler.data.RawDataSet;
import tv.notube.usermanager.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

/**
 * Reference test case for {@link UserDataSource}.
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class UserDataSourceTestCase {

    private DataSource dataSource;

    private UserManager userManager;

    private UUID userId;

    @BeforeTest(enabled = false)
    public void setUp() throws DataSourceException, UserManagerFactoryException, URISyntaxException, UserManagerException {
        userManager = DefaultUserManagerFactory.getInstance().build();
        userId = initUserManager(userManager);
        dataSource = new UserDataSource(userManager);
        dataSource.init();
    }

    private UUID initUserManager(UserManager userManager) throws URISyntaxException, UserManagerException {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        user.setName("Davide");
        user.setSurname("Palmisano");
        user.setUsername("dpalmisano");
        user.setForcedProfiling(false);
        user.setPassword("nah");
        user.setProfiledAt(new DateTime());
        user.setReference(new URI("http://davidepalmisano.com/me.rdf"));
        userManager.storeUser(user);
        return userId;
    }

    @AfterTest(enabled = false)
    public void tearDown() throws DataSourceException {
        dataSource.dispose();
        dataSource = null;
    }

    @Test(enabled = false)
    public void testGetRawData() throws DataSourceException, UserManagerException {
        RawDataSet<UserActivities> rawData = dataSource.getRawData();
        Assert.assertNotNull(rawData);
        Assert.assertEquals(rawData.size(), 1);
        UserActivities userActivities = rawData.getNext();
        Assert.assertNotNull(userActivities);

        userManager.deleteUser(userId);
        User shouldBeNull = userManager.getUser(userId);
        Assert.assertNull(shouldBeNull);
    }

}
