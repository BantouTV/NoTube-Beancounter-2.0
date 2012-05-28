package tv.notube.platform.responses;

import tv.notube.commons.model.activity.Activity;
import tv.notube.platform.PlatformResponse;

import java.util.List;

/**
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
public class ActivitiesPlatformResponse extends PlatformResponse<List<Activity>> {

    private List<Activity> activities;

    public ActivitiesPlatformResponse(){}

    public ActivitiesPlatformResponse(Status s, String m) {
        super(s, m);
    }

    public ActivitiesPlatformResponse(Status s, String m, List<Activity> act) {
        super(s, m);
        activities = act;
    }

    //@JsonProperty("activities")
    public List<Activity> getObject() {
        return activities;
    }

    public void setObject(List<Activity> act) {
        this.activities = act;
    }
}