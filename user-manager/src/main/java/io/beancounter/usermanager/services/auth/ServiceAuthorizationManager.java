package io.beancounter.usermanager.services.auth;

import io.beancounter.commons.model.auth.AuthHandler;
import io.beancounter.commons.model.Service;
import io.beancounter.commons.model.User;

import java.util.List;

/**
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public interface ServiceAuthorizationManager {

    /**
     * Return an authorized user.
     *
     * @param user
     * @param service
     * @param token
     * @return
     * @throws ServiceAuthorizationManagerException
     *
     */
    public User register(User user, String service, String token)
            throws ServiceAuthorizationManagerException;

    /**
     *
     * @param service
     * @param handler
     * @throws ServiceAuthorizationManagerException
     */
    public void addHandler(Service service, AuthHandler handler)
            throws ServiceAuthorizationManagerException;

    /**
     *
     * @param service
     * @return
     * @throws ServiceAuthorizationManagerException
     */
    public AuthHandler getHandler(String service)
            throws ServiceAuthorizationManagerException;

    /**
     *
     * @return
     * @throws ServiceAuthorizationManagerException
     */
    public List<Service> getServices()
            throws ServiceAuthorizationManagerException;

    /**
     *
     * @param serviceName
     * @return
     * @throws ServiceAuthorizationManagerException
     */
    public Service getService(String serviceName)
            throws ServiceAuthorizationManagerException;


}
