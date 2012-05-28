package tv.notube.platform.responses;

import tv.notube.commons.model.User;
import tv.notube.platform.PlatformResponse;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
@XmlRootElement
public class UserPlatformResponse extends PlatformResponse<User> {

    private User user;

    public UserPlatformResponse() {}

    public UserPlatformResponse(Status status, String message, User user) {
        super(status, message);
        this.user = user;
    }

    public UserPlatformResponse(Status status, String message) {
        super(status, message);
    }

    @XmlElement
    public User getObject() {
        return user;
    }

}