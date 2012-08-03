package tv.notube.jmspublisher.process;

import it.rainet.portal.cms.client.integration.lightstreamer.LightstreamerDTO;
import tv.notube.commons.model.activity.Object;
import tv.notube.commons.model.activity.ResolvedActivity;
import tv.notube.commons.model.activity.rai.Comment;

public class ActivityToJmsConverter {

    public static final String TYPE = "beancounter";

    public LightstreamerDTO wrapInExternalObject(ResolvedActivity resolvedActivity, String json) {

        System.out.println("sssssssssss");
        Object object = resolvedActivity.getActivity().getObject();
        if (object instanceof Comment) {
            Comment comment = (Comment)object;
            String event = comment.getOnEvent();
            if (event != null && event.length() > 0) {
                return new LightstreamerDTO(event, json, TYPE);
            }
        }
        return null;
    }
}


