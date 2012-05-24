package tv.notube.platform.analytics;

import org.joda.time.DateTime;
import tv.notube.analytics.analysis.AnalysisResult;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class FakeOneResult extends AnalysisResult {

    public FakeOneResult(DateTime dateTime) {
        super(dateTime);
    }

    public String getFakeSomething(Integer p) {
        return "hey, [" + p + "] is your fake.";
    }

    public String getFakeSomething() {
        return "hey, no parameters here.";
    }

}
