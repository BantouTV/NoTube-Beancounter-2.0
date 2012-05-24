package tv.notube.platform.responses;

import com.google.gson.annotations.Expose;
import tv.notube.platform.PlatformResponse;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
@Produces(MediaType.APPLICATION_JSON)
@XmlRootElement
@XmlAccessorType(XmlAccessType.PROPERTY)
public class PlatformResponseString implements PlatformResponse<String> {

    public enum Status {
        OK,
        NOK
    }

    @Expose
    private Status status;

    @Expose
    private String message;

    @Expose
    private String string;

    public PlatformResponseString(){}

    public PlatformResponseString(Status s, String m) {
        status = s;
        message = m;
    }

    public PlatformResponseString(Status s, String m, String str) {
        status = s;
        message = m;
        string = str;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getObject() {
        return string;
    }

    public void setObject(String str) {
        this.string = str;
    }

}