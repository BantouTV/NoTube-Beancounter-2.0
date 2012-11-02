package io.beancounter.usermanager.grabber;

import io.beancounter.commons.model.activity.ResolvedActivity;

import java.util.List;

/**
 * Responsible for retrieving and resolving user activities from social web
 * services. It will then use a callback to do something with the retrieved
 * activities.
 *
 * @author Alex Cowell
 */
public interface ActivityGrabberManager {

    /**
     * Uses the ActivityGrabber to retrieve activities from a social web service
     * such as Facebook or Twitter, then pass the result to the Callback to do
     * something with it.
     *
     * @param grabber The ActivityGrabber which will be used to grab activities
     * from a social web service.
     * @param callback The Callback to do something with the activities once
     * they have been retrieved.
     */
    void submit(ActivityGrabber grabber, Callback<List<ResolvedActivity>> callback);
}
