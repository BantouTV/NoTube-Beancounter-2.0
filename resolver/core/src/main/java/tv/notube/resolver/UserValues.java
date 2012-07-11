package tv.notube.resolver;

import java.util.UUID;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class UserValues {

    public static UserValues parse(String text) {
        if(!text.contains(",")) {
            throw new IllegalArgumentException("provided text [" + text + "] is not well formed");
        }
        String[] pairs = text.split(",");
        if(pairs.length != 2) {
            throw new IllegalArgumentException("provided text [" + text + "] is not well formed");
        }
        return new UserValues(
                UUID.fromString(pairs[0]),
                pairs[1]
        );
    }

    public static String unparse(UserValues uv) {
        return uv.getUserId().toString() + ',' + uv.getUsername();
    }

    private UUID userId;

    private String username;

    public UserValues(UUID userId, String username) {
        this.userId = userId;
        this.username = username;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String toString() {
        return "UserValues{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                '}';
    }
}
