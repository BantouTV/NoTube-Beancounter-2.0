package tv.notube.platform.responses;

import tv.notube.commons.model.User;
import tv.notube.platform.PlatformResponse;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Defines the result of a processing.
 *
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
//@Produces(MediaType.APPLICATION_JSON)
//@XmlRootElement
//@XmlAccessorType(XmlAccessType.PROPERTY)
public class PlatformResponseUser extends PlatformResponse<User> {

    private User user;

    public PlatformResponseUser(){}

    public PlatformResponseUser(Status s, String m) {
        super(s, m);
    }

    public PlatformResponseUser(Status s, String m, User user) {
        super(s, m);
        this.user = user;
    }

    //@XmlElement
    public User getObject() {
        return user;
    }

    public void setObject(User user) {
        this.user = user;
    }
}