package io.beancounter.analyses;

import io.beancounter.commons.model.AnalysisResult;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class MockAnalyses implements Analyses {

    @Override
    public void store(AnalysisResult ar) throws AnalysesException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public AnalysisResult lookup(String analysesId) throws AnalysesException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
