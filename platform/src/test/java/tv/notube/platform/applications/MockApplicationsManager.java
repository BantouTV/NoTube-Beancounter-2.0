package tv.notube.platform.applications;

import tv.notube.applications.Application;
import tv.notube.applications.ApplicationsManager;
import tv.notube.applications.ApplicationsManagerException;
import tv.notube.applications.Permission;

import java.util.UUID;

/**
 * Mockup class for {@link ApplicationsManager}.
 *
 * @author Enrico Candino (enrico.candino@gmail.com)
 */
public class MockApplicationsManager implements ApplicationsManager {

    private static final String APIKEY = "APIKEY";

    @Override
    public String registerApplication(Application application)
            throws ApplicationsManagerException {
        return APIKEY;
    }

    @Override
    public Application getApplication(String name)
            throws ApplicationsManagerException {
        throw new UnsupportedOperationException("NIY");
    }

    @Override
    public Application getApplicationByApiKey(String name)
            throws ApplicationsManagerException {
        return new Application(
                name,
                "This is a Fake Application",
                "fake_mail@test.com"
        );
    }

    @Override
    public void grantPermission(String name, Permission permission)
            throws ApplicationsManagerException {
        throw new UnsupportedOperationException("NIY");
    }

    @Override
    public void grantPermission(String name, UUID resource, Permission.Action action)
            throws ApplicationsManagerException { }

    @Override
    public void deregisterApplication(String name) throws ApplicationsManagerException {
        if(name == null) {
            throw new ApplicationsManagerException("parameter name cannot be null");
        }
    }

    @Override
    public boolean isAuthorized(String apiKey, UUID resource, Permission.Action action)
            throws ApplicationsManagerException {
        return true;
    }

    @Override
    public boolean isAuthorized(String apiKey) throws ApplicationsManagerException {
        return apiKey.equals(APIKEY);
    }
}