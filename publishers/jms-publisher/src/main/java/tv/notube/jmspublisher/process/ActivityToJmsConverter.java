package tv.notube.jmspublisher.process;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.rainet.portal.cms.client.integration.lightstreamer.LightstreamerDTO;
import tv.notube.commons.model.activity.Object;
import tv.notube.commons.model.activity.ResolvedActivity;
import tv.notube.commons.model.activity.rai.Comment;

public class ActivityToJmsConverter {
    public static final String TYPE = "beancounter";
    private static final Logger LOG = LoggerFactory.getLogger(ActivityToJmsConverter.class);

    public LightstreamerDTO wrapInExternalObject(ResolvedActivity resolvedActivity, String json) {
        LOG.debug("Converting activity: {}", json);
        Object object = resolvedActivity.getActivity().getObject();
        if (object instanceof Comment) {
            Comment comment = (Comment)object;
            String event = comment.getOnEvent();
            if (event != null && event.length() > 0) {
                LOG.debug("Converted activity: {}", json);
                return new LightstreamerDTO(event, json, TYPE);
            }
        }
        return null;
    }
}


