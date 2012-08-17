package io.beancounter.platform.responses;

import io.beancounter.commons.model.User;
import io.beancounter.platform.PlatformResponse;

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

    public void setObject(User user) {
        this.user = user;
    }

}