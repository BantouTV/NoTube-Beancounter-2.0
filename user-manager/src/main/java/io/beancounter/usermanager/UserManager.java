package io.beancounter.usermanager;

import io.beancounter.commons.model.OAuthToken;
import io.beancounter.commons.model.User;
import io.beancounter.commons.model.activity.Activity;
import io.beancounter.commons.model.auth.AuthenticatedUser;
import io.beancounter.usermanager.services.auth.ServiceAuthorizationManager;

import java.net.URL;
import java.util.List;

/**
 * Defines main responsabilities of a class handling users in
 * the <i>beancounter.io</i> ecosystem.
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public interface UserManager {

    /**
     * Store a {@link User} on the Beancounter.
     *
     * @param
     * @throws UserManagerException
     */
    public void storeUser(User user) throws UserManagerException;

    /**
     * Retrieve a {@link User} from the Beancounter.
     *
     * @param username
     * @return
     * @throws UserManagerException
     */
    public User getUser(String username) throws UserManagerException;

    /**
     * Completely flushes out all the {@link User} data.
     *
     * @param user
     * @throws UserManagerException
     */
    public void deleteUser(User user) throws UserManagerException;

    /**
     * Get the user <a href="http://oauth.net">OAuth</> token.
     *
     *
     * @param service
     * @param username
     * @return
     */
    public OAuthToken getOAuthToken(String service, String username)
            throws UserManagerException;

    /**
     *
     * @param serviceName
     * @param username
     * @param url the custom callback. it overwrites the one in {@link io.beancounter.commons.model.Service}
     * @return
     */
    public OAuthToken getOAuthToken(String serviceName, String username, URL url)
            throws UserManagerException;


    /**
     * It handles all the <i>OAuth-like</i> protocols handshaking.
     *
     * @param service
     * @param user
     * @param token   @throws UserManagerException
     */
    public void registerService(
            String service,
            User user,
            String token
    ) throws UserManagerException;

    /**
     * It handles all the <i>OAuth</i> protocol handshaking.
     *
     * @param service
     * @param user
     * @param token   @throws UserManagerException
     */
    public AuthenticatedUser registerOAuthService(
            String service,
            User user,
            String token,
            String verifier
    ) throws UserManagerException;

    /**
     * Returns the {@link ServiceAuthorizationManager} concrete implementation
     * binding with this user manager.
     *
     * @return
     * @throws UserManagerException
     */
    public ServiceAuthorizationManager getServiceAuthorizationManager()
            throws UserManagerException;

    /**
     * Removes from the provided {@link User} a service with the name
     * provided as input.
     *
     * @param service
     * @param userObj
     */
    void deregisterService(String service, User userObj) throws UserManagerException;

    /**
     * a temporary in-memory store for the final url where a user will be
     * redirected at the very end of all the authorization exchanges.
     *
     * @param username
     * @param url
     */
    public void setUserFinalRedirect(
            String username,
            URL url
    ) throws UserManagerException;

    /**
     * Get the user temporary final url where the user will be redirected
     * at the end of all the authorization exchange process. Once the url has
     * been consumed he needs to be set again.
     *
     * @param username
     * @return
     * @throws UserManagerException
     */
    public URL consumeUserFinalRedirect(String username) throws UserManagerException;

    /**
     * This voids the current user OAuth token for the given service.
     *
     * @param user
     * @param service
     */
    public void voidOAuthToken(User user, String service) throws UserManagerException;;

    /**
     * This method asks for an {@link OAuthToken} without the needs of a
     * {@link User} username.
     *
     * @param service
     * @return
     * @throws UserManagerException
     */
    public OAuthToken getOAuthToken(String service) throws UserManagerException;

    /**
     * Totally equivalent to #getOAuthToken(String service) but it allows a final
     * redirect user.
     *
     * @param service
     * @param finalRedirectUrl
     * @return
     */
    public OAuthToken getOAuthToken(String service, URL finalRedirectUrl) throws UserManagerException;

    /**
     * This method creates a user from scratch, handles the <i>OAuth exchange</i>
     * for it give the input verifier and stores it with a random username.
     *
     * @param service
     * @param verifier
     * @return {@link AtomicSignUp} with all needed information
     */
    public AtomicSignUp storeUserFromOAuth(String service, String token, String verifier) throws UserManagerException;

    /**
     * Totally equivalent to storeUserFromOAuth(String service, String verifier) but it handles
     * the final redirect callback, for Web access.
     *
     * @param service
     * @param verifier
     * @param decodedFinalRedirect
     * @return
     * @throws UserManagerException
     */
    public AtomicSignUp storeUserFromOAuth(
            String service,
            String token,
            String verifier,
            String decodedFinalRedirect
    ) throws UserManagerException;

    /**
     * This method grabs the latest activities a user performed on a service.
     *
     * @param user
     * @param identifier
     * @param service
     * @param limit
     * @return
     */
    public List<Activity> grabUserActivities(User user, String identifier, String service, int limit)
            throws UserManagerException;

}
