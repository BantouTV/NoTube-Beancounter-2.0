package io.beancounter.analyser.manager;

import io.beancounter.analyser.analysis.Analysis;
import org.testng.annotations.Test;

import java.util.Collection;
import java.util.Collections;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class InMemoryAnalysisManagerImplTest {

    private AnalysisManager analysisManager;

    @Test
    public void givenNoConfiguredAnalysesWhenGettingAnalysesThenReturnZeroAnalyses() throws Exception {
        InMemoryAnalysisManagerConfig config = mock(InMemoryAnalysisManagerConfig.class);
        when(config.getAnalyses()).thenReturn(Collections.<Analysis>emptyList());

        analysisManager = new InMemoryAnalysisManagerImpl(config);
        assertTrue(analysisManager.getRegisteredAnalyses().isEmpty());
    }

    @Test
    public void givenTwoConfiguredAnalysesWhenGettingAnalysesThenReturnTwoAnalyses() throws Exception {
        @SuppressWarnings("unchecked")
        Collection<Analysis> analyses = mock(Collection.class);
        InMemoryAnalysisManagerConfig config = mock(InMemoryAnalysisManagerConfig.class);

        when(config.getAnalyses()).thenReturn(analyses);
        when(analyses.size()).thenReturn(2);

        analysisManager = new InMemoryAnalysisManagerImpl(config);
        assertEquals(analysisManager.getRegisteredAnalyses().size(), 2);
    }
}
