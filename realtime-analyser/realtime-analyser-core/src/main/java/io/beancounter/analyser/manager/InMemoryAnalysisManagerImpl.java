package io.beancounter.analyser.manager;

import io.beancounter.analyser.analysis.Analysis;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;
import java.util.UUID;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class InMemoryAnalysisManagerImpl implements AnalysisManager {

    private Collection<Analysis> analyses;

    public InMemoryAnalysisManagerImpl(Properties properties) {
        analyses = new ArrayList<Analysis>();

        String definedAnalyses = properties.getProperty("analyser.analyses");

        if (definedAnalyses != null) {
            String[] analysisKeys = definedAnalyses.split(",");
            for (String analysisKey : analysisKeys) {
                String name = properties.getProperty("analyser.analysis." + analysisKey + ".name");
                if (name == null || name.isEmpty()) {
                    throw new IllegalArgumentException("Name must exist");
                }
                String description = properties.getProperty("analyser.analysis." + analysisKey + ".description");
                if (description == null) {
                    throw new IllegalArgumentException("Description must exist");
                }
                String className = properties.getProperty("analyser.analysis." + analysisKey + ".class");
                if (className == null) {
                    throw new IllegalArgumentException("Class name must exist");
                }

                try {
                    Class<?> analysisClass = Class.forName(className);
                    Constructor<?> ctor = analysisClass.getConstructor();
                    Analysis analysis = (Analysis) ctor.newInstance();
                    analysis.setName(name);
                    analysis.setDescription(description);
                    analyses.add(analysis);
                } catch (ClassNotFoundException cfne) {
                    throw new IllegalArgumentException("Class not found.", cfne);
                } catch (ClassCastException cce) {
                    throw new IllegalArgumentException("Class not type of analysis.", cce);
                } catch (Exception ignore) {}
            }
        }
    }

    public String register(String name, String description) throws AnalysisManagerException {
        throw new UnsupportedOperationException("niy");
    }

    public Analysis get(UUID uuid) throws AnalysisManagerException {
        throw new UnsupportedOperationException("niy");
    }

    public void delete(UUID uuid) throws AnalysisManagerException {
        throw new UnsupportedOperationException("niy");
    }

    public void start(UUID uuid) throws AnalysisManagerException {
        throw new UnsupportedOperationException("niy");
    }

    public void stop(UUID uuid) throws AnalysisManagerException {
        throw new UnsupportedOperationException("niy");
    }

    public Collection<Analysis> getRegisteredAnalyses() throws AnalysisManagerException {
        return analyses;
    }
}
