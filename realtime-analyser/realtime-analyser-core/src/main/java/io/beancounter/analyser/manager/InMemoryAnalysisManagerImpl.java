package io.beancounter.analyser.manager;

import io.beancounter.analyser.analysis.Analysis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class InMemoryAnalysisManagerImpl implements AnalysisManager {

    private Collection<Analysis> analyses;

    public InMemoryAnalysisManagerImpl() {
        analyses = new ArrayList<Analysis>();
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
        // TODO (high): For the first sprint, load the analyses found on the
        // classpath and configure them using a properties file. After doing
        // this the first time, just return the same Collection (they won't
        // change).
        return analyses;
    }
}
