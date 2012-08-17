package io.beancounter.profiles;

import io.beancounter.commons.model.UserProfile;

import java.util.UUID;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public interface Profiles {

    /**
     * it always update regardless what's already there
     *
     * @param up
     * @throws ProfilesException
     */
    public void store(UserProfile up) throws ProfilesException;

    public UserProfile lookup(UUID userId) throws ProfilesException;
}
