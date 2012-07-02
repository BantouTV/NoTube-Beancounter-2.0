package tv.notube.usermanager;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import tv.notube.commons.model.User;
import tv.notube.commons.tests.Tests;
import tv.notube.commons.tests.TestsBuilder;
import tv.notube.commons.tests.TestsException;

/**
 * Reference test case for {@link JedisUserManagerImpl}.
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class JedisUserManagerIntegrationTest {

    private Tests tests;

    private UserManager userManager;

    @BeforeClass
    public void setUp() {
        Injector injector = Guice.createInjector(new UserManagerModule());
        userManager = injector.getInstance(UserManager.class);
        tests = TestsBuilder.getInstance().build();
    }

    @Test
    public void testStoreAndRetrieve() throws TestsException, UserManagerException {
        User expected = getUser();
        userManager.storeUser(expected);

        User actual = userManager.getUser(expected.getUsername());
        Assert.assertNotNull(actual);
        Assert.assertEquals(actual, expected);

        userManager.deleteUser(actual.getUsername());
        actual = userManager.getUser(expected.getUsername());
        Assert.assertNull(actual);
    }

    private User getUser() throws TestsException {
        return tests.build(User.class).getObject();
    }
}