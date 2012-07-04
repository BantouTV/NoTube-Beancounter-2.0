package tv.notube.indexer;

import junit.framework.Assert;
import org.testng.annotations.Test;
import tv.notube.activities.ActivityStoreException;
import tv.notube.activities.MockActivityStore;
import tv.notube.commons.model.activity.Activity;
import tv.notube.commons.tests.TestsException;

import java.util.UUID;

/**
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
public class ActivityServiceTestCase {

    @Test
    public void storeSingleActivity() throws TestsException, ActivityStoreException {
        MockActivityStore activityStore = new MockActivityStore();
        ActivityService underTest = new ActivityServiceImpl(activityStore);
        Activity activity = new Activity();

        UUID testUserId = UUID.randomUUID();
        underTest.store(testUserId, activity);

        Activity activityStored = activityStore.getLastActivity();
        Assert.assertEquals(activity, activityStored);
    }

}