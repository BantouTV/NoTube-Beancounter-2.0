package tv.notube.profiler;

import org.apache.log4j.Logger;
import tv.notube.commons.model.UserProfile;
import tv.notube.profiler.container.ProfilingLineContainer;
import tv.notube.profiler.container.ProfilingLineContainerException;
import tv.notube.profiler.data.DataManager;
import tv.notube.profiler.data.DataManagerException;
import tv.notube.profiler.data.RawDataSet;
import tv.notube.profiler.line.ProfilingInput;
import tv.notube.profiler.line.ProfilingResult;
import tv.notube.profiler.storage.ProfileStore;
import tv.notube.profiler.storage.ProfileStoreException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Main class orchestrating all the profiling process.
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class Profiler {

    private static Logger logger = Logger.getLogger(Profiler.class);

    private DataManager dataManager;

    private ProfilingLineContainer profilingLineContainer;

    private ProfileStore profileStore;

    private Map<UUID, String> statuses;

    private ExecutorService executor;

    public Profiler(
            DataManager dataManager,
            ProfilingLineContainer profilingLineContainer,
            ProfileStore profileStore
    ) {
        this.dataManager = dataManager;
        this.profilingLineContainer = profilingLineContainer;
        this.profileStore = profileStore;
        this.statuses = new HashMap<UUID, String>();
        this.executor = Executors.newCachedThreadPool();
    }

    public void run() throws ProfilerException {
        Map<String, List<String>> registeredDataKeys;
        try {
            registeredDataKeys = dataManager.getRegisteredKeys();
        } catch (DataManagerException e) {
            final String errMsg = "Error while accessing to the registered keys";
            logger.error(errMsg, e);
            throw new ProfilerException("Error while accessing to the registered keys", e);
        }
        for (String key : registeredDataKeys.keySet()) {
            RawDataSet rawDataSet;
            try {
                rawDataSet = dataManager.getRawData(key);
            } catch (DataManagerException e) {
                final String errMsg = "Error while accessing raw data for key: " + key + ". Skipping.";
                logger.error(errMsg, e);
                continue;
            }
            while (rawDataSet.hasNext()) {
                Object objectToProfile = rawDataSet.getNext();
                for (String profilingLine : registeredDataKeys.get(key)) {
                    ProfilingResult profilingResult;
                    try {
                        profilingResult = profilingLineContainer.profile(
                                new ProfilingInput(objectToProfile),
                                profilingLine
                        );
                    } catch (ProfilingLineContainerException e) {
                        final String errMsg = "Error while profiling object: '" +
                                objectToProfile + "' on profiling line '" + profilingLine + "'. Skipping.";
                        logger.error(errMsg, e);
                        continue;
                    }
                    UserProfile profileToStore = (UserProfile) profilingResult.getValue();
                    try {
                        profileStore.deleteUserProfile(profileToStore.getUsername());
                        profileStore.storeUserProfile(profileToStore);
                    } catch (ProfileStoreException e) {
                        final String errMsg = "Error while storing profile: '" + profileToStore + "' on ProfileStore";
                        logger.error(errMsg, e);
                        throw new ProfilerException(errMsg, e);
                    }
                }
            }
        }
    }

    private void launch(
            UUID userId,
            Object objectToProfile,
            String profilingLine,
            ProfileStore profileStore,
            Profiler profiler) {
        executor.execute(
                new ProfilerRunnable(
                        userId,
                        objectToProfile,
                        profilingLine,
                        profilingLineContainer,
                        profileStore,
                        profiler
                )
        );
    }

    public void run(UUID userId) throws ProfilerException {
        RawDataSet rawDataSet;
        try {
            rawDataSet = dataManager.getRawData("user", userId);
        } catch (DataManagerException e) {
            final String errMsg = "Error while accessing raw data for key: [user]";
            logger.error(errMsg, e);
            throw new ProfilerException(errMsg, e);
        }
        Map<String, List<String>> registeredDataKeys;
        try {
            registeredDataKeys = dataManager.getRegisteredKeys();
        } catch (DataManagerException e) {
            final String errMsg = "Error while accessing to the registered keys";
            logger.error(errMsg, e);
            throw new ProfilerException("Error while accessing to the registered keys", e);
        }
        while (rawDataSet.hasNext()) {
            Object objectToProfile = rawDataSet.getNext();
            for (String profilingLine : registeredDataKeys.get("user")) {
                launch(
                        userId,
                        objectToProfile,
                        profilingLine,
                        profileStore,
                        this
                );
            }
        }
    }

    public String profilingStatus(UUID userId) {
        String status = statuses.get(userId);
        if(status == null) {
            return "not profiled";
        }
        return status;
    }

    protected synchronized void profilingStarted(UUID userId) {
        final String STATUS = "under profiling";
        if(statuses.containsKey(userId)) {
            statuses.remove(userId);
            statuses.put(userId, STATUS);
        } else {
            statuses.put(userId, STATUS);
        }
    }

    protected synchronized void profilingEnded(UUID userId) {
        final String STATUS = "profiled";
        if(statuses.containsKey(userId)) {
            statuses.remove(userId);
            statuses.put(userId, STATUS);
        } else {
            statuses.put(userId, STATUS);
        }
    }

    public ProfilingLineContainer getProfilingContainer() {
        return profilingLineContainer;
    }

    public ProfileStore getProfileStore() {
        return profileStore;
    }

    public Object getObject(UUID userId) throws ProfilerException {
        RawDataSet rawDataSet;
        try {
            rawDataSet = dataManager.getRawData("user", userId);
        } catch (DataManagerException e) {
            final String errMsg = "Error while accessing raw data for key: [user]";
            logger.error(errMsg, e);
            throw new ProfilerException(errMsg, e);
        }
        Map<String, List<String>> registeredDataKeys;
        try {
            registeredDataKeys = dataManager.getRegisteredKeys();
        } catch (DataManagerException e) {
            final String errMsg = "Error while accessing to the registered keys";
            logger.error(errMsg, e);
            throw new ProfilerException("Error while accessing to the registered keys", e);
        }
        return rawDataSet.getNext();
    }
}
