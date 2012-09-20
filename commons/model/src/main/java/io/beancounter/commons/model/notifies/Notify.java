package io.beancounter.commons.model.notifies;

import org.joda.time.DateTime;

import java.util.HashMap;
import java.util.Map;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class Notify {

    private String action;

    private String object;

    private String value;

    private DateTime dateTime;

    private Map<String, String> metadata = new HashMap<String, String>();

    public Notify() {}

    public Notify(String action, String object, String value) {
        this.action = action;
        this.object = object;
        this.value = value;
        this.dateTime = DateTime.now();
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getObject() {
        return object;
    }

    public void setObject(String object) {
        this.object = object;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public DateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(DateTime dateTime) {
        this.dateTime = dateTime;
    }

    public void addMetadata(String k, String v) {
        metadata.put(k, v);
    }

    public String getMetadataValue(String k) {
        return metadata.get(k);
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    @Override
    public String toString() {
        return "Notify{" +
                "action='" + action + '\'' +
                ", object='" + object + '\'' +
                ", value='" + value + '\'' +
                ", dateTime=" + dateTime +
                ", metadata=" + metadata +
                '}';
    }
}
