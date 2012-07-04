package tv.notube.commons.model.activity;

import java.lang.*;
import java.lang.Object;
import java.util.UUID;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class ResolvedActivity {

    private UUID userId;

    private Activity activity;

    public ResolvedActivity() {}

    public ResolvedActivity(UUID userId, Activity activity) {
        this.userId = userId;
        this.activity = activity;
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

    @Override
    public String toString() {
        return "ResolvedActivity{" +
                "userId=" + userId +
                ", activity=" + activity +
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
