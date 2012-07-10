package tv.notube.commons.model.activity;

import org.joda.time.DateTime;

import java.net.URL;

/**
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public interface ActivityBuilder {

    public Activity pop() throws ActivityBuilderException;

    public void push() throws ActivityBuilderException;

    public void setVerb(Verb verb) throws ActivityBuilderException;

    public void setVerb(String verb) throws ActivityBuilderException;

    public void setObject(
            Class<? extends Object> obj,
            URL url,
            String name,
            java.util.Map<String, java.lang.Object> fields
    )  throws ActivityBuilderException;

    public void setContext(DateTime dateTime, String service, String username)
            throws ActivityBuilderException;

    public void objectSetField(
            String method,
            java.lang.Object object,
            Class clazz
    ) throws ActivityBuilderException;
}
