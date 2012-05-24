package tv.notube.platform;

import org.joda.time.DateTime;
import tv.notube.analytics.Analyzer;
import tv.notube.analytics.AnalyzerException;
import tv.notube.analytics.analysis.AnalysisResult;
import tv.notube.commons.configuration.analytics.AnalysisDescription;
import tv.notube.commons.configuration.analytics.MethodDescription;
import tv.notube.commons.storage.model.Query;

import java.util.HashSet;

/**
 *
 * @author Enrico Candino (enrico.candino@gmail.com)
 */
public class TestAnalyzer implements Analyzer {
    @Override
    public void registerAnalysis(AnalysisDescription analysisDescription, boolean persist) throws AnalyzerException {
        throw new UnsupportedOperationException("NIY");
    }

    @Override
    public AnalysisDescription getAnalysisDescription(String name) throws AnalyzerException {
        return new AnalysisDescription(
                "test-analysis-1",
                "fake analysis 1",
                new Query(),
                "com.test.fake.First",
                "com.test.fake.first.Result",
                new HashSet<MethodDescription>()
        );
    }

    @Override
    public void run(String owner) throws AnalyzerException {
        throw new UnsupportedOperationException("NIY");
    }

    @Override
    public AnalysisResult getResult(String name, String username) throws AnalyzerException {
        AnalysisResult ar = new AnalysisResult(DateTime.now());
        ar.setName(name);
        ar.setUser(username);
        return ar;
    }

    @Override
    public void deregisterAnalysis(String name) throws AnalyzerException {
        throw new UnsupportedOperationException("NIY");
    }

    @Override
    public AnalysisDescription[] getRegisteredAnalysis() throws AnalyzerException {
        return getTestData();
    }

    public AnalysisDescription[] getTestData() {
        AnalysisDescription[] analysisDescriptions = new AnalysisDescription[2];
        analysisDescriptions[0] = new AnalysisDescription(
                "test-analysis-1",
                "fake analysis 1",
                new Query(),
                "com.test.fake.First",
                "com.test.fake.first.Result",
                new HashSet<MethodDescription>()
        );
        analysisDescriptions[1] = new AnalysisDescription(
                "test-analysis-2",
                "fake analysis 2",
                new Query(),
                "com.test.fake.Second",
                "com.test.fake.second.Result",
                new HashSet<MethodDescription>()
        );
        return analysisDescriptions;
    }

    @Override
    public void flush(String name, String username) throws AnalyzerException {
        throw new UnsupportedOperationException("NIY");
    }
}