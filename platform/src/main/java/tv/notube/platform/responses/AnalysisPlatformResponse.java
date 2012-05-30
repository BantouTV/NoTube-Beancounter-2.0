package tv.notube.platform.responses;

import tv.notube.commons.configuration.analytics.AnalysisDescription;
import tv.notube.platform.PlatformResponse;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
@XmlRootElement
public class AnalysisPlatformResponse extends PlatformResponse<AnalysisDescription> {

    private AnalysisDescription analysisDescription;

    public AnalysisPlatformResponse(Status status, String message, AnalysisDescription analysisDescription) {
        super(status, message);
        this.analysisDescription = analysisDescription;
    }

    @XmlElement
    public AnalysisDescription getObject() {
        return analysisDescription;
    }

}