package io.beancounter.analyses;

import io.beancounter.commons.model.AnalysisResult;

import java.util.UUID;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public interface Analyses {

    /**
     * It always update regardless what's already there
     *
     * @param ar
     * @throws AnalysesException
     */
    public void store(AnalysisResult ar) throws AnalysesException;

    public AnalysisResult lookup(UUID analysesId) throws AnalysesException;
}
