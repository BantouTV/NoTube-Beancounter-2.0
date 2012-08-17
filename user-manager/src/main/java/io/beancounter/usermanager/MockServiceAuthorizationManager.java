package io.beancounter.usermanager;

import io.beancounter.commons.model.Service;
import io.beancounter.commons.model.User;
import io.beancounter.commons.model.auth.AuthHandler;
import io.beancounter.usermanager.services.auth.ServiceAuthorizationManager;
import io.beancounter.usermanager.services.auth.ServiceAuthorizationManagerException;

import java.util.HashMap;
import java.util.List;

/**
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
public class MockServiceAuthorizationManager implements ServiceAuthorizationManager {

    @Override
    public User register(User user, String service, String token) throws ServiceAuthorizationManagerException {
        throw new UnsupportedOperationException("NIY");
    }

    @Override
    public void addHandler(Service service, AuthHandler handler) throws ServiceAuthorizationManagerException {
        throw new UnsupportedOperationException("NIY");
    }

    @Override
    public AuthHandler getHandler(String service) throws ServiceAuthorizationManagerException {
        throw new UnsupportedOperationException("NIY");
    }

    @Override
    public List<Service> getServices() throws ServiceAuthorizationManagerException {
        throw new UnsupportedOperationException("NIY");
    }

    @Override
    public synchronized Service getService(String serviceName) throws ServiceAuthorizationManagerException {
        HashMap<String, Service> services = new HashMap<String, Service>();
        Service s1 = new Service("fake-oauth-service");
        Service s2 = new Service("fake-simple-service");
        services.put(s1.getName(), s1);
        services.put(s2.getName(), s2);
        return services.get(serviceName);
    }
}