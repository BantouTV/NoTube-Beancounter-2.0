package tv.notube.indexer;

import org.joda.time.DateTime;
import tv.notube.activities.ActivityStore;
import tv.notube.activities.ActivityStoreException;
import tv.notube.commons.model.activity.Activity;

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
        throw new UnsupportedOperationException("NIY");
    }

    @Override
    public Map<UUID, Collection<Activity>> getByDateRange(DateTime from, DateTime to)
            throws ActivityStoreException {
        throw new UnsupportedOperationException("NIY");
    }

    public Activity getLastActivity() {
        return lastActivity;
    }

}