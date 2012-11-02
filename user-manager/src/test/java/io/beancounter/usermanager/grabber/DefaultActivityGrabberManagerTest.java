package io.beancounter.usermanager.grabber;

import io.beancounter.commons.model.activity.ResolvedActivity;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Alex Cowell
 */
public class DefaultActivityGrabberManagerTest {

    private ActivityGrabberManager activityGrabberManager;
    private ExecutorService executorService;

    @BeforeMethod
    @SuppressWarnings("unchecked")
    public void setUp() throws Exception {
        executorService = mock(ExecutorService.class);
        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                Callable callable = (Callable) args[0];
                return callable.call();
            }
        }).when(executorService).submit(any(Callable.class));

        activityGrabberManager = new DefaultActivityGrabberManager(executorService);
    }

    @Test
    public void callbackShouldBeExecutedAfterGrabberTaskIsComplete() throws Exception {
        @SuppressWarnings("unchecked")
        Callback<List<ResolvedActivity>> callback = mock(Callback.class);
        ActivityGrabber facebookGrabber = mock(ActivityGrabber.class);
        List<ResolvedActivity> expected = new ArrayList<ResolvedActivity>();

        activityGrabberManager.submit(facebookGrabber, callback);

        verify(executorService).submit(any(GrabberTask.class));
        verify(callback).complete(expected);
    }

    @Test
    public void callbackShouldBePassedTheResultOfTheGrabberTask() throws Exception {
        @SuppressWarnings("unchecked")
        Callback<List<ResolvedActivity>> callback = mock(Callback.class);
        ActivityGrabber facebookGrabber = mock(ActivityGrabber.class);
        List<ResolvedActivity> activities = new ArrayList<ResolvedActivity>();
        activities.add(mock(ResolvedActivity.class));
        activities.add(mock(ResolvedActivity.class));
        activities.add(mock(ResolvedActivity.class));

        when(facebookGrabber.grab()).thenReturn(activities);

        activityGrabberManager.submit(facebookGrabber, callback);

        verify(executorService).submit(any(GrabberTask.class));
        verify(callback).complete(activities);
    }
}
