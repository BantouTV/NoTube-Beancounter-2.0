package tv.notube.profiler;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import tv.notube.commons.configuration.Configurations;
import tv.notube.commons.model.activity.*;
import tv.notube.profiler.container.DefaultProfilingLineContainer;
import tv.notube.profiler.container.ProfilingLineContainer;
import tv.notube.profiler.storage.ProfileStore;

import java.lang.Object;
import java.util.UUID;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class ProfilerRunnableTestCase {

    private ProfilerRunnable runnable;

    private UUID userId = UUID.fromString("4363d649-105f-45a4-a860-d999df419c78");

    @Test
    public void test() throws ProfilerException {
        Profiler profiler = DefaultProfilerFactory.getInstance().build();
        ProfileStore ps = profiler.getProfileStore();
        ProfilingLineContainer plContainer = profiler.getProfilingContainer();
        Object o = profiler.getObject(userId);
        runnable = new ProfilerRunnable(
                userId,
                o,
                "profiling-line",
                plContainer,
                ps,
                profiler
        );
        runnable.run();
    }



}
