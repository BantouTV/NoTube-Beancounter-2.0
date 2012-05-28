package tv.notube.platform.user;

import org.joda.time.DateTime;
import tv.notube.commons.model.OAuthToken;
import tv.notube.commons.model.User;
import tv.notube.commons.model.activity.*;
import tv.notube.commons.model.activity.bbc.BBCProgramme;
import tv.notube.commons.model.activity.bbc.GenreBuilder;
import tv.notube.usermanager.UserManager;
import tv.notube.usermanager.UserManagerException;
import tv.notube.usermanager.services.auth.ServiceAuthorizationManager;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class MockUserManager implements UserManager {

    @Override
    public void storeUser(User user) throws UserManagerException { }

    @Override
    public User getUser(UUID userId) throws UserManagerException {
        throw new UnsupportedOperationException("NIY");
    }

    @Override
    public User getUser(String username) throws UserManagerException {
        return (username.equals("NotExistingUser") ? null : new User());
    }

    @Override
    public void storeUserActivities(UUID userId, List<Activity> activities) throws UserManagerException {
        throw new UnsupportedOperationException("NIY");
    }

    @Override
    public List<Activity> getUserActivities(UUID userId) throws UserManagerException {
        return getActivities();
    }

    @Override
    public List<Activity> getUserActivities(String username) throws UserManagerException {
        throw new UnsupportedOperationException("NIY");
    }

    @Override
    public void deleteUser(UUID userId) throws UserManagerException {
        throw new UnsupportedOperationException("NIY");
    }

    @Override
    public List<UUID> getUsersToBeProfiled() throws UserManagerException {
        throw new UnsupportedOperationException("NIY");
    }

    @Override
    public List<UUID> getUsersToCrawled() throws UserManagerException {
        throw new UnsupportedOperationException("NIY");
    }

    @Override
    public OAuthToken getOAuthToken(String service, String username) throws UserManagerException {
        throw new UnsupportedOperationException("NIY");
    }

    @Override
    public void registerService(String service, User user, String token) throws UserManagerException {
        throw new UnsupportedOperationException("NIY");
    }

    @Override
    public void registerOAuthService(String service, User user, String token, String verifier) throws UserManagerException {
        throw new UnsupportedOperationException("NIY");
    }

    @Override
    public ServiceAuthorizationManager getServiceAuthorizationManager() throws UserManagerException {
        throw new UnsupportedOperationException("NIY");
    }

    @Override
    public void deregisterService(String service, User userObj) throws UserManagerException {
        throw new UnsupportedOperationException("NIY");
    }

    @Override
    public void setUserFinalRedirect(String username, URL url) throws UserManagerException {
        throw new UnsupportedOperationException("NIY");
    }

    @Override
    public URL consumeUserFinalRedirect(String username) throws UserManagerException {
        throw new UnsupportedOperationException("NIY");
    }

    private List<Activity> getActivities() {
        List<Activity> activities = new ArrayList<Activity>();
        Song s = new Song();
        s.setMbid("3278462378462");
        Activity a1 = new Activity(
                Verb.LIKE,
                s,
                new Context(DateTime.now())
        );
        BBCProgramme p = new BBCProgramme();
        try {
            p.addGenre(GenreBuilder.getInstance().lookup(new URL("http://www.bbc.co.uk/programmes/genres/sport#genre")));
        } catch (MalformedURLException e) {
            // never happens
        }
        p.addActor("Pippo Franco");
        p.addActor("Marisa Laurito");
        p.setMediumSynopsis("just a fake BBC programme");
        Activity a2 = new Activity(
                Verb.LISTEN,
                p,
                new Context(DateTime.now())
        );
        Tweet t = new Tweet();
        t.setText("just #fake tweet check it http://t.com/3423");
        t.setName("fake tweet name");
        try {
            t.setUrl(new URL("http://twitter.com/2347223423/32"));
        } catch (MalformedURLException e) {
            // never happens
        }
        t.addHashTag("fake");
        try {
            t.addUrl(new URL("http://t.com/3423"));
        } catch (MalformedURLException e) {
            // never happens
        }
        Activity a3= new Activity(
                Verb.LISTEN,
                t,
                new Context(DateTime.now()));
        activities.add(a1);
        activities.add(a2);
        activities.add(a3);
        return activities;
    }
}