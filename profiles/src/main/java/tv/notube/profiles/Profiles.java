package tv.notube.profiles;

import tv.notube.commons.model.UserProfile;

import java.util.UUID;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public interface Profiles {

    public void store(UserProfile up) throws ProfilesException;

    public UserProfile lookup(UUID userId) throws ProfilesException;
}
