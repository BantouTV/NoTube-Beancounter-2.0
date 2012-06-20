package tv.notube.profiles;

import tv.notube.commons.model.UserProfile;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public final class InMemoryProfilesImpl implements Profiles {

    private Set<UserProfile> profiles = new HashSet<UserProfile>();

    public synchronized void store(UserProfile up) throws ProfilesException {
        profiles.add(up);
    }

    public UserProfile lookup(UUID userId) throws ProfilesException {
        for(UserProfile up : profiles) {
            if(up.getId().equals(userId)) {
                return up;
            }
        }
        return null;
    }
}
