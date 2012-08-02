package tv.notube.commons.model.activity;

import tv.notube.commons.model.User;

import java.lang.*;
import java.lang.Object;
import java.util.UUID;

/**
 * This class wraps an {@link Activity} with the <i>beancounter.io</i> user
 * identifier.
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class ResolvedActivity {

    /**
     * The <i>beancounter.io</i> user identifier
     */
    private UUID userId;

    /**
     * The activity the user performed
     */
    private Activity activity;

    /**
     * The {@link tv.notube.commons.model.User} owner of this activity.
     */
    private User user;

    public ResolvedActivity() {}

    public ResolvedActivity(UUID userId, Activity activity, User user) {
        this.userId = userId;
        this.activity = activity;
        this.user = user;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "ResolvedActivity{" +
                "userId=" + userId +
                ", activity=" + activity +
                ", user=" + user +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ResolvedActivity that = (ResolvedActivity) o;

        if (activity != null ? !activity.equals(that.activity) : that.activity != null)
            return false;
        if (userId != null ? !userId.equals(that.userId) : that.userId != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = userId != null ? userId.hashCode() : 0;
        result = 31 * result + (activity != null ? activity.hashCode() : 0);
        return result;
    }
}
