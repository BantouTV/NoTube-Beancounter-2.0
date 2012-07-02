package tv.notube.usermanager.services.auth;

import tv.notube.commons.model.auth.AuthHandler;
import tv.notube.commons.model.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public abstract class AbstractServiceAuthorizationManager
        implements ServiceAuthorizationManager {

    protected Map<Service, AuthHandler> handlers
            = new HashMap<Service, AuthHandler>();

    public void addHandler(Service service, AuthHandler handler)
            throws ServiceAuthorizationManagerException {
        handlers.put(service, handler);
    }

    public AuthHandler getHandler(String service)
            throws ServiceAuthorizationManagerException {
        for(Service s : handlers.keySet()) {
            if(s.getName().equals(service)) {
                return handlers.get(s);
            }
        }
        throw new ServiceAuthorizationManagerException("service '" + service + "' not found");
    }

    public List<Service> getServices() throws ServiceAuthorizationManagerException {
        return new ArrayList<Service>(handlers.keySet());
    }

    public Service getService(String serviceName)
            throws ServiceAuthorizationManagerException {
        for(Service service : handlers.keySet()) {
            if(service.getName().equals(serviceName)) {
                return service;
            }
        }
        return null;
    }
}
