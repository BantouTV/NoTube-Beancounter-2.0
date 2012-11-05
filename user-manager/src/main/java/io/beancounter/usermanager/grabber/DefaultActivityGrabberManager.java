package io.beancounter.usermanager.grabber;

import com.google.inject.Inject;
import io.beancounter.commons.model.activity.ResolvedActivity;

import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * An asynchronous implementation of the ActivityGrabberManager which uses
 * a pluggable ExecutorService to manage threads.
 *
 * @author Alex Cowell
 */
public class DefaultActivityGrabberManager implements ActivityGrabberManager {

    private final ExecutorService executorService;

    @Inject
    public DefaultActivityGrabberManager(ExecutorService executorService) {
        this.executorService = executorService;
    }

    @Override
    public void submit(ActivityGrabber grabber, Callback<List<ResolvedActivity>> callback) {
        executorService.submit(new GrabberTask(grabber, callback));
    }
}
