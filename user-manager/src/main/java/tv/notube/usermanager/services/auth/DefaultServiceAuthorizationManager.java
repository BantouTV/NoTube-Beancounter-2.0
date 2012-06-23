package tv.notube.usermanager.services.auth;

import tv.notube.commons.model.auth.AuthHandler;
import tv.notube.commons.model.auth.AuthHandlerException;
import tv.notube.commons.model.User;

/**
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class DefaultServiceAuthorizationManager
        extends AbstractServiceAuthorizationManager {

    public User register(User user, String service, String token)
            throws ServiceAuthorizationManagerException {
        AuthHandler ah = getHandler(service);
        try {
            return ah.auth(user, token);
        } catch (AuthHandlerException e) {
            final String errMsg = "Error while authorizing user [" + user.getId() + "] to service [" + service + "]";
            throw new ServiceAuthorizationManagerException(errMsg, e);
        }
    }

}
