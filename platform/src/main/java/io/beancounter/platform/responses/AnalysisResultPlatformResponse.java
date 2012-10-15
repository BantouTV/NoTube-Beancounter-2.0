package io.beancounter.platform.responses;

import io.beancounter.commons.model.AnalysisResult;
import io.beancounter.platform.PlatformResponse;

/**
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
public class AnalysisResultPlatformResponse extends PlatformResponse<AnalysisResult> {

    private AnalysisResult analysisResult;

    public AnalysisResultPlatformResponse(){}

    public AnalysisResultPlatformResponse(Status s, String m) {
        super(s, m);
    }

    public AnalysisResultPlatformResponse(Status s, String m, AnalysisResult analysisResult) {
        super(s, m);
        this.analysisResult = analysisResult;
    }

    public AnalysisResult getObject() {
        return analysisResult;
    }

    public void setObject(AnalysisResult analysisResult) {
        this.analysisResult = analysisResult;
    }
}