package tv.notube.platform;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This is the most generic response the REST platform is able to deliver.
 *
 * @author Enrico Candino ( enrico.candino@gmail.com )
 * @see {@link javax.xml.bind.annotation.XmlRootElement}
 */
@XmlRootElement
public abstract class PlatformResponse<T> {

    public enum Status {
        OK,
        NOK
    }

    private Status status;

    private String message;

    public PlatformResponse() {}

    public PlatformResponse(Status status, String message) {
        this.status = status;
        this.message = message;
    }

    public PlatformResponse(String message) {
        this(Status.OK, message);
    }

    @XmlElement
    public Status getStatus() {
        return status;
    }

    @XmlElement
    public String getMessage() {
        return message;
    }

    public abstract T getObject();

}
