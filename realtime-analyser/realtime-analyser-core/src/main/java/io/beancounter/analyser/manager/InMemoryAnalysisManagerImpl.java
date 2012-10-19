package io.beancounter.analyser.manager;

import com.google.inject.Inject;
import io.beancounter.analyser.analysis.Analysis;

import java.util.Collection;
import java.util.UUID;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class InMemoryAnalysisManagerImpl implements AnalysisManager {

    private Collection<Analysis> analyses;

    @Inject
    public InMemoryAnalysisManagerImpl(InMemoryAnalysisManagerConfig config) {
        analyses = config.getAnalyses();
    }

    public String register(String name, String description) throws AnalysisManagerException {
        throw new UnsupportedOperationException("niy");
    }

    public Analysis get(UUID uuid) throws AnalysisManagerException {
        throw new UnsupportedOperationException("niy");
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

    public Collection<Analysis> getRegisteredAnalyses() throws AnalysisManagerException {
        return analyses;
    }
}
