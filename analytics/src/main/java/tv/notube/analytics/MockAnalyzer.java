package tv.notube.analytics;

import org.joda.time.DateTime;
import tv.notube.analytics.analysis.AnalysisResult;
import tv.notube.commons.configuration.analytics.AnalysisDescription;
import tv.notube.commons.configuration.analytics.MethodDescription;
import tv.notube.commons.storage.model.Query;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Enrico Candino (enrico.candino@gmail.com)
 */
public class MockAnalyzer implements Analyzer {
    @Override
    public void registerAnalysis(AnalysisDescription analysisDescription, boolean persist) throws AnalyzerException {
        throw new UnsupportedOperationException("NIY");
    }

    @Override
    public AnalysisDescription getAnalysisDescription(String name) throws AnalyzerException {
        return getTestData()[0];
    }

    @Override
    public void run(String owner) throws AnalyzerException {
        throw new UnsupportedOperationException("NIY");
    }

    @Override
    public AnalysisResult getResult(String name, String username) throws AnalyzerException {
        return new FakeOneResult(DateTime.now());
    }

    @Override
    public void deregisterAnalysis(String name) throws AnalyzerException {
        throw new UnsupportedOperationException("NIY");
    }

    @Override
    public AnalysisDescription[] getRegisteredAnalysis() throws AnalyzerException {
        return getTestData();
    }

    private AnalysisDescription[] getTestData() {
        AnalysisDescription[] analysisDescriptions = new AnalysisDescription[2];
        Set<MethodDescription> mds = new HashSet<MethodDescription>();
        mds.add(
                new MethodDescription(
                        "getFakeSomething",
                        "just a fake method",
                        new String[] { "java.lang.Integer" }
                )
        );
        mds.add(
                new MethodDescription(
                        "getFakeSomething",
                        "the same fake method with no parameters",
                        new String[] {}
                )
        );
        analysisDescriptions[0] = new AnalysisDescription(
                "test-analysis-1",
                "fake analysis 1",
                new Query(),
                "tv.notube.analytics.FakeOne",
                "tv.notube.analytics.FakeOneResult",
                mds
        );
        mds = new HashSet<MethodDescription>();
        mds.add(
                new MethodDescription(
                        "getAnotherFakeSomething",
                        "just a another fake method",
                        new String[]{ "java.lang.String", "java.lang.Boolean" }
                )
        );
        analysisDescriptions[1] = new AnalysisDescription(
                "test-analysis-2",
                "fake analysis 2",
                new Query(),
                "com.test.fake.Second",
                "com.test.fake.second.Result",
                mds
        );
        return analysisDescriptions;
    }

    @Override
    public void flush(String name, String username) throws AnalyzerException {
        throw new UnsupportedOperationException("NIY");
    }
}