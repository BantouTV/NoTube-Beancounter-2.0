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
    public void store(Activity activity) throws ActivityStoreException {
        activityStore.store(getUserUUID(), activity);
    }

    private UUID getUserUUID() {
        // TODO (hardcoded)
        return UUID.fromString("12345678-1234-1234-1234-123456789ab");
    }
}