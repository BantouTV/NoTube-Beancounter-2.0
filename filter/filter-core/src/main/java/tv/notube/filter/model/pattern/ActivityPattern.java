package tv.notube.filter.model.pattern;

import tv.notube.commons.model.activity.Activity;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class ActivityPattern implements Pattern<Activity> {

    public final static ActivityPattern ANY = new ActivityPattern(
            UUIDPattern.ANY,
            VerbPattern.ANY,
            ObjectPattern.ANY,
            ContextPattern.ANY
    );

    private UUIDPattern userId;

    private VerbPattern verb;

    private ObjectPattern object;

    private ContextPattern context;

    public ActivityPattern() {}

    public ActivityPattern(
            UUIDPattern uuidId,
            VerbPattern verb,
            ObjectPattern object,
            ContextPattern context
    ) {
        this.userId = uuidId;
        this.verb = verb;
        this.object = object;
        this.context = context;
    }

    public VerbPattern getVerb() {
        return verb;
    }

    public void setVerb(VerbPattern verb) {
        this.verb = verb;
    }

    public ContextPattern getContext() {
        return context;
    }

    public void setContext(ContextPattern context) {
        this.context = context;
    }

    public UUIDPattern getUserId() {
        return userId;
    }

    public void setUserId(UUIDPattern userId) {
        this.userId = userId;
    }

    public ObjectPattern getObject() {
        return object;
    }

    public void setObject(ObjectPattern object) {
        this.object = object;
    }

    @Override
    public boolean matches(Activity activity) {
        return (this.equals(ANY)) || (userId.matches(activity.getId()) &&
                verb.matches(activity.getVerb()) &&
                context.matches(activity.getContext()));
    }

    @Override
    public String toString() {
        return "ActivityPattern{" +
                "userId=" + userId +
                ", verb=" + verb +
                ", object=" + object +
                ", context=" + context +
                '}';
    }
}
