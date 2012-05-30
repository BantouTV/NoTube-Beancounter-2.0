package tv.noube.crawler;

import tv.notube.commons.model.Service;
import tv.notube.commons.model.User;
import tv.notube.commons.model.auth.AuthHandler;
import tv.notube.commons.model.auth.OAuthAuth;
import tv.notube.commons.model.auth.SimpleAuth;
import tv.notube.usermanager.services.auth.ServiceAuthorizationManager;
import tv.notube.usermanager.services.auth.ServiceAuthorizationManagerException;

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
    public Service getService(String serviceName) throws ServiceAuthorizationManagerException {
        HashMap<String, Service> nameToService = new HashMap<String, Service>();
        Service s1 = new Service("fake-service-1");
        Service s2 = new Service("fake-service-2");
        nameToService.put(s1.getName(), s1);
        nameToService.put(s2.getName(), s2);
        return nameToService.get(serviceName);
    }
}