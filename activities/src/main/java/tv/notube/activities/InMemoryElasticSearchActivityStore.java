package tv.notube.activities;

import org.joda.time.DateTime;
import tv.notube.commons.model.activity.Activity;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class InMemoryElasticSearchActivityStore implements ActivityStore {
    @Override
    public void store(UUID userId, Activity activity) throws ActivityStoreException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void store(UUID userId, Collection<Activity> activities) throws ActivityStoreException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Collection<Activity> getByUser(UUID uuidId, int max) throws ActivityStoreException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Collection<Activity> getByUserAndDateRange(UUID uuid, DateTime from, DateTime to) throws ActivityStoreException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Map<UUID, Collection<Activity>> getByDateRange(DateTime from, DateTime to) throws ActivityStoreException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
