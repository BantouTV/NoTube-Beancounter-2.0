package io.beancounter.jmspublisher.process;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import it.rainet.portal.cms.client.integration.lightstreamer.LightstreamerDTO;
import io.beancounter.commons.model.activity.ResolvedActivity;
import io.beancounter.commons.model.activity.rai.Comment;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.isNull;

public class ActivityToJmsConverterTest {
    private ActivityToJmsConverter activityToJmsConverter;

    @BeforeMethod
    public void setUp() throws Exception {
        activityToJmsConverter = new ActivityToJmsConverter();
    }

    @Test
    public void wrapsTheInputIntoThirdPartyBeanWhenCommentHasOnEventValue() throws Exception {
        String json = "{'key':'value'}";
        String onEvent = "onUserClick";
        Comment comment = new Comment();
        comment.setOnEvent(onEvent);

        ResolvedActivity resolvedActivity = ActivityBuilder.aResolvedActivity();
        resolvedActivity.getActivity().setObject(comment);

        LightstreamerDTO lightstreamerDTO = activityToJmsConverter.wrapInExternalObject(resolvedActivity, json);

        assertThat(lightstreamerDTO.getBody(), is(json));
        assertThat(lightstreamerDTO.getOwner(), is(onEvent));
        assertThat(lightstreamerDTO.getType(), is(ActivityToJmsConverter.TYPE));
    }

    @Test
       public void ignoresTheInputWhenCommentHasNoOnEventValue() throws Exception {
           String json = "{'key':'value'}";
           String onEvent = "";
           Comment comment = new Comment();
           comment.setOnEvent(onEvent);

           ResolvedActivity resolvedActivity = ActivityBuilder.aResolvedActivity();
           resolvedActivity.getActivity().setObject(comment);

           LightstreamerDTO lightstreamerDTO = activityToJmsConverter.wrapInExternalObject(resolvedActivity, json);

           assertThat(lightstreamerDTO, is(nullValue()));
       }
}
