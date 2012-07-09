package tv.notube.listener.facebook.model;

import org.codehaus.jackson.annotate.JsonProperty;

import javax.xml.bind.annotation.XmlElement;
import java.util.List;

/**
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
public class FacebookChange {

    private String uid;

    @JsonProperty("changed_fields")
    private List<String> changedFields;

    private long time;

    public FacebookChange() {}

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public List<String> getChangedFields() {
        return changedFields;
    }

    public void setChangedFields(List<String> changedFields) {
        this.changedFields = changedFields;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return "tv.notube.listener.facebook.model.FacebookChange{" +
                "uid='" + uid + '\'' +
                ", changedFields=" + changedFields +
                ", time=" + time +
                '}';
    }
}