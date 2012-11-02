package io.beancounter.usermanager.grabber;

import com.google.common.collect.ImmutableList;
import io.beancounter.commons.model.activity.ResolvedActivity;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * @author Alex Cowell
 */
public final class GrabberTask implements Callable<Void> {

    private final ActivityGrabber grabber;
    private final Callback<List<ResolvedActivity>> callback;

    public GrabberTask(ActivityGrabber grabber, Callback<List<ResolvedActivity>> callback) {
        this.grabber = grabber;
        this.callback = callback;
    }

    @Override
    public Void call() throws Exception {
        ImmutableList<ResolvedActivity> result = ImmutableList.copyOf(grabber.grab());
        callback.complete(result);

        return null;
    }
}
