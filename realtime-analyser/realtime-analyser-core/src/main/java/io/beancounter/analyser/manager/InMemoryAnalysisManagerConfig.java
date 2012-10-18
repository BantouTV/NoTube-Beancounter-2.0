package io.beancounter.analyser.manager;

import io.beancounter.analyser.analysis.Analysis;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

public class InMemoryAnalysisManagerConfig {

    private static final Logger LOG = LoggerFactory.getLogger(InMemoryAnalysisManagerConfig.class);

    private Collection<Analysis> analyses = new ArrayList<Analysis>();

    public static InMemoryAnalysisManagerConfig build(Properties properties) {
        InMemoryAnalysisManagerConfig config = new InMemoryAnalysisManagerConfig();

        if (hasAnalysisConfigurations(properties)) {
            loadConfiguration(config, properties);
            LOG.info("{} analyses have been loaded.", config.getAnalyses().size());
        } else {
            LOG.info("No analyses were defined or configured.");
        }

        return config;
    }

    public void addAnalysis(Analysis analysis) {
        analyses.add(analysis);
    }

    public Collection<Analysis> getAnalyses() {
        return analyses;
    }

    private static boolean hasAnalysisConfigurations(Properties properties) {
        String definedAnalyses = properties.getProperty("analyser.analyses");
        return definedAnalyses != null
                && !definedAnalyses.isEmpty()
                && definedAnalyses.split(",").length > 0;
    }

    private static void loadConfiguration(InMemoryAnalysisManagerConfig config, Properties properties) {
        String definedAnalyses = properties.getProperty("analyser.analyses");
        String[] analysisKeys = definedAnalyses.split(",");

        for (String analysisKey : analysisKeys) {
            String name = getProperty(properties, analysisKey, "name");
            String description = getProperty(properties, analysisKey, "description");
            String className = getProperty(properties, analysisKey, "class");

            config.addAnalysis(createAnalysis(name, description, className));
        }
    }

    private static String getProperty(
            Properties properties,
            String analysisKey,
            String property
    ) {
        String value = properties.getProperty("analyser.analysis." + analysisKey + "." + property);
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("Analysis [" + analysisKey + "] must have a " + property);
        }
        return value;
    }

    private static Analysis createAnalysis(String name, String description, String className) {
        Analysis analysis;

        try {
            Class<?> analysisClass = Class.forName(className);
            Constructor<?> ctor = analysisClass.getConstructor();
            analysis = (Analysis) ctor.newInstance();
            analysis.setName(name);
            analysis.setDescription(description);
        } catch (ClassNotFoundException cfne) {
            throw new IllegalArgumentException("Class [" + className + "] not found.", cfne);
        } catch (ClassCastException cce) {
            throw new IllegalArgumentException("Class [" + className + "] not type of analysis.", cce);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Error loading analysis [" + name + "]", ex);
        }

        return analysis;
    }
}
