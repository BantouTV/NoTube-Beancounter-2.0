package io.beancounter.publisher.facebook.adapters;

import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Parameter;
import com.restfb.types.FacebookType;
import io.beancounter.commons.model.activity.Object;
import io.beancounter.commons.model.activity.Verb;

/**
 *
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
public class ObjectPublisher implements Publisher<Object> {

    @Override
    public FacebookType publishActivity(String token, Verb verb, Object object) {
        FacebookClient client = new DefaultFacebookClient(token);
        return client.publish(
                "me/feed",
                FacebookType.class,
                Parameter.with("message", object.getDescription()),
                Parameter.with("link", object.getUrl().toString())
        );
    }

}
