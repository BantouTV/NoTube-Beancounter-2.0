package io.beancounter.commons.model.activity.rai;

import java.lang.Object;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class Comment extends io.beancounter.commons.model.activity.Object {

    private String text;

    private UUID inReplyTo;

    private String onEvent;

    public Comment() {
        super();
    }

    public Comment(URL url, String text) throws MalformedURLException {
        super(url);
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public UUID getInReplyTo() {
        return inReplyTo;
    }

    public void setInReplyTo(UUID inReplyTo) {
        this.inReplyTo = inReplyTo;
    }

    public String getOnEvent() {
        return onEvent;
    }

    public void setOnEvent(String onEvent) {
        this.onEvent = onEvent;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Comment comment = (Comment) o;

        if (inReplyTo != null ? !inReplyTo.equals(comment.inReplyTo) : comment.inReplyTo != null)
            return false;
        if (text != null ? !text.equals(comment.text) : comment.text != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (text != null ? text.hashCode() : 0);
        result = 31 * result + (inReplyTo != null ? inReplyTo.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Comment{" +
                "text='" + text + '\'' +
                ", inReplyTo=" + inReplyTo +
                ", onEvent=" + onEvent +
                '}';
    }
}
