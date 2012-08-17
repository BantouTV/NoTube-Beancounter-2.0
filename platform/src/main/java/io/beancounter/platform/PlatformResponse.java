package io.beancounter.platform;

/**
 * This is the most generic response the REST platform is able to deliver.
 *
 * @author Enrico Candino ( enrico.candino@gmail.com )
 * @see {@link javax.xml.bind.annotation.XmlRootElement}
 */
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

    public Status getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public abstract T getObject();

}
