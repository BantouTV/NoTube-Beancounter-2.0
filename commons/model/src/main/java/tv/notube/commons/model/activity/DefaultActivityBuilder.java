package tv.notube.commons.model.activity;

import org.joda.time.DateTime;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;

/**
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class DefaultActivityBuilder implements ActivityBuilder {

    private Activity activity;

    public Activity pop() throws ActivityBuilderException {
        if (activity != null) {
            try {
                return activity;
            } finally {
                activity = null;
            }
        }
        throw new IllegalStateException("Did you invoke push?");
    }

    public void push() throws ActivityBuilderException {
        if (activity == null) {
            activity = new Activity();
            return;
        }
        throw new IllegalStateException("it seems that there already is an " +
                "activity under construction");
    }

    public void setVerb(Verb verb) throws ActivityBuilderException {
        if (activity != null) {
            activity.setVerb(verb);
            return;
        }
        throw new IllegalStateException("Did you invoke push?");
    }

    public void setVerb(String verb) throws ActivityBuilderException {
        Verb v = Verb.valueOf(verb);
        setVerb(v);
    }

    public void setObject(
            Class<? extends Object> clazz,
            URL url,
            String name,
            java.util.Map<String, java.lang.Object> fields
    ) throws ActivityBuilderException {
        if(activity == null) {
            throw new IllegalStateException("Did you invoke push?");
        }
        Object obj;
        try {
            obj = clazz.newInstance();
        } catch (InstantiationException e) {
            throw new ActivityBuilderException("", e);
        } catch (IllegalAccessException e) {
            throw new ActivityBuilderException("", e);
        }
        obj.setUrl(url);
        obj.setName(name);
        for(String methodName : fields.keySet()) {
            Method method;
            try {
                method = clazz.getDeclaredMethod(
                        methodName,
                        fields.get(methodName).getClass()
                );
            } catch (NoSuchMethodException e) {
                throw new ActivityBuilderException("", e);
            }
            try {
                method.invoke(obj, fields.get(methodName));
            } catch (IllegalAccessException e) {
                throw new ActivityBuilderException("", e);
            } catch (InvocationTargetException e) {
                throw new ActivityBuilderException("", e);
            }
        }
        activity.setObject(obj);
    }

    public void setContext(DateTime dateTime, URL service) throws ActivityBuilderException {
        if (activity != null) {
            Context c = new Context();
            c.setDate(dateTime);
            c.setService(service);
            activity.setContext(c);
            return;
        }
        throw new IllegalStateException("Did you invoke push?");
    }
}
