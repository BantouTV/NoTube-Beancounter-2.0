package io.beancounter.platform.user;

import io.beancounter.usermanager.UserManagerException;
import io.beancounter.usermanager.UserTokenManager;

import java.util.UUID;

public class MockUserTokenManager implements UserTokenManager {

    @Override
    public boolean checkTokenExists(UUID token) throws UserManagerException {
        return false;
    }

    @Override
    public UUID createUserToken(String username) throws UserManagerException {
        return UUID.randomUUID();
    }

    @Override
    public boolean deleteUserToken(UUID token) throws UserManagerException {
        return true;
    }
}
