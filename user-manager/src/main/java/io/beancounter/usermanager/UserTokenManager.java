package io.beancounter.usermanager;

import java.util.UUID;

public interface UserTokenManager {

    /**
     * Check if the given token is valid (ie. it has not expired).
     *
     * @param token The token to check.
     * @return true if the token exists; false otherwise.
     * @throws UserManagerException If the token is null or an error occurs
     *         while interacting with Redis.
     */
    boolean checkTokenExists(UUID token) throws UserManagerException;

    /**
     * Creates a new unique user token for the given user and stores it in
     * the user tokens Redis database.
     *
     * @param username The username of the user to generate a token for.
     * @return The new user token.
     * @throws UserManagerException If the username is null or an error occurs
     *         while interacting with Redis.
     */
    UUID createUserToken(String username) throws UserManagerException;

    /**
     * Delete the specified user token (if it exists).
     *
     * @param token The user token to delete.
     * @return true if the token exists and was deleted; false otherwise.
     * @throws UserManagerException If the token is null or an error occurs
     *         while interacting with Redis.
     */
    boolean deleteUserToken(UUID token) throws UserManagerException;
}
