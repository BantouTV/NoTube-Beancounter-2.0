package io.beancounter.analyser.examples;

import io.beancounter.analyser.analysis.Analysis;
import io.beancounter.analyser.analysis.AnalysisException;
import io.beancounter.analyser.analysis.AnalysisNotApplicableException;
import io.beancounter.commons.model.AnalysisResult;
import io.beancounter.commons.model.Interest;
import io.beancounter.commons.model.UserProfile;
import io.beancounter.commons.model.activity.Activity;
import org.joda.time.DateTime;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class TrendingInterestsAnalysis extends Analysis {

    /**
     * top trends to be shown
     */
    private final static int TOP = 2;

    @Override
    public AnalysisResult run(Activity activity, AnalysisResult previous) throws AnalysisException {
        throw new AnalysisNotApplicableException("this analysis cannot be applied to activities");
    }

    @Override
    public AnalysisResult run(UserProfile userProfile, AnalysisResult previous) throws AnalysisException {
        AnalysisResult result = new AnalysisResult(getName());
        if (previous == null) {
            // it's the first time this analysis runs
            // as a strategy, just grab the first TOP of this profile
            List<Interest> interests = new ArrayList<Interest>(userProfile.getInterests());
            Map<String, Double> results = computeTop(interests, TOP);
            setResults(result, results);
            result.setLastUpdated(DateTime.now());
            return result;
        }
        // it's not the first time this analysis is triggered
        List<Interest> oldInterests = toInterests(previous.getResults());
        List<Interest> merged = merge(oldInterests, new ArrayList<Interest>(userProfile.getInterests()));
        Map<String, Double> results = computeTop(merged, TOP);
        setResults(result, results);
        result.setLastUpdated(DateTime.now());
        return result;
    }

    private void setResults(AnalysisResult result, Map<String, Double> results) {
        for (Map.Entry<String, Double> r : results.entrySet()) {
            result.setValue(r.getKey(), r.getValue());
        }
    }

    private List<Interest> merge(List<Interest> oldInterests, List<Interest> interests) {
        oldInterests.addAll(interests);
        return oldInterests;
    }

    private List<Interest> toInterests(Map<String, String> results) {
        List<Interest> result = new ArrayList<Interest>();
        for(String interestUri : results.keySet()) {
            try {
                result.add(new Interest("labels-are-not-used", new URI(interestUri)));
            } catch (URISyntaxException e) {
                // uris could be malformed - unlikely but could happens. log and skip
                continue;
            }
        }
        return result;
    }

    private Map<String, Double> computeTop(List<Interest> interests, int top) {
        Map<String, Double> result = new HashMap<String, Double>();
        Collections.sort(interests);
        int index = 0;
        for (Interest i : interests) {
            if(index >= top) break;
            result.put(i.getResource().toString(), i.getWeight());
            index++;
        }
        return result;
    }

}
