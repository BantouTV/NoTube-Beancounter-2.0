package io.beancounter.publisher.facebook.adapters;

import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Parameter;
import com.restfb.types.FacebookType;
import io.beancounter.commons.model.activity.Verb;
import io.beancounter.commons.model.activity.rai.TVEvent;

/**
 *
 * @author Enrico Candino ( enrico.candino @ gmail.com )
 */
public class TVEventPublisher implements Publisher<TVEvent> {

    @Override
    public FacebookType publishActivity(String token, Verb verb, TVEvent tvEvent) {
        FacebookClient client = new DefaultFacebookClient(token);
        return client.publish(
                "me/feed",
                FacebookType.class,
                Parameter.with("message", getMessage(verb, tvEvent)),
                Parameter.with("link", tvEvent.getUrl().toString())
        );
    }

    private String getMessage(Verb verb, TVEvent tvEvent) {
        String message = "";
        if(verb.equals(Verb.WATCHED)) {
            message += "Just watched at ";
        } else if(verb.equals(Verb.CHECKIN)) {
            message += "Just joined the tv event ";
        }
        message += tvEvent.getName();
        return message;
    }
}
