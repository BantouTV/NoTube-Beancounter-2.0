package tv.notube.profiles;

import com.google.inject.Guice;
import com.google.inject.Injector;
import junit.framework.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import tv.notube.commons.model.UserProfile;

import java.util.UUID;

/**
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
public class JedisProfilesIntegrationTest {

    private Profiles profiles;

    @BeforeClass
    public void setUp() {
        Injector injector = Guice.createInjector(new ProfilesModule());
        profiles = injector.getInstance(Profiles.class);
    }

    @Test
    public void testStoreUserProfile() throws ProfilesException {
        UserProfile testProfile = getTestUserProfile();
        profiles.store(testProfile);
        UserProfile profileRetrieved = profiles.lookup(testProfile.getUserId());
        Assert.assertEquals(testProfile, profileRetrieved);
    }

    private UserProfile getTestUserProfile() {
        UUID id = UUID.fromString("22345678-1234-1234-1234-123456789ab");
        UserProfile profile = new UserProfile(id);
        profile.setUserId(id);
        profile.setUsername("TEST-USERNAME");
        return profile;
    }
}