package tv.notube.platform.applications;

import tv.notube.applications.Application;
import tv.notube.applications.ApplicationsManager;
import tv.notube.applications.ApplicationsManagerException;
import tv.notube.applications.Permission;

import java.util.UUID;

/**
 *
 * @author Enrico Candino (enrico.candino@gmail.com)
 */
public class MockApplicationsManager implements ApplicationsManager {
    @Override
    public String registerApplication(Application application) throws ApplicationsManagerException {
        throw new UnsupportedOperationException("NIY");
    }

    @Override
    public Application getApplication(String name) throws ApplicationsManagerException {
        throw new UnsupportedOperationException("NIY");
    }

    @Override
    public Application getApplicationByApiKey(String name) throws ApplicationsManagerException {
        throw new UnsupportedOperationException("NIY");
    }

    @Override
    public void grantPermission(String name, Permission permission) throws ApplicationsManagerException {
        throw new UnsupportedOperationException("NIY");
    }

    @Override
    public void grantPermission(String name, UUID resource, Permission.Action action) throws ApplicationsManagerException {
        throw new UnsupportedOperationException("NIY");
    }

    @Override
    public void deregisterApplication(String name) throws ApplicationsManagerException {
        throw new UnsupportedOperationException("NIY");
    }

    @Override
    public boolean isAuthorized(String apiKey, UUID resource, Permission.Action action) throws ApplicationsManagerException {
        throw new UnsupportedOperationException("NIY");
    }

    @Override
    public boolean isAuthorized(String apiKey) throws ApplicationsManagerException {
        return (apiKey.compareTo("APIKEY")==0 ? true : false);
    }
}
