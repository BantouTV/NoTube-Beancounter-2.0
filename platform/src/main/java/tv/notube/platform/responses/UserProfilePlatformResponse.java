package tv.notube.platform.responses;

import tv.notube.commons.model.UserProfile;
import tv.notube.platform.PlatformResponse;

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
}