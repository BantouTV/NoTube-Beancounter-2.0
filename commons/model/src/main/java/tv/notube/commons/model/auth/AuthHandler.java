package tv.notube.commons.model.auth;

import tv.notube.commons.model.OAuthToken;
import tv.notube.commons.model.User;
import tv.notube.commons.model.activity.Activity;

import java.net.URL;
import java.util.List;

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
     * @param username
     * @return
     * @throws AuthHandlerException
     */
    public OAuthToken getToken(String username) throws AuthHandlerException;

    /**
     * @return
     * @throws AuthHandlerException
     */
    public OAuthToken getToken() throws AuthHandlerException;

    /**
     * Totally equivalent to OAuthToken getToken() but it allows a final redirect url.
     *
     * @param finalRedirectUrl
     * @return
     * @throws AuthHandlerException
     */
    public OAuthToken getToken(URL finalRedirectUrl) throws AuthHandlerException;

    /**
     * @param username
     * @param callback a custom callback, which overrides the {@link tv
     *                 .notube.commons.model.Service} one.
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

    /**
     * To handle anonymous <i>OAuth</i> callbacks.
     *
     * @param verifier
     * @return
     */
    public AuthenticatedUser auth(String verifier) throws AuthHandlerException;


    /**
     * To grab an initial bunch of user activities.
     *
     * @param secret
     * @param identifier
     * @param limit
     * @return
     * @throws AuthHandlerException
     */
    public List<Activity> grabActivities(String secret, String identifier, int limit)
            throws AuthHandlerException;

    /**
     * Totally equivalent to auth(String verifier) but it allows custom redirect url.
     *
     * @param verifier
     * @param finalRedirect
     * @return
     * @throws AuthHandlerException
     */
    public AuthenticatedUser auth(String verifier, String finalRedirect)
            throws AuthHandlerException;
}
