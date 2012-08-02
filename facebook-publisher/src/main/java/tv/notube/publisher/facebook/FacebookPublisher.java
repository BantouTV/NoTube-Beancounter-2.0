package tv.notube.publisher.facebook;

import com.google.inject.Inject;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Parameter;
import com.restfb.types.FacebookType;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tv.notube.commons.model.activity.Activity;
import tv.notube.commons.model.activity.ResolvedActivity;
import tv.notube.resolver.Resolver;
import tv.notube.usermanager.UserManager;

public class FacebookPublisher implements Processor {

    private static final Logger LOG = LoggerFactory.getLogger(FacebookPublisher.class);

    @Inject
    private Resolver resolver;

    @Inject
    private UserManager userManager;

    @Override
    public void process(Exchange exchange) throws Exception {
        ResolvedActivity resolvedActivity = exchange.getIn().getBody(ResolvedActivity.class);
        Activity activity = resolvedActivity.getActivity();

        // Get FB user access token.
        String service = activity.getContext().getService();
        String beancounterUsername = resolver.resolveUsername(
                activity.getContext().getUsername(),
                service
        );

        String token = userManager.getUser(beancounterUsername)
                .getServices().get(service).getSession();

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
