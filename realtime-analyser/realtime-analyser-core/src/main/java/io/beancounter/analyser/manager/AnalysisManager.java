package io.beancounter.analyser.manager;

import io.beancounter.analyser.analysis.Analysis;

import java.util.Collection;
import java.util.UUID;

/**
 * This interface defines the minimum contract to perform CRUD operations
 * on {@link io.beancounter.analyser.analysis.Analysis}.
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public interface AnalysisManager {

    public String register(
            String name,
            String description
    ) throws AnalysisManagerException;

    public Analysis get(UUID uuid) throws AnalysisManagerException;

    public void delete(UUID uuid) throws AnalysisManagerException;

    public void start(UUID uuid) throws AnalysisManagerException;

    public void stop(UUID uuid) throws AnalysisManagerException;

    public Collection<UUID> getRegisteredAnalysis() throws AnalysisManagerException;

}
