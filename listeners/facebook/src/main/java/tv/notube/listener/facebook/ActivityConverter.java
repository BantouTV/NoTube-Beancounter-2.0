package tv.notube.listener.facebook;

import java.util.List;

import tv.notube.commons.model.activity.Activity;
import tv.notube.listener.facebook.model.FacebookNotification;

public interface ActivityConverter {
    List<Activity> getActivities(FacebookNotification notification);
}
