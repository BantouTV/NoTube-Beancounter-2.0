package io.beancounter.analyser.manager;

import io.beancounter.analyser.analysis.Analysis;
import io.beancounter.analyser.analysis.AnalysisException;
import io.beancounter.commons.model.AnalysisResult;
import io.beancounter.commons.model.UserProfile;
import io.beancounter.commons.model.activity.Activity;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class InMemoryAnalysisManagerImpl implements AnalysisManager {

    Collection<Analysis> analyses = new ArrayList<Analysis>();

    public InMemoryAnalysisManagerImpl() {
        analyses.add(new Analysis() {

            private UUID id = UUID.randomUUID();

            @Override
            public AnalysisResult run(Activity activity, AnalysisResult previous) throws AnalysisException {
                AnalysisResult ar = new AnalysisResult(id);
                ar.setLastUpdated(DateTime.now());
                ar.setValue("result.value", "fake");
                ar.setValue("result.type", "activity");
                ar.setValue("activity.id", activity.getId());
                return ar;
            }

            @Override
            public AnalysisResult run(UserProfile userProfile, AnalysisResult previous) throws AnalysisException {
                AnalysisResult ar = new AnalysisResult(id);
                ar.setLastUpdated(DateTime.now());
                ar.setValue("result.value", "fake");
                ar.setValue("result.type", "profile");
                ar.setValue("user.id", userProfile.getUserId());
                return ar;
            }
        });

    }

    public String register(String name, String description) throws AnalysisManagerException {
        throw new UnsupportedOperationException("niy");
    }

    public Analysis get(UUID uuid) throws AnalysisManagerException {
        return analyses.iterator().next();
    }

    public void delete(UUID uuid) throws AnalysisManagerException {
        throw new UnsupportedOperationException("niy");
    }

    public void start(UUID uuid) throws AnalysisManagerException {
        throw new UnsupportedOperationException("niy");
    }

    public void stop(UUID uuid) throws AnalysisManagerException {
        throw new UnsupportedOperationException("niy");
    }

    public Collection<UUID> getRegisteredAnalysis() throws AnalysisManagerException {
        Collection<UUID> uuids = new ArrayList<UUID>();
        for(Analysis analysis : analyses) {
            uuids.add(analysis.getId());
        }
        return uuids;
    }
}
