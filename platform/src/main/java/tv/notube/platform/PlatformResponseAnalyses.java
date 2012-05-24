package tv.notube.platform;

import com.google.gson.annotations.Expose;
import tv.notube.commons.configuration.analytics.AnalysisDescription;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;

/**
 * Defines the result of a processing.
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
@Produces(MediaType.APPLICATION_JSON)
@XmlRootElement
@XmlAccessorType(XmlAccessType.PROPERTY)
public class PlatformResponseAnalyses implements PlatformResponseI<AnalysisDescription[]> {

    public enum Status {
        OK,
        NOK
    }

    @Expose
    private Status status;

    @Expose
    private String message;

    @Expose
    private AnalysisDescription[] analysisDescriptions;

    public PlatformResponseAnalyses(){}

    public PlatformResponseAnalyses(Status s, String m) {
        status = s;
        message = m;
    }

    public PlatformResponseAnalyses(Status s, String m, AnalysisDescription[] ad) {
        status = s;
        message = m;
        analysisDescriptions = ad;
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

    public AnalysisDescription[] getObject() {
        return analysisDescriptions;
    }

    public void setObject(AnalysisDescription[] ad) {
        this.analysisDescriptions = ad;
    }
}