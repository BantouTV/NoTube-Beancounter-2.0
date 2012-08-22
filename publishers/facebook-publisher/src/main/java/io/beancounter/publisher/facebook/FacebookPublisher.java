package io.beancounter.publisher.facebook;

import com.google.inject.Inject;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Parameter;
import com.restfb.types.FacebookType;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.beancounter.commons.model.activity.Activity;
import io.beancounter.commons.model.activity.ResolvedActivity;

public class FacebookPublisher implements Processor {

    private static final Logger LOG = LoggerFactory.getLogger(FacebookPublisher.class);

    @Override
    public void process(Exchange exchange) throws Exception {
        ResolvedActivity resolvedActivity = exchange.getIn().getBody(ResolvedActivity.class);
        Activity activity = resolvedActivity.getActivity();

        // Get FB user access token.
        String token = resolvedActivity.getUser().getServices().get("facebook").getSession();

        // Publish activity on user's FB feed. Should be configurable.
        FacebookType response = publishActivity(token, activity.getObject().getUrl().toString());

        LOG.debug("Published message ID: " + response.getId());
    }

    FacebookType publishActivity(String token, String message) {
        FacebookClient client = new DefaultFacebookClient(token);
        return client.publish(
                "me/feed",
                FacebookType.class,
                Parameter.with("message", message)
        );
    }
}
