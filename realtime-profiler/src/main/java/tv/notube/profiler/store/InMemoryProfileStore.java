package tv.notube.profiler.store;

import tv.notube.commons.model.UserProfile;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class InMemoryProfileStore implements ProfileStore {

    private Set<UserProfile> profiles = new HashSet<UserProfile>();

    public void store(UserProfile up) throws ProfileStoreException {
        profiles.add(up);
    }

    public UserProfile lookup(UUID userId) throws ProfileStoreException {
        for(UserProfile up : profiles) {
            if(up.getId().equals(userId)) {
                return up;
            }
        }
        return null;
    }
}
