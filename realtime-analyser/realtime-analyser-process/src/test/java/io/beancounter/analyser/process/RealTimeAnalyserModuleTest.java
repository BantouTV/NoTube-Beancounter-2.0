package io.beancounter.analyser.process;

import com.google.inject.Guice;
import com.google.inject.Injector;
import io.beancounter.analyser.manager.InMemoryAnalysisManagerConfig;
import org.testng.annotations.Test;

public class RealTimeAnalyserModuleTest {

    @Test
    public void dependenciesAreConfiguredCorrectly() throws Exception {
        Injector injector = Guice.createInjector(new StubRealTimeAnalyserModule());

        injector.getInstance(RealTimeAnalyserRoute.class);
    }

    private static class StubRealTimeAnalyserModule extends RealTimeAnalyserModule {

        @Override
        InMemoryAnalysisManagerConfig analysisManagerConfig() {
            return new InMemoryAnalysisManagerConfig();
        }
    }
}
