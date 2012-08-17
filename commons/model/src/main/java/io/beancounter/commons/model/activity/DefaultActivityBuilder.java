package io.beancounter.commons.model.activity;

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
            if(fields.get(methodName) == null) {
                continue;
            }
            Method method;
            try {
                method = clazz.getMethod(
                        methodName,
                        fields.get(methodName).getClass()
                );
            } catch (NoSuchMethodException e) {
                throw new ActivityBuilderException(
                        "Method '" + methodName + "' not found",
                        e
                );
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

    public void setContext(DateTime dateTime, String service, String username)
            throws ActivityBuilderException {
        if (activity != null) {
            Context c = new Context();
            c.setDate(dateTime);
            c.setService(service);
            c.setUsername(username);
            activity.setContext(c);
            return;
        }
        throw new IllegalStateException("Did you invoke push?");
    }

    public void objectSetField(String method, java.lang.Object object, Class clazz)
            throws ActivityBuilderException {
        if(activity == null) {
            throw new IllegalStateException("Did you invoke push?");
        }
        Method adder;
        try {
            adder = activity.getObject().getClass().getDeclaredMethod(
                    method,
                    clazz
            );
        } catch (NoSuchMethodException e) {
            throw new ActivityBuilderException("", e);
        }
        try {
            adder.invoke(activity.getObject(), object);
        } catch (IllegalAccessException e) {
            throw new ActivityBuilderException("", e);
        } catch (InvocationTargetException e) {
            throw new ActivityBuilderException("", e);
        }
    }
}
