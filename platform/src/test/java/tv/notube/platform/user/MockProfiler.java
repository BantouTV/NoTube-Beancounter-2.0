package tv.notube.platform.user;

import tv.notube.profiler.Profiler;
import tv.notube.profiler.ProfilerException;
import tv.notube.profiler.container.ProfilingLineContainer;
import tv.notube.profiler.storage.ProfileStore;

import java.util.UUID;

/**
 *
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
public class MockProfiler implements Profiler {

    @Override
    public void run() throws ProfilerException {}

    @Override
    public void run(UUID userId) throws ProfilerException {}

    @Override
    public String profilingStatus(UUID userId) {
        return "TEST-PROFILING-STATUS";
    }

    @Override
    public ProfilingLineContainer getProfilingContainer() {
        throw new UnsupportedOperationException("NIY");
    }

    @Override
    public ProfileStore getProfileStore() {
        throw new UnsupportedOperationException("NIY");
    }

    @Override
    public Object getObject(UUID userId) throws ProfilerException {
        throw new UnsupportedOperationException("NIY");
    }
}