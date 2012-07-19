package tv.notube.profiles;

import tv.notube.commons.model.Interest;
import tv.notube.commons.model.UserProfile;
import tv.notube.commons.tests.Tests;
import tv.notube.commons.tests.TestsBuilder;
import tv.notube.commons.tests.TestsException;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public final class MockProfiles implements Profiles {

    private Set<UserProfile> profiles = new HashSet<UserProfile>();

    private Tests tests = TestsBuilder.getInstance().build();

    public synchronized void store(UserProfile up) throws ProfilesException {
        profiles.remove(up);
        profiles.add(up);
    }

    public UserProfile lookup(UUID userId) throws ProfilesException {
        for(UserProfile up : profiles) {
            if(up.getUserId().equals(userId)) {
                return up;
            }
        }
        return null;
        // TODO (mid) fix this mock to use it also for the api test (getProfile)
        // this was used to test the api to get the profile
        /*
        UserProfile profile;
        Set<Interest> interests = getRandomInterests();
        try {
            profile = tests.build(UserProfile.class).getObject();
        } catch (TestsException e) {
            throw new ProfilesException("Error while creating random profile", e);
        }
        profile.setInterests(interests);
        return profile;
        */
    }

    private Set<Interest> getRandomInterests() throws ProfilesException {
        Set<Interest> interests = new HashSet<Interest>();
        for(int i = 0; i<10; i++) {
            try {
                Interest interest = new Interest(new URI("http://random.test"));
                Collection<UUID> uuids = new ArrayList<UUID>();
                for(int x = 0; x<5; x++) {
                    uuids.add(UUID.randomUUID());
                }
                interest.setActivities(uuids);
                interests.add(interest);
            } catch (URISyntaxException e) {
                throw new ProfilesException("Error while creating random interest", e);
            }
        }
        return interests;
    }
}
