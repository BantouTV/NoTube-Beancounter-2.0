package io.beancounter.listener.facebook.core;

import com.restfb.Connection;
import com.restfb.FacebookClient;
import com.restfb.Parameter;
import com.restfb.exception.FacebookOAuthException;
import com.restfb.types.Post;
import io.beancounter.commons.model.activity.Verb;
import io.beancounter.listener.facebook.core.model.FacebookData;

import java.util.ArrayList;
import java.util.Collection;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class FacebookUtils {

    public static Class<?> getFacebookClass(String field) {
        // TODO (high) (put this in the properties and make it real)
        if (!field.equals("feed")) {
            return FacebookData.class;
        }
        return Post.class;
    }

    public static <T> Collection<T> fetch(
            Class<? extends T> clazz,
            FacebookClient client,
            String field,
            int limit
    ) throws FacebookOAuthException {
        Connection<T> connection = (Connection<T>) client.fetchConnection(
                "me/" + field,
                clazz,
                Parameter.with("limit", limit)
        );
        Collection<T> result = new ArrayList<T>();
        for (T t : connection.getData()) {
            result.add(t);
        }
        return result;
    }

    public static Verb fromFieldToVerb(String field) {
        // TODO (high) (put this in the properties and make it real)
        if (!field.equals("feed")) {
            return Verb.LIKE;
        }
        return Verb.SHARE;
    }



}
