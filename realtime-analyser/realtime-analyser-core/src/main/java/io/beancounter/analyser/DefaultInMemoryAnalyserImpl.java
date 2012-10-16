package io.beancounter.analyser;

import com.google.inject.Inject;
import io.beancounter.analyser.analysis.Analysis;
import io.beancounter.analyser.analysis.AnalysisException;
import io.beancounter.analyser.analysis.AnalysisNotApplicableException;
import io.beancounter.analyses.Analyses;
import io.beancounter.analyses.AnalysesException;
import io.beancounter.commons.model.AnalysisResult;
import io.beancounter.analyser.manager.AnalysisManager;
import io.beancounter.analyser.manager.AnalysisManagerException;
import io.beancounter.commons.model.UserProfile;
import io.beancounter.commons.model.activity.Activity;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class DefaultInMemoryAnalyserImpl implements Analyser {

    private Analyses analyses;

    private AnalysisManager manager;

    @Inject
    public DefaultInMemoryAnalyserImpl(
            Analyses analyses,
            AnalysisManager manager
    ) {
        this.analyses = analyses;
        this.manager = manager;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void refresh() throws AnalyserException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void refresh(UUID analysis) throws AnalyserException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, AnalysisResult> analyse(Activity activity) throws AnalyserException {
        Collection<UUID> analysesIds;
        try {
            analysesIds = manager.getRegisteredAnalysis();
        } catch (AnalysisManagerException e) {
            throw new AnalyserException();
        }
        Map<String, AnalysisResult> results = new HashMap<String, AnalysisResult>();
        for(UUID analysisId : analysesIds) {
            Analysis analysis;
            try {
                analysis = manager.get(analysisId);
            } catch (AnalysisManagerException e) {
                throw new AnalyserException();
            }
            AnalysisResult result;
            try {
                result = analysis.run(activity);
            } catch (AnalysisNotApplicableException e) {
                // just skip, log, warn but skipy
                continue;
            } catch (AnalysisException e) {
                throw new AnalyserException();
            }
            results.put(analysisId.toString(), result);
        }
        return results;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, AnalysisResult> analyse(UserProfile profile) throws AnalyserException {
        Collection<UUID> analysesIds;
        try {
            analysesIds = manager.getRegisteredAnalysis();
        } catch (AnalysisManagerException e) {
            throw new AnalyserException();
        }
        Map<String, AnalysisResult> results = new HashMap<String, AnalysisResult>();
        for (UUID analysisId : analysesIds) {
            Analysis analysis;
            try {
                analysis = manager.get(analysisId);
            } catch (AnalysisManagerException e) {
                throw new AnalyserException();
            }
            AnalysisResult result;
            try {
                result = analysis.run(profile);
            } catch (AnalysisNotApplicableException e) {
                // just skip, log, warn but skip
                continue;
            } catch (AnalysisException e) {
                throw new AnalyserException();
            }
            results.put(analysisId.toString(), result);
            storeResult(result);
        }
        return results;
    }

    private void storeResult(AnalysisResult result) throws AnalyserException {
        try {
            analyses.store(result);
        } catch (AnalysesException e) {
            throw new AnalyserException();
        }
    }
}
