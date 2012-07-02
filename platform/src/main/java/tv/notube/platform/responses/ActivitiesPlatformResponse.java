package tv.notube.platform.responses;

import tv.notube.commons.model.activity.Activity;
import tv.notube.platform.PlatformResponse;

import java.util.Collection;

/**
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
public class ActivitiesPlatformResponse extends PlatformResponse<Collection<Activity>> {

    private Collection<Activity> activities;

    public ActivitiesPlatformResponse(){}

    public ActivitiesPlatformResponse(Status s, String m) {
        super(s, m);
    }

    public ActivitiesPlatformResponse(Status s, String m, Collection<Activity> act) {
        super(s, m);
        activities = act;
    }

    public Collection<Activity> getObject() {
        return activities;
    }

    public void setObject(Collection<Activity> act) {
        this.activities = act;
    }
}