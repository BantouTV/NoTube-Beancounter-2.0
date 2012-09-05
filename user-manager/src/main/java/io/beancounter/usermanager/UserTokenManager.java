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
     * Set the number of the database in Redis that is used for storing user
     * tokens.
     *
     * @param database The Redis database number.
     */
    void setDatabase(int database);

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
}
