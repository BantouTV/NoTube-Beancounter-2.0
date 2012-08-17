package io.beancounter.platform.responses;

import io.beancounter.commons.model.activity.ResolvedActivity;
import io.beancounter.platform.PlatformResponse;

import java.util.Collection;

/**
 * @author Enrico Candino ( enrico.candino@gmail.com )
 * @author Davide Palmisano ( dpalmisano.gmail.com )
 */
public class ResolvedActivitiesPlatformResponse extends PlatformResponse<Collection<ResolvedActivity>> {

    private Collection<ResolvedActivity> activities;

    public ResolvedActivitiesPlatformResponse(){}

    public ResolvedActivitiesPlatformResponse(Status s, String m) {
        super(s, m);
    }

    public ResolvedActivitiesPlatformResponse(Status s, String m, Collection<ResolvedActivity> act) {
        super(s, m);
        activities = act;
    }

    public Collection<ResolvedActivity> getObject() {
        return activities;
    }

    public void setObject(Collection<ResolvedActivity> act) {
        this.activities = act;
    }
}