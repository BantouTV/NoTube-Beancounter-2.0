package tv.notube.profiler;

import tv.notube.profiler.container.ProfilingLineContainer;
import tv.notube.profiler.storage.ProfileStore;

import java.util.UUID;

/**
 *
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
public interface Profiler {

    public void run() throws ProfilerException;

    public void run(UUID userId) throws ProfilerException;

    public String profilingStatus(UUID userId);

    public ProfilingLineContainer getProfilingContainer();

    public ProfileStore getProfileStore();

    public Object getObject(UUID userId) throws ProfilerException;

}
