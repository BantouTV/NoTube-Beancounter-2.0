package tv.notube.commons.model.activity.rai;

import java.lang.Object;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

/**
 * This class models a social TV event as defined by <i>rai.tv</i>.
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class TVEvent extends tv.notube.commons.model.activity.Object {

    private final static String BASE_URL =
            "http://www.rai.tv/dl/RaiTV/programmi/media/EventItem-";

    private UUID id;

    public TVEvent() {
        super();
    }

    public TVEvent(UUID id, String name, String description) throws MalformedURLException {
        super(new URL(BASE_URL + id + ".html"));
        this.id = id;
        setName(name);
        setDescription(description);
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        TVEvent that = (TVEvent) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (id != null ? id.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "TVEvent{" +
                "id=" + id +
                '}';
    }

}
