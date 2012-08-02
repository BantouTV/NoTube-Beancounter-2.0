package tv.notube.activities.model.activity;

import tv.notube.commons.model.User;
import tv.notube.commons.model.activity.Activity;

import java.io.Serializable;
import java.util.UUID;

/**
 * A wrapper for the normal Activity which includes the User UUID and the {@link User}
 * related to the {@link Activity}. Objects of this class will be indexed in <i>Elastic Search</i>.
 *
 * @author Alex Cowell ( alxcwll@gmail.com )
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class ElasticSearchActivity implements Serializable {

    private static final long serialVersionUID = -8450986786361474378L;

    /**
     * User <i>beancounter.io</i> identifier
     */
    private UUID userId;

    /**
     * The activity to be indexed.
     */
    private Activity activity;

    /**
     * User data.
     */
    private User user;

    /**
     * needed by Jackson.
     */
    public ElasticSearchActivity() {}

    public ElasticSearchActivity(UUID userId, Activity activity, User user) {
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
        return "ElasticSearchActivity{" +
                "userId=" + userId +
                ", activity=" + activity +
                ", user=" + user +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (!(o instanceof ElasticSearchActivity)) return false;

        ElasticSearchActivity esa = (ElasticSearchActivity) o;

        return !(userId != null ? !userId.equals(esa.userId) : esa.userId != null)
                && !(activity != null ? !activity.equals(esa.activity) : esa.activity != null);

    }

    @Override
    public int hashCode() {
        int result = userId != null ? userId.hashCode() : 0;
        result = 31 * result + (activity != null ? activity.hashCode() : 0);
        return result;
    }
}
