package tv.notube.profiler;

import tv.notube.commons.model.UserProfile;
import tv.notube.profiler.container.ProfilingLineContainer;
import tv.notube.profiler.container.ProfilingLineContainerException;
import tv.notube.profiler.line.ProfilingInput;
import tv.notube.profiler.line.ProfilingResult;
import tv.notube.profiler.storage.ProfileStore;
import tv.notube.profiler.storage.ProfileStoreException;
import tv.notube.synch.client.Helper;
import tv.notube.synch.client.SynchronizerClientException;

import java.util.UUID;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class ProfilerRunnable implements Runnable {

    private UUID userId;

    private Object objectToProfile;

    private String profilingLine;

    private ProfilingLineContainer profilingLineContainer;

    private ProfileStore profileStore;

    private Profiler profiler;

    public ProfilerRunnable(
            UUID userId,
            Object objectToProfile,
            String profilingLine,
            ProfilingLineContainer profilingLineContainer,
            ProfileStore profileStore,
            Profiler profiler
    ) {
        this.userId = userId;
        this.objectToProfile = objectToProfile;
        this.profilingLine = profilingLine;
        this.profilingLineContainer = profilingLineContainer;
        this.profileStore = profileStore;
        this.profiler = profiler;
    }

    public void run() {
        profiler.profilingStarted(userId);
        UUID token;
        Helper helper = Helper.getInstance("http://moth.notube.tv:9090/service-1.0-SNAPSHOT/rest/synch");
        try {
            token = helper.access("platform-profiler-" + userId.toString());
        } catch (SynchronizerClientException e) {
            profiler.profilingEnded(userId);
            throw new RuntimeException("Error while getting access synch", e);
        }
        ProfilingResult profilingResult;
        try {
            profilingResult = profilingLineContainer.profile(
                    new ProfilingInput(objectToProfile),
                    profilingLine
            );
        } catch (ProfilingLineContainerException e) {
            try {
                helper.release("platform-profiler-" + userId.toString(), token);
            } catch (SynchronizerClientException e1) {
                profiler.profilingEnded(userId);
                throw new RuntimeException("Error while releasing access to db", e1);
            }
            profiler.profilingEnded(userId);
            final String errMsg = "Error while profiling object: ["
                    + objectToProfile + "] on profiling line [" + profilingLine + "]";
            throw new RuntimeException(errMsg, e);
        }
        UserProfile profileToStore = (UserProfile) profilingResult.getValue();
        try {
            profileStore.deleteUserProfile(profileToStore.getUsername());
            profileStore.storeUserProfile(profileToStore);
        } catch (ProfileStoreException e) {
            try {
                helper.release("platform-profiler-" + userId.toString(), token);
            } catch (SynchronizerClientException e1) {
                profiler.profilingEnded(userId);
                throw new RuntimeException("Error while releasing access to db", e1);
            }
            final String errMsg = "Error while storing profile: '" + profileToStore + "' on ProfileStore";
            throw new RuntimeException(errMsg, e);
        }
        try {
            helper.release("platform-profiler-" + userId.toString(), token);
        } catch (SynchronizerClientException e) {
            profiler.profilingEnded(userId);
            throw new RuntimeException("Error while releasing access to db");
        }
        profiler.profilingEnded(userId);
    }
}
