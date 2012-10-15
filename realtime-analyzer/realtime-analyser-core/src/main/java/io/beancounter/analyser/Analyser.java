package io.beancounter.analyser;

import io.beancounter.commons.model.AnalysisResult;
import io.beancounter.commons.model.UserProfile;
import io.beancounter.commons.model.activity.Activity;

import java.util.Map;
import java.util.UUID;

/**
 * This interface defines the behavior of a <i>beancounter.io</i> real-time analyzer.
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public interface Analyser {

    /**
     * Reloads all the started filters.
     *
     * @throws AnalyserException
     * @see {@link io.beancounter.analyser.analysis.Analysis}.
     */
    public void refresh() throws AnalyserException;

    /**
     * Reloads in memory only the given {@link io.beancounter.analyser.analysis.Analysis}.
     *
     * @param analysis
     * @throws AnalyserException
     * @see {@link io.beancounter.analyser.analysis.Analysis}.
     */
    public void refresh(UUID analysis) throws AnalyserException;

    /**
     * It runs all the registered analyses and returns all of the computed results.
     *
     * @param activity
     * @return
     * @throws AnalyserException
     */
    public Map<String, AnalysisResult> analyse(Activity activity) throws AnalyserException;

    /**
     * It runs all the registered analyses and returns all of the computed results.
     *
     * @param profile
     * @return
     * @throws AnalyserException
     */
    public Map<String, AnalysisResult> analyse(UserProfile profile) throws AnalyserException;

}
