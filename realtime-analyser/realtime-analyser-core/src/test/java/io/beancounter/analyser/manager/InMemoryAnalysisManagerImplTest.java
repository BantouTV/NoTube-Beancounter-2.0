package io.beancounter.analyser.manager;

import io.beancounter.analyser.analysis.Analysis;
import io.beancounter.commons.helper.PropertiesHelper;
import org.testng.annotations.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Properties;

import static org.testng.Assert.assertEquals;

public class InMemoryAnalysisManagerImplTest {

    private AnalysisManager analysisManager;

    @Test
    public void givenEmptyPropertiesFileWhenInitializingThenLoadZeroAnalyses() throws Exception {
        Properties properties = PropertiesHelper.readFromClasspath("/empty-analyser.properties");
        analysisManager = new InMemoryAnalysisManagerImpl(properties);

        assertEquals(analysisManager.getRegisteredAnalyses(), Collections.emptyList());
    }

    @Test
    public void givenPropertiesFileWithOneAnalysisWhenInitializingThenLoadOneAnalyses() throws Exception {
        Properties properties = PropertiesHelper.readFromClasspath("/1-analysis-analyser.properties");
        analysisManager = new InMemoryAnalysisManagerImpl(properties);

        Collection<Analysis> registeredAnalyses = analysisManager.getRegisteredAnalyses();
        assertEquals(registeredAnalyses.size(), 1);

        Analysis analysis = registeredAnalyses.iterator().next();
        assertEquals(analysis.getName(), "top-5-trending-interests");
        assertEquals(analysis.getDescription(), "Top 5 Trending Interests");
        assertEquals(analysis.getClass(), Class.forName("io.beancounter.analyser.examples.TrendingInterestsAnalysis"));
    }

    @Test
    public void givenPropertiesFileWithTwoAnalysesWhenInitializingThenLoadTwoAnalyses() throws Exception {
        Properties properties = PropertiesHelper.readFromClasspath("/2-analysis-analyser.properties");
        analysisManager = new InMemoryAnalysisManagerImpl(properties);

        Collection<Analysis> registeredAnalyses = analysisManager.getRegisteredAnalyses();
        assertEquals(registeredAnalyses.size(), 2);

        Iterator<Analysis> iterator = registeredAnalyses.iterator();
        Analysis analysis = iterator.next();
        assertEquals(analysis.getName(), "top-5-trending-interests");
        assertEquals(analysis.getDescription(), "Top 5 Trending Interests");
        assertEquals(analysis.getClass(), Class.forName("io.beancounter.analyser.examples.TrendingInterestsAnalysis"));

        analysis = iterator.next();
        assertEquals(analysis.getName(), "top-3-trending-interests");
        assertEquals(analysis.getDescription(), "Top 3 Trending Interests");
        assertEquals(analysis.getClass(), Class.forName("io.beancounter.analyser.examples.TrendingInterestsAnalysis"));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void givenPropertiesFileWithDeclaredAnalysisButMissingNameWhenInitializingThenThrowException() throws Exception {
        Properties properties = PropertiesHelper.readFromClasspath("/missing-name-analyser.properties");
        analysisManager = new InMemoryAnalysisManagerImpl(properties);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void givenPropertiesFileWithDeclaredAnalysisButMissingDescriptionWhenInitializingThenThrowException() throws Exception {
        Properties properties = PropertiesHelper.readFromClasspath("/missing-desc-analyser.properties");
        analysisManager = new InMemoryAnalysisManagerImpl(properties);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void givenPropertiesFileWithDeclaredAnalysisButMissingClassWhenInitializingThenThrowException() throws Exception {
        Properties properties = PropertiesHelper.readFromClasspath("/missing-class-analyser.properties");
        analysisManager = new InMemoryAnalysisManagerImpl(properties);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void givenPropertiesFileWithClassThatDoesNotExistWhenInitializingThenThrowException() throws Exception {
        Properties properties = PropertiesHelper.readFromClasspath("/non-existent-class-analyser.properties");
        analysisManager = new InMemoryAnalysisManagerImpl(properties);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void givenPropertiesFileWithClassThatIsNotAnAnalysisWhenInitializingThenThrowException() throws Exception {
        Properties properties = PropertiesHelper.readFromClasspath("/not-analysis-analyser.properties");
        analysisManager = new InMemoryAnalysisManagerImpl(properties);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void givenPropertiesFileWithEmptyAnalysisNameWhenInitializingThenThrowException() throws Exception {
        Properties properties = PropertiesHelper.readFromClasspath("/empty-name-analyser.properties");
        analysisManager = new InMemoryAnalysisManagerImpl(properties);
    }
}
