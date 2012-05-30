package tv.notube.analytics;

import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import tv.notube.analytics.analysis.AnalysisException;
import tv.notube.analytics.analysis.custom.ActivityAnalysis;
import tv.notube.analytics.analysis.custom.ActivityAnalysisResult;
import tv.notube.analytics.analysis.custom.TimeFrameAnalysis;
import tv.notube.analytics.analysis.custom.TimeFrameAnalysisResult;
import tv.notube.commons.configuration.analytics.AnalysisDescription;
import tv.notube.commons.configuration.analytics.MethodDescription;
import tv.notube.commons.storage.alog.DefaultActivityLogImpl;
import tv.notube.commons.storage.kvs.KVStore;
import tv.notube.commons.storage.kvs.mybatis.MyBatisKVStore;
import tv.notube.commons.storage.model.ActivityLog;
import tv.notube.commons.storage.model.Query;
import tv.notube.commons.storage.model.fields.Field;
import tv.notube.commons.storage.model.fields.StringField;
import tv.notube.commons.storage.model.fields.serialization.SerializationManager;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class StorageAnalyzerImplTestCase {

// TODO libby - not working for me
// java.lang.AssertionError: expected object to not be null
    private static final String TIMEFRAME_ANALYSIS = "timeframe-analysis";

    private static final String ACTIVITY_ANALYSIS = "activity-analysis";

    private Analyzer analyzer;

    @BeforeTest(enabled = false)
    public void setUp() throws AnalysisException, AnalyzerException {
        analyzer = getAnalyzer();
        MethodDescription getAmount;
        getAmount = new MethodDescription(
                "getNumberOfActivitiesByVerb",
                "it returns the total number of activities of the verb specified as parameter",
                new String[] { "java.lang.String" }
        );

        MethodDescription getActivities;
        getActivities = new MethodDescription(
                "getActivities",
                "it returns the activity types the user performed",
                new String[] {}
        );

        MethodDescription getTotalActivities;
        getTotalActivities = new MethodDescription(
                "getTotalActivities",
                "it returns the total number of activities of the user",
                new String[] {}
        );

        MethodDescription getServiceAmount;
        getServiceAmount = new MethodDescription(
                "getNumberOfActivitiesByService",
                "it returns the total number of activities done on a " +
                        "specified service (must be a valid URL)",
                new String[] { "java.net.URL" }
        );

        Set<MethodDescription> aadMds = new HashSet<MethodDescription>();
        aadMds.add(getAmount);
        aadMds.add(getActivities);
        aadMds.add(getTotalActivities);
        aadMds.add(getServiceAmount);
        AnalysisDescription aad = new AnalysisDescription(
                ACTIVITY_ANALYSIS,
                "this analysis summarizes the user activities",
                getQuery(),
                ActivityAnalysis.class.getCanonicalName(),
                ActivityAnalysisResult.class.getCanonicalName(),
                aadMds
        );

        MethodDescription getStatistics;
        getStatistics = new MethodDescription(
                "getStatistics",
                "this method returns activity statistics day by day of the last month",
                new String[] { "java.lang.Integer" }
        );
        Set<MethodDescription> tfdMds = new HashSet<MethodDescription>();
        tfdMds.add(getStatistics);
        AnalysisDescription tad = new AnalysisDescription(
                TIMEFRAME_ANALYSIS,
                "this analysis summarizes the user activities over time",
                getQuery(),
                TimeFrameAnalysis.class.getCanonicalName(),
                TimeFrameAnalysisResult.class.getCanonicalName(),
                tfdMds
        );
        analyzer.registerAnalysis(aad, true);
        analyzer.registerAnalysis(tad, true);
    }

    private Query getQuery() {
        Query query = new Query();
        Field tweet = new StringField("verb", "TWEET");
        query.push(tweet, Query.Math.EQ);
        query.push(Query.Boolean.OR);
        Field listen = new StringField("verb", "LISTEN");
        query.push(listen, Query.Math.EQ);
        return query;
    }

    private Analyzer getAnalyzer() {
        Properties prop = new Properties();
        prop.setProperty("url", "jdbc:mysql://moth.notube.tv:3306/alog");
        prop.setProperty("username", "alog");
        prop.setProperty("password", "alog");
        ActivityLog alog = new DefaultActivityLogImpl(prop);
        prop = new Properties();
        prop.setProperty("url", "jdbc:mysql://moth.notube.tv:3306/kvs");
        prop.setProperty("username", "kvs");
        prop.setProperty("password", "kvs");
        KVStore kvs = new MyBatisKVStore(prop, new SerializationManager());
        return new DefaultAnalyzerImpl(kvs, alog);
    }

    @Test(enabled = false)
    public void testExecution() throws AnalyzerException {
        AnalysisDescription analysisDescriptions[] =
                analyzer.getRegisteredAnalysis();
        Assert.assertEquals(2, analysisDescriptions.length);

        analyzer.run("user-manager-8c33b0e6-d3cf-4909-b04c-df93056e64a8");
        TimeFrameAnalysisResult tfActual =
                (TimeFrameAnalysisResult) analyzer.getResult(
                        TIMEFRAME_ANALYSIS,
                        "user-manager-8c33b0e6-d3cf-4909-b04c-df93056e64a8"
                );
        ActivityAnalysisResult aaActual =
                (ActivityAnalysisResult) analyzer.getResult(
                        ACTIVITY_ANALYSIS,
                        "user-manager-8c33b0e6-d3cf-4909-b04c-df93056e64a8"
                );

        Assert.assertNotNull(tfActual);
        Assert.assertNotNull(aaActual);

        analyzer.flush(
                ACTIVITY_ANALYSIS,
                "user-manager-8c33b0e6-d3cf-4909-b04c-df93056e64a8"
        );
        analyzer.flush(
                TIMEFRAME_ANALYSIS,
                "user-manager-8c33b0e6-d3cf-4909-b04c-df93056e64a8"
        );

        analyzer.deregisterAnalysis(TIMEFRAME_ANALYSIS);
        analyzer.deregisterAnalysis(ACTIVITY_ANALYSIS);

        analysisDescriptions =
                analyzer.getRegisteredAnalysis();
        Assert.assertEquals(0, analysisDescriptions.length);
    }

}
