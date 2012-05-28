package tv.notube.platform.responses;

import com.google.gson.annotations.Expose;
import tv.notube.commons.configuration.analytics.AnalysisDescription;
import tv.notube.platform.PlatformResponse;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Defines the result of a processing.
 *
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
@Produces(MediaType.APPLICATION_JSON)
@XmlRootElement
@XmlAccessorType(XmlAccessType.PROPERTY)
public class PlatformResponseAnalysis extends PlatformResponse<AnalysisDescription> {

    private AnalysisDescription analysisDescription;

    public PlatformResponseAnalysis(){}

    public PlatformResponseAnalysis(Status s, String m) {
        super(s, m);
    }

    public PlatformResponseAnalysis(Status s, String m, AnalysisDescription ad) {
        super(s, m);
        analysisDescription = ad;
    }

    public AnalysisDescription getObject() {
        return analysisDescription;
    }
}