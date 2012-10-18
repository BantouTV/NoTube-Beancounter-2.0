package io.beancounter.analyser.examples;

import io.beancounter.analyser.analysis.Analysis;
import io.beancounter.analyser.analysis.AnalysisException;
import io.beancounter.commons.model.AnalysisResult;
import io.beancounter.commons.model.UserProfile;
import io.beancounter.commons.model.activity.Activity;
import org.joda.time.DateTime;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class ActivityFrequencyRateAnalysis extends Analysis {

    private final static String RATE = "rate";

    private final static String ACTIVITIES = "activities";

    private final static String PROFILES = "profiles";

    private final static String LAST_TS = "timestamp";

    @Override
    public AnalysisResult run(Activity activity, AnalysisResult previous) throws AnalysisException {
        AnalysisResult result = new AnalysisResult(getName());
        if(previous == null) {
            result.setValue(ACTIVITIES + '.' + RATE, 0.0d);
            result.setValue(ACTIVITIES + '.' + LAST_TS, DateTime.now().getMillis());
            return result;
        }
        // it seems it's not the first one
        long lastUpdate = Long.valueOf(previous.getResults().get(ACTIVITIES + '.' + LAST_TS));
        long now = DateTime.now().getMillis();
        double perSecond = 1 / ((now - lastUpdate) * 1000);
        result.setValue(ACTIVITIES + '.' + RATE, perSecond);
        result.setValue(ACTIVITIES + '.' + LAST_TS, now);
        return result;
    }

    @Override
    public AnalysisResult run(UserProfile userProfile, AnalysisResult previous) throws AnalysisException {
        AnalysisResult result = new AnalysisResult(getName());
        if(previous == null) {
            result.setValue(PROFILES + '.' + RATE, 0.0d);
            result.setValue(PROFILES + '.' + LAST_TS, DateTime.now().getMillis());
            return result;
        }
        // it seems it's not the first one
        long lastUpdate = Long.valueOf(previous.getResults().get(PROFILES + '.' + LAST_TS));
        long now = DateTime.now().getMillis();
        double perSecond = 1 / ((now - lastUpdate) * 1000);
        result.setValue(PROFILES + '.' + RATE, perSecond);
        result.setValue(PROFILES + '.' + LAST_TS, now);
        return result;
    }
}
