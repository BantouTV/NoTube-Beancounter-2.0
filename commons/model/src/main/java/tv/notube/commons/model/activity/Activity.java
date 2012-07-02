package tv.notube.commons.model.activity;

import tv.notube.commons.tests.annotations.Random;

import java.io.Serializable;
import java.util.UUID;

/**
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class Activity implements Serializable {

    private static final long serialVersionUID = 68843445235L;

    private UUID id;

    private Verb verb;

    private tv.notube.commons.model.activity.Object object;

    private Context context;

    public Activity() {
        id = UUID.randomUUID();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    @Random(names = { "v", "obj", "c"} )
    public Activity(Verb v, Object obj, Context c) {
        verb = v;
        object = obj;
        context = c;
    }

    public Verb getVerb() {
        return verb;
    }

    public void setVerb(Verb verb) {
        this.verb = verb;
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    @Override
    public String toString() {
        return "Activity{" +
                "id=" + id +
                ", verb=" + verb +
                ", object=" + object +
                ", context=" + context +
                '}';
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) return true;
        if (!(o instanceof Activity)) return false;

        Activity activity = (Activity) o;

        if (context != null ? !context.equals(activity.context) : activity.context != null) return false;
        if (object != null ? !object.equals(activity.object) : activity.object != null) return false;
        if (verb != activity.verb) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = verb != null ? verb.hashCode() : 0;
        result = 31 * result + (object != null ? object.hashCode() : 0);
        result = 31 * result + (context != null ? context.hashCode() : 0);
        return result;
    }
}
