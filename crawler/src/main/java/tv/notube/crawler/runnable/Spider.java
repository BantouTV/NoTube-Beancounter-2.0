package tv.notube.crawler.runnable;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import tv.notube.commons.model.Service;
import tv.notube.commons.model.User;
import tv.notube.commons.model.activity.Activity;
import tv.notube.crawler.requester.Requester;
import tv.notube.crawler.requester.RequesterException;
import tv.notube.usermanager.UserManager;
import tv.notube.usermanager.UserManagerException;
import tv.notube.usermanager.services.auth.ServiceAuthorizationManager;
import tv.notube.usermanager.services.auth.ServiceAuthorizationManagerException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class Spider implements Runnable {

    private static Logger logger = Logger.getLogger(Spider.class);

    private String name;

    private UserManager um;

    private Requester requester;

    private User user;

    public Spider(String name, UserManager um, UUID id, Requester req) throws SpiderException {
        this.name = name;
        this.um = um;
        this.requester = req;
        try {
            this.user = getUser(id);
        } catch (UserManagerException e) {
            final String errMsg = "Error while getting user with id '" + id + "'";
            logger.error(errMsg, e);
            throw new SpiderException(errMsg, e);
        }
    }

    public void run() {
        List<Activity> activities = new ArrayList<Activity>();
        ServiceAuthorizationManager sam;
        try {
            sam = um.getServiceAuthorizationManager();
        } catch (UserManagerException e) {
            final String errMsg = "Error while getting Service SimpleAuth Manager";
            logger.error(errMsg, e);
            throw new RuntimeException(errMsg, e);
        }
        for (String serviceName : user.getServices()) {
            Service service;
            try {
                service = sam.getService(serviceName);
            } catch (ServiceAuthorizationManagerException e) {
                final String errMsg = "Error while getting Service '" + serviceName + "'";
                logger.error(errMsg, e);
                throw new RuntimeException(errMsg, e);
            }
            try {
                activities.addAll(
                        requester.call(service, user.getAuth(serviceName))
                );
            } catch (RequesterException e) {
                final String errMsg = "Error while calling service '" + serviceName + "'";
                logger.error(errMsg, e);
                throw new RuntimeException(errMsg, e);
            }
            user.setProfiledAt(new DateTime());
        }
        try {
            um.storeUser(user);
        } catch (UserManagerException e) {
            final String errMsg = "Error while storing user '" + user
                    .getId() + "'";
            logger.error(errMsg, e);
            throw new RuntimeException(errMsg, e);
        }
        try {
            um.storeUserActivities(user.getId(), activities);
        } catch (UserManagerException e) {
            final String errMsg = "Error while storing activities for " +
                    "user '" + user.getId() + "'";
            logger.error(errMsg, e);
            throw new RuntimeException(errMsg, e);
        }
    }

    private User getUser(UUID id) throws UserManagerException {
        return um.getUser(id);
    }

    public String getName() {
        return name;
    }
}
