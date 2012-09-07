package io.beancounter.platform.responses;

import io.beancounter.commons.model.UserProfile;
import io.beancounter.platform.PlatformResponse;

/**
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
public class UserProfilePlatformResponse extends PlatformResponse<UserProfile> {

    private UserProfile userProfile;

    public UserProfilePlatformResponse() { }

    public UserProfilePlatformResponse(Status s, String m) {
        super(s, m);
    }

    public UserProfilePlatformResponse(Status s, String m, UserProfile up) {
        super(s, m);
        userProfile = up;
    }

    @Override
    public UserProfile getObject() {
        return userProfile;
    }

    public void setObject(UserProfile userProfile) {
        this.userProfile = userProfile;
    }
}