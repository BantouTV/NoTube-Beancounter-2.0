package tv.notube.indexer;

import tv.notube.activities.ActivityStoreException;
import tv.notube.commons.model.activity.Activity;

import java.util.UUID;

/**
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
public interface ActivityService {

    void store(UUID userId, Activity activity) throws ActivityStoreException;

}