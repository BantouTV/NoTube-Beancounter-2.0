package tv.notube.profiler.store;

import tv.notube.commons.model.UserProfile;

import java.util.UUID;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
// TODO: move this class somewhere else
public interface ProfileStore {

    public void store(UserProfile up) throws ProfileStoreException;

    public UserProfile lookup(UUID userId) throws ProfileStoreException;
}
