package tv.notube.applications;

import tv.notube.applications.model.Application;

import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

/**
 * Mockup class for {@link ApplicationsManager}.
 *
 * @author Enrico Candino (enrico.candino@gmail.com)
 * @author Davide Palmisano (dpalmisano@gmail.com)
 */
public class MockApplicationsManager implements ApplicationsManager {

    private Set<Application> applications = new HashSet<Application>();

    @Override
    public UUID registerApplication(
            String name,
            String description,
            String email,
            URL callback
    ) throws ApplicationsManagerException {
        Application application = Application.build(
                name,
                description,
                email,
                callback
        );
        applications.add(application);
        return application.getApiKey();
    }

    @Override
    public void deregisterApplication(UUID key) throws ApplicationsManagerException {
        Iterator it = applications.iterator();
        while(it.hasNext()) {
            Application app = (Application) it.next();
            if(app.getApiKey().equals(key)) {
               it.remove();
            }
        }
    }

    @Override
    public Application getApplicationByApiKey(UUID key) throws ApplicationsManagerException {
        for(Application application : applications) {
            if(application.getApiKey().equals(key)) {
                return application;
            }
        }
        throw new ApplicationsManagerException("application with key [" + key + "] not found");
    }

    @Override
    public boolean isAuthorized(UUID key, Action action, Object object) throws ApplicationsManagerException {
        Application application = getApplicationByApiKey(key);
        if(application.getApiKey().equals(key)) {
            return true;
        }
        return false;
    }
}