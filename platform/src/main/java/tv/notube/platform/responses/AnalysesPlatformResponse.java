package tv.notube.platform.responses;

import tv.notube.commons.configuration.analytics.AnalysisDescription;
import tv.notube.platform.PlatformResponse;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
@XmlRootElement
public class AnalysesPlatformResponse extends PlatformResponse<AnalysisDescription[]> {

    private AnalysisDescription[] analysisDescriptions;

    public AnalysesPlatformResponse() {}

    public AnalysesPlatformResponse(
            Status status,
            String message,
            AnalysisDescription[] analysisDescriptions
    ) {
        super(status, message);
        this.analysisDescriptions = analysisDescriptions;
    }

    public AnalysesPlatformResponse(Status status, String message) {
        super(status, message);
    }

    @XmlElement
    public AnalysisDescription[] getObject() {
        return analysisDescriptions;
    }

}