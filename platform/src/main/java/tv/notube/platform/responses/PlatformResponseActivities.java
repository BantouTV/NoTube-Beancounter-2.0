package tv.notube.platform.responses;

import com.google.gson.annotations.Expose;
import tv.notube.commons.configuration.analytics.AnalysisDescription;
import tv.notube.commons.model.activity.Activity;
import tv.notube.platform.PlatformResponse;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Defines the result of a processing.
 *
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
@Produces(MediaType.APPLICATION_JSON)
@XmlRootElement
@XmlAccessorType(XmlAccessType.PROPERTY)
public class PlatformResponseActivities implements PlatformResponse<List<Activity>> {

    public enum Status {
        OK,
        NOK
    }

    private Status status;

    private String message;

    private List<Activity> activities;

    public PlatformResponseActivities(){}

    public PlatformResponseActivities(Status s, String m) {
        status = s;
        message = m;
    }

    public PlatformResponseActivities(Status s, String m, List<Activity> act) {
        status = s;
        message = m;
        activities = act;
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

    @XmlElement
    public List<Activity> getObject() {
        return activities;
    }

    public void setObject(List<Activity> act) {
        this.activities = act;
    }
}