package tv.notube.profiles;

import junit.framework.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import tv.notube.commons.model.UserProfile;
import tv.notube.profiles.jedis.DefaultJedisPoolFactory;
import tv.notube.profiles.jedis.JedisPoolFactory;

import java.util.UUID;

/**
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
public class JedisProfilesIntegrationTest {

    private JedisPoolFactory factory;

    @BeforeClass
    public void setUp() {
        factory = DefaultJedisPoolFactory.getInstance();
    }

    @Test
    public void testStoreUserProfile() throws ProfilesException {
        UserProfile testProfile = getTestUserProfile();

        Profiles underTest = new JedisProfiles(factory);
        underTest.store(testProfile);

        UserProfile profileRetrieved = underTest.lookup(testProfile.getId());
        Assert.assertEquals(testProfile, profileRetrieved);
    }

    private UserProfile getTestUserProfile() {
        UserProfile profile = new UserProfile();
        UUID id = UUID.fromString("12345678-1234-1234-1234-123456789ab");
        profile.setId(id);
        profile.setUsername("TEST-USERNAME");
        return profile;
    }
}