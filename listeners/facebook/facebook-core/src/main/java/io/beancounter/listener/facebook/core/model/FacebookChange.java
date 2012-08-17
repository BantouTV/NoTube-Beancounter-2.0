package io.beancounter.listener.facebook.core.model;

import org.codehaus.jackson.annotate.JsonProperty;

import java.util.List;

/**
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
public class FacebookChange {

    private String uid;

    private String id;

    @JsonProperty("changed_fields")
    private List<String> changedFields;

    private long time;

    public FacebookChange() {
    }

    public String getUid() {
        return uid;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
        return "FacebookChange{" +
                "uid='" + uid + '\'' +
                ", id='" + id + '\'' +
                ", changedFields=" + changedFields +
                ", time=" + time +
                '}';
    }
}