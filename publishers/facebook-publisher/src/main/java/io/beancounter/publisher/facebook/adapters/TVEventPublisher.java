package io.beancounter.publisher.facebook.adapters;

import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Parameter;
import com.restfb.types.FacebookType;
import io.beancounter.commons.model.activity.Verb;
import io.beancounter.commons.model.activity.rai.TVEvent;
import io.beancounter.publisher.facebook.FacebookPublisherException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Enrico Candino (( enrico.candino@gmail.com ))
 */
public class TVEventPublisher implements Publisher<TVEvent> {

    private static final Logger LOG = LoggerFactory.getLogger(TVEventPublisher.class);

    @Override
    public FacebookType publishActivity(String token, Verb verb, TVEvent tvEvent) throws FacebookPublisherException {
        FacebookClient client = new DefaultFacebookClient(token);
        return client.publish(
                "me/feed",
                FacebookType.class,
                Parameter.with("message", getMessage(verb, tvEvent)),
                Parameter.with("link", tvEvent.getUrl().toString())
        );
    }

    private String getMessage(Verb verb, TVEvent tvEvent) throws FacebookPublisherException {
        String message = "";
        if(verb.equals(Verb.CHECKIN)) {
            message += "Just joined the tv event ";
        } else {
            final String errMessage = "Verb [" + verb + "] not supported";
            LOG.error(errMessage);
            throw new FacebookPublisherException(errMessage, new UnsupportedOperationException());
        }
        message += tvEvent.getName();
        return message;
    }
}
