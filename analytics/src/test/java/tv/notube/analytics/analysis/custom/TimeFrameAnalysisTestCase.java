package tv.notube.analytics.analysis.custom;

import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import tv.notube.analytics.analysis.AnalysisException;
import tv.notube.analytics.analysis.AnalysisResult;
import tv.notube.commons.storage.alog.DefaultActivityLogImpl;
import tv.notube.commons.storage.model.ActivityLog;
import tv.notube.commons.storage.model.Query;
import tv.notube.commons.storage.model.fields.Field;
import tv.notube.commons.storage.model.fields.StringField;

import java.util.Properties;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class TimeFrameAnalysisTestCase {

    private TimeFrameAnalysis timeFrameAnalysis;

    @BeforeTest
    public void setUp() throws AnalysisException {
        Properties prop = new Properties();
        prop.setProperty("url", "jdbc:mysql://moth.notube.tv:3306/alog");
        prop.setProperty("username", "alog");
        prop.setProperty("password", "alog");
        ActivityLog alog = new DefaultActivityLogImpl(prop);
        timeFrameAnalysis = new TimeFrameAnalysis(
                alog,
                "user-activity-analysis",
                "this analysis summarized the user activities"
        );

        Query query = new Query();
        Field tweet = new StringField("verb", "TWEET");
        query.push(tweet, Query.Math.EQ);
        query.push(Query.Boolean.OR);
        Field listen = new StringField("verb", "LISTEN");
        query.push(listen, Query.Math.EQ);
        timeFrameAnalysis.registerQuery(query);
    }

    @Test(enabled = false)
    public void testRun() throws AnalysisException {
        AnalysisResult analysisResult =
                timeFrameAnalysis.run("user-manager-8c33b0e6-d3cf-4909-b04c-df93056e64a8");
        Assert.assertNotNull(analysisResult);
    }

}
