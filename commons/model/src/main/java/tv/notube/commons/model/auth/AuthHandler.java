package tv.notube.commons.model.auth;

import tv.notube.commons.model.OAuthToken;
import tv.notube.commons.model.User;

import java.net.URL;

/**
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public interface AuthHandler {

    public User auth(User user, String token) throws AuthHandlerException;

    /**
     * To handle <i>OAuth</i> callbacks.
     *
     * @param user
     * @param token
     * @param verifier
     * @return
     * @throws AuthHandlerException
     */
    public AuthenticatedUser auth(
            User user,
            String token,
            String verifier
    ) throws AuthHandlerException;

    /**
     *
     * @param username
     * @return
     * @throws AuthHandlerException
     */
    public OAuthToken getToken(String username) throws AuthHandlerException;

    /**
     *
     * @param username
     * @param callback a custom callback, which overrides the {@link tv
     * .notube.commons.model.Service} one.
     * @return
     * @throws AuthHandlerException
     */
    public OAuthToken getToken(String username, URL callback) throws AuthHandlerException;

    /**
     * Returns the service url which this auth serves.
     *
     * @return
     */
    public String getService();

}
