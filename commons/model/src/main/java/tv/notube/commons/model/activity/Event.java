package tv.notube.commons.model.activity;

import org.joda.time.DateTime;

/**
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class Event extends tv.notube.commons.model.activity.Object {

    private DateTime start;

    private DateTime end;

    private String summary;

    public DateTime getStart() {
        return start;
    }

    public void setStart(DateTime start) {
        this.start = start;
    }

    public DateTime getEnd() {
        return end;
    }

    public void setEnd(DateTime end) {
        this.end = end;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    @Override
    public String toString() {
        return "Event{" +
                "start=" + start +
                ", end=" + end +
                ", summary='" + summary + '\'' +
                '}';
    }
}
