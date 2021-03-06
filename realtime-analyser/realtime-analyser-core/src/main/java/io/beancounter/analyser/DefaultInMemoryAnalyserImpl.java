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
        throw new UnsupportedOperationException("NIY");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void refresh(UUID analysis) throws AnalyserException {
        throw new UnsupportedOperationException("NIY");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, AnalysisResult> analyse(Activity activity) throws AnalyserException {
        Collection<Analysis> analyses;
        try {
            analyses = manager.getRegisteredAnalyses();
        } catch (AnalysisManagerException e) {
            throw new AnalyserException();
        }
        Map<String, AnalysisResult> results = new HashMap<String, AnalysisResult>();
        for (Analysis analysis : analyses) {
            // get previous result
            AnalysisResult previous;
            try {
                previous = this.analyses.lookup(analysis.getName());
            } catch (AnalysesException e) {
                throw new AnalyserException();
            }
            AnalysisResult result;
            try {
                result = analysis.run(activity, previous);
            } catch (AnalysisNotApplicableException e) {
                // just skip, log, warn but skip
                continue;
            } catch (AnalysisException e) {
                throw new AnalyserException();
            }
            results.put(analysis.getName(), result);
            storeResult(result);
        }
        return results;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, AnalysisResult> analyse(UserProfile profile) throws AnalyserException {
        Collection<Analysis> analyses;
        try {
            analyses = manager.getRegisteredAnalyses();
        } catch (AnalysisManagerException e) {
            throw new AnalyserException();
        }
        Map<String, AnalysisResult> results = new HashMap<String, AnalysisResult>();
        for (Analysis analysis : analyses) {
            // get previous result
            AnalysisResult previous;
            try {
                previous = this.analyses.lookup(analysis.getName());
            } catch (AnalysesException e) {
                throw new AnalyserException();
            }
            AnalysisResult result;
            try {
                result = analysis.run(profile, previous);
            } catch (AnalysisNotApplicableException e) {
                // just skip, log, warn but skip
                continue;
            } catch (AnalysisException e) {
                throw new AnalyserException();
            }
            results.put(analysis.getName(), result);
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
