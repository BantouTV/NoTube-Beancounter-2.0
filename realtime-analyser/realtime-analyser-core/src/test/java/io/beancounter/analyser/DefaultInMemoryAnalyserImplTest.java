package io.beancounter.analyser;

import io.beancounter.analyser.analysis.Analysis;
import io.beancounter.analyser.manager.AnalysisManager;
import io.beancounter.analyses.Analyses;
import io.beancounter.commons.model.AnalysisResult;
import io.beancounter.commons.model.UserProfile;
import io.beancounter.commons.model.activity.Activity;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertNotNull;

public class DefaultInMemoryAnalyserImplTest {

    private Analyser analyser;
    private Analyses analysisResultsStore;
    private AnalysisManager analysisManager;

    @BeforeMethod
    public void setUp() throws Exception {
        analysisResultsStore = mock(Analyses.class);
        analysisManager = mock(AnalysisManager.class);
        analyser = new DefaultInMemoryAnalyserImpl(analysisResultsStore, analysisManager);
    }

    @Test
    public void givenNoRegisteredAnalysesWhenAnalysingActivityThenReturnEmptyMap() throws Exception {
        Activity activity = mock(Activity.class);
        Map<String, AnalysisResult> analysisResults = analyser.analyse(activity);
        assertNotNull(analysisResults);
    }

    @Test
    public void givenNoRegisteredAnalysesWhenAnalysingProfileTenReturnEmptyMap() throws Exception {
        UserProfile profile = mock(UserProfile.class);
        Map<String, AnalysisResult> analysisResults = analyser.analyse(profile);
        assertNotNull(analysisResults);
    }

    @Test
    public void givenOneRegisteredAnalysisWhenAnalysingActivityThenReturnOneResult() throws Exception {
        Activity activity = mock(Activity.class);
        Analysis analysis = mock(Analysis.class);
        Collection<Analysis> analyses = new ArrayList<Analysis>();
        analyses.add(analysis);

        when(analysisManager.getRegisteredAnalyses()).thenReturn(analyses);

        Map<String, AnalysisResult> analysisResults = analyser.analyse(activity);
        assertNotNull(analysisResults);
    }
}
