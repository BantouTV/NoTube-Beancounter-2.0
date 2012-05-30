package tv.notube.usermanager;

import org.joda.time.DateTime;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import tv.notube.commons.model.auth.SimpleAuth;
import tv.notube.commons.model.Service;
import tv.notube.commons.model.User;
import tv.notube.commons.model.activity.*;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Reference test case for {@link UserManager}.
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class UserManagerTestCase {

    private UserManager userManager;

    private UUID userId;

    @BeforeTest
    public void setUp() throws UserManagerFactoryException {
        userManager = DefaultUserManagerFactory.getInstance().build();
        userId = UUID.randomUUID();
    }

    @AfterTest
    public void tearDown() throws UserManagerException {
        userManager.deleteUser(userId);
        userManager = null;
    }

    @Test(enabled = false)
    public void testUserCRUD() throws URISyntaxException, UserManagerException {
        User user = new User();
        user.setId(userId);
        user.setName("Libby");
        user.setSurname("Miller");
        user.setForcedProfiling(false);
        user.setReference(new URI("http://notube.tv/user/" + userId));
        user.setProfiledAt(new DateTime());
        user.setUsername("lmiller");

        Service service = new Service("facebook");
        user.addService(
                service.getName(),
                new SimpleAuth("fake-session","lmiller")
        );

        userManager.storeUser(user);
        User actual = userManager.getUser(userId);
        Assert.assertEquals(user, actual);

        List<UUID> users = userManager.getUsersToCrawled();
        Assert.assertTrue(users.contains(userId));
    }

    @Test(enabled = false)
    public void testActivityCRUD()
            throws URISyntaxException, UserManagerException, MalformedURLException {
        User user = new User();
        user.setId(userId);
        user.setName("Libby");
        user.setSurname("Miller");
        user.setForcedProfiling(false);
        user.setReference(new URI("http://notube.tv/user/" + userId));
        user.setProfiledAt(new DateTime());
        user.setUsername("lmiller");

        Service service = new Service("facebook");
        user.addService(
                service.getName(),
                new SimpleAuth("fake-session","lmiller")
        );
        userManager.storeUser(user);

        List<Activity> activities = new ArrayList<Activity>();
        Activity a = new Activity();
        a.setVerb(Verb.TWEET);
        Tweet t = new Tweet();
        t.setText("Hey this is a tweet never happened");
        t.setUrl(new URL("http://twitter.com/user/pigger/262653"));
        a.setObject(t);
        Context c = new Context();
        c.setDate(new DateTime());
        c.setService(new URL("http://twitter.com"));
        a.setContext(c);
        activities.add(a);
        userManager.storeUserActivities(userId, activities);

        List<Activity> actual = userManager.getUserActivities(userId);
        Assert.assertNotNull(actual);
        Assert.assertEquals(1, actual.size());
    }

    @Test(enabled = false)
    public void testGetToken() throws URISyntaxException, UserManagerException {
        User user = new User();
        user.setId(userId);
        user.setName("Davide");
        user.setSurname("Palmisano");
        user.setForcedProfiling(false);
        user.setReference(new URI("http://notube.tv/user/" + userId));
        user.setProfiledAt(new DateTime());
        user.setUsername("dpalmisano");

        userManager.storeUser(user);

        User actual = userManager.getUser(userId);
        Assert.assertEquals(user, actual);
    }

}
