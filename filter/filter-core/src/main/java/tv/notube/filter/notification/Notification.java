package tv.notube.filter.notification;

/**
 * This class is used to send notifications over Redis to
 * the Filter engine.
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class Notification {

    public enum Action {
        START,
        STOP
    }

    private Action action;

    private String id;

    public Notification() {}

    public Notification(Action action, String id) {
        this.action = action;
        this.id = id;
    }

    public Action getAction() {
        return action;
    }

    public String getId() {
        return id;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Notification{" +
                "action=" + action +
                ", id='" + id + '\'' +
                '}';
    }
}
