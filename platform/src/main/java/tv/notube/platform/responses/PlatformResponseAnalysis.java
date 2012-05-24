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
 *
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
@Produces(MediaType.APPLICATION_JSON)
@XmlRootElement
@XmlAccessorType(XmlAccessType.PROPERTY)
public class PlatformResponseAnalysis implements PlatformResponse<AnalysisDescription> {

    public enum Status {
        OK,
        NOK
    }

    @Expose
    private Status status;

    @Expose
    private String message;

    @Expose
    private AnalysisDescription analysisDescription;

    public PlatformResponseAnalysis(){}

    public PlatformResponseAnalysis(Status s, String m) {
        status = s;
        message = m;
    }

    public PlatformResponseAnalysis(Status s, String m, AnalysisDescription ad) {
        status = s;
        message = m;
        analysisDescription = ad;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public AnalysisDescription getObject() {
        return analysisDescription;
    }

    public void setObject(AnalysisDescription ad) {
        this.analysisDescription = ad;
    }
}