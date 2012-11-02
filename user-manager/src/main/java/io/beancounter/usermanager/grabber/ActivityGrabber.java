package io.beancounter.usermanager.grabber;

import io.beancounter.commons.model.activity.ResolvedActivity;

import java.util.List;

/**
 * @author Alex Cowell
 */
public interface ActivityGrabber {

    List<ResolvedActivity> grab();
}
