package tv.notube.activities;

import org.joda.time.DateTime;
import tv.notube.commons.model.activity.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

/**
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
public class MockActivityStore implements ActivityStore {

    private Activity lastActivity;

    @Override
    public void store(UUID userId, Activity activity)
            throws ActivityStoreException {
        lastActivity = activity;
    }

    @Override
    public void store(UUID userId, Collection<Activity> activities)
            throws ActivityStoreException {}

    @Override
    public Collection<Activity> getByUser(UUID uuidId, int max)
            throws ActivityStoreException {
        throw new UnsupportedOperationException("NIY");
    }

    @Override
    public Collection<Activity> getByUserAndDateRange(UUID uuid, DateTime from, DateTime to)
            throws ActivityStoreException {
        // missing the VerbRandomiser -> cannot create Random activities
        Activity a1 = new Activity();
        a1.setVerb(Verb.TWEET);
        a1.setContext(new Context());
        a1.setObject(new Tweet());
        Activity a2 = new Activity();
        a2.setVerb(Verb.LIKE);
        a2.setContext(new Context());
        a2.setObject(new Song());
        Collection<Activity> activities = new ArrayList<Activity>();
        activities.add(a1);
        activities.add(a2);
        return activities;
    }

    @Override
    public Map<UUID, Collection<Activity>> getByDateRange(DateTime from, DateTime to)
            throws ActivityStoreException {
        throw new UnsupportedOperationException("NIY");
    }

    @Override
    public Collection<Activity> getByUser(UUID userId) throws ActivityStoreException {
        throw new UnsupportedOperationException("NIY");
    }

    @Override
    public Activity getByUser(UUID userId, UUID activityId) throws ActivityStoreException {
        throw new UnsupportedOperationException("NIY");
    }

    @Override
    public Collection<Activity> getByUser(UUID userId, Collection<UUID> activityIds) throws ActivityStoreException {
        throw new UnsupportedOperationException("NIY");
    }

    @Override
    public void shutDown() throws ActivityStoreException {}

    public Activity getLastActivity() {
        return lastActivity;
    }

}