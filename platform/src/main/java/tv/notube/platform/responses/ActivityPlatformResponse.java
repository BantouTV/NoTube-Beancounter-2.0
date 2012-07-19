package tv.notube.platform.responses;

import tv.notube.commons.model.activity.Activity;
import tv.notube.platform.PlatformResponse;

/**
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
public class ActivityPlatformResponse extends PlatformResponse<Activity> {

    private Activity activity;

    public ActivityPlatformResponse(){}

    public ActivityPlatformResponse(Status s, String m) {
        super(s, m);
    }

    public ActivityPlatformResponse(Status s, String m, Activity act) {
        super(s, m);
        activity = act;
    }

    public Activity getObject() {
        return activity;
    }

    public void setObject(Activity act) {
        this.activity = act;
    }
}