package tv.notube.indexer;

import tv.notube.activities.ActivityStore;
import tv.notube.activities.ActivityStoreException;
import tv.notube.commons.model.activity.Activity;

import java.util.UUID;

/**
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
public class ActivityServiceImpl implements ActivityService {

    private final ActivityStore activityStore;

    public ActivityServiceImpl(final ActivityStore activityStore) {
        this.activityStore = activityStore;
    }

    @Override
    public void store(UUID userId, Activity activity) throws ActivityStoreException {
        activityStore.store(userId, activity);
    }
}