package tv.notube.platform.responses;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.annotate.JsonTypeName;
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
//@Produces(MediaType.APPLICATION_JSON)
//@XmlRootElement
//@XmlAccessorType(XmlAccessType.PROPERTY)
public class PlatformResponseActivities extends PlatformResponse<List<Activity>> {

    private List<Activity> activities;

    public PlatformResponseActivities(){}

    public PlatformResponseActivities(Status s, String m) {
        super(s, m);
    }

    public PlatformResponseActivities(Status s, String m, List<Activity> act) {
        super(s, m);
        activities = act;
    }

    //@JsonProperty("activities")
    public List<Activity> getObject() {
        return activities;
    }

    public void setObject(List<Activity> act) {
        this.activities = act;
    }
}