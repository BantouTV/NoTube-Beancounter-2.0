package io.beancounter.jmspublisher.process;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.rainet.portal.cms.client.integration.lightstreamer.LightstreamerDTO;
import io.beancounter.commons.model.activity.Object;
import io.beancounter.commons.model.activity.ResolvedActivity;
import io.beancounter.commons.model.activity.rai.Comment;

public class ActivityToJmsConverter {
    public static final String TYPE = "BC";
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


