package tv.notube.listener.facebook.model;

import tv.notube.listener.facebook.model.FacebookChange;

import java.util.List;

/**
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
public class FacebookNotification {

    private String object;

    private List<FacebookChange> entry;

    public FacebookNotification() {}

    public String getObject() {
        return object;
    }

    public void setObject(String object) {
        this.object = object;
    }

    public List<FacebookChange> getEntry() {
        return entry;
    }

    public void setEntry(List<FacebookChange> entry) {
        this.entry = entry;
    }

    @Override
    public String toString() {
        return "tv.notube.listener.facebook.model.FacebookNotification{" +
                "object='" + object + '\'' +
                ", entry=" + entry +
                '}';
    }
}