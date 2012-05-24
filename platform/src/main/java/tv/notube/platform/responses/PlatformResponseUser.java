package tv.notube.platform.responses;

import com.google.gson.annotations.Expose;
import tv.notube.commons.model.User;
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
public class PlatformResponseUser implements PlatformResponse<User> {

    public enum Status {
        OK,
        NOK
    }

    @Expose
    private Status status;

    @Expose
    private String message;

    @Expose
    private User user;

    public PlatformResponseUser(){}

    public PlatformResponseUser(Status s, String m) {
        status = s;
        message = m;
    }

    public PlatformResponseUser(Status s, String m, User user) {
        status = s;
        message = m;
        user = user;
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

    public User getObject() {
        return user;
    }

    public void setObject(User user) {
        this.user = user;
    }
}