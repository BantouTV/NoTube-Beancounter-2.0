package io.beancounter.commons.model.notifies;

import org.joda.time.DateTime;

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

    @Override
    public String toString() {
        return "Notify{" +
                "action='" + action + '\'' +
                ", object='" + object + '\'' +
                ", value='" + value + '\'' +
                ", dateTime=" + dateTime +
                '}';
    }
}
