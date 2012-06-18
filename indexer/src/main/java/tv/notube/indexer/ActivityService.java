package tv.notube.indexer;

import tv.notube.activities.ActivityStoreException;
import tv.notube.commons.model.activity.Activity;

/**
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
public interface ActivityService {

    void store(Activity activity) throws ActivityStoreException;

}