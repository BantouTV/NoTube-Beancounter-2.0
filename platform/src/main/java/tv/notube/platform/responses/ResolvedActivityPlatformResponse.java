package tv.notube.platform.responses;

import tv.notube.commons.model.activity.ResolvedActivity;
import tv.notube.platform.PlatformResponse;

/**
 * @author Enrico Candino ( enrico.candino@gmail.com )
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class ResolvedActivityPlatformResponse extends PlatformResponse<ResolvedActivity> {

    private ResolvedActivity activity;

    public ResolvedActivityPlatformResponse(){}

    public ResolvedActivityPlatformResponse(Status s, String m) {
        super(s, m);
    }

    public ResolvedActivityPlatformResponse(Status s, String m, ResolvedActivity act) {
        super(s, m);
        activity = act;
    }

    public ResolvedActivity getObject() {
        return activity;
    }

    public void setObject(ResolvedActivity act) {
        this.activity = act;
    }
}