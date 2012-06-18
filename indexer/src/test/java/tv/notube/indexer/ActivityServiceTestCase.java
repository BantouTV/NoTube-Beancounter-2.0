package tv.notube.indexer;

import junit.framework.Assert;
import org.testng.annotations.Test;
import tv.notube.activities.ActivityStoreException;
import tv.notube.commons.model.activity.Activity;
import tv.notube.commons.tests.TestsException;

/**
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
public class ActivityServiceTestCase {

    @Test
    public void storeSingleActivity() throws TestsException, ActivityStoreException {
        MockActivityStore activityStore = new MockActivityStore();
        ActivityService underTest = new ActivityServiceImpl(activityStore);
        Activity activity = new Activity();

        underTest.store(activity);

        Activity activityStored = activityStore.getLastActivity();
        Assert.assertEquals(activity, activityStored);
    }

}