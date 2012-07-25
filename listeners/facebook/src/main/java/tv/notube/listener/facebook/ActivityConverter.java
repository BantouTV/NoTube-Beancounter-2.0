package tv.notube.listener.facebook;

import java.util.List;

import tv.notube.commons.model.activity.Activity;
import tv.notube.listener.facebook.model.FacebookNotification;

/**
 * Every implementation able to convert a {@link FacebookNotification} into
 * a {@link List} of <i>beancounter.io</i> {@link Activity}
 */
public interface ActivityConverter {

    /**
     *
     * @param notification
     * @return
     * @throws ActivityConverterException
     */
    public List<Activity> getActivities(FacebookNotification notification)
            throws ActivityConverterException;
}
