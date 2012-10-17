package io.beancounter.analyser.manager;

import org.testng.annotations.Test;

import java.util.Collections;

import static org.testng.Assert.assertEquals;

public class InMemoryAnalysisManagerImplTest {

    @Test
    public void givenNoAnalysesWhenGettingRegisteredAnalysesThenReturnEmptyCollection() throws Exception {
        AnalysisManager analysisManager = new InMemoryAnalysisManagerImpl();
        assertEquals(analysisManager.getRegisteredAnalyses(), Collections.emptyList());
    }
}
