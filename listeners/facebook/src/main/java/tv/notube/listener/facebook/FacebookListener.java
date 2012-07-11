package tv.notube.listener.facebook;

import com.google.inject.Inject;
import com.restfb.Connection;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Parameter;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.joda.time.DateTime;
import tv.notube.commons.model.User;
import tv.notube.commons.model.activity.*;
import tv.notube.commons.model.activity.Object;
import tv.notube.listener.facebook.model.FacebookChange;
import tv.notube.listener.facebook.model.FacebookData;
import tv.notube.listener.facebook.model.FacebookNotification;
import tv.notube.resolver.Resolver;
import tv.notube.resolver.ResolverException;
import tv.notube.usermanager.UserManager;
import tv.notube.usermanager.UserManagerException;

import javax.servlet.http.HttpServletRequest;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
public class FacebookListener extends RouteBuilder {

    private final static String SERVICE = "facebook";

    @Inject
    private Resolver resolver;

    @Inject
    private UserManager userManager;

    @Override
    public void configure() throws Exception {

        from("jetty:http://0.0.0.0:34567/facebook")
                .choice()
                .when(header(Exchange.HTTP_METHOD).isEqualTo("GET"))
                .to("direct:verification")
                .when(header(Exchange.HTTP_METHOD).isEqualTo("POST"))
                .to("direct:streaming");

        from("direct:verification")
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        log.debug("started verification");
                        HttpServletRequest request = exchange.getIn().getHeader(Exchange.HTTP_SERVLET_REQUEST, HttpServletRequest.class);
                        log.debug("request: " + request);
                        if (request.getParameter("hub.mode") != null &&
                                request.getParameter("hub.verify_token") != null) {
                            if (request.getParameter("hub.mode").equals("subscribe") &&
                                    request.getParameter("hub.verify_token").equals("TEST-BEANCOUNTER-FACEBOOK")) {
                                exchange.getOut().setBody(request.getParameter("hub.challenge"));
                            }
                        }
                        log.debug("hub.mode [" + request.getParameter("hub.mode") + "] - hub.verify_token [" + request.getParameter("hub.verify_token") + "]");
                    }
                });

        from("direct:streaming")
                .unmarshal().json(JsonLibrary.Jackson, FacebookNotification.class)
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        FacebookNotification notification = exchange.getIn().getBody(FacebookNotification.class);
                        List<Activity> activities = getActivities(notification);
                        exchange.getIn().setBody(activities);
                    }

                    private List<Activity> getActivities(FacebookNotification notification) {
                        List<Activity> activities = new ArrayList<Activity>();
                        for (FacebookChange change : notification.getEntry()) {
                            String userId = change.getUid();
                            String token = getAccessToken(userId);
                            FacebookClient client = new DefaultFacebookClient(token);
                            for (String field : change.getChangedFields()) {
                                // TODO (low) limit should be configurable
                                // to handle expired tokes:
                                // catch com.restfb.exception.FacebookOAuthException
                                // get the response error: should be
                                //      "code": 190, "error_subcode": 460
                                // during the handling:
                                // ask to the usermanager to renew the token
                                // and store it again (this is a responsability
                                // of the user manager)
                                // this shows how http://stackoverflow.com/questions/10971218/how-to-handle-expired-token-from-server-side
                                Connection<FacebookData> entities = client.fetchConnection(
                                        "me/" + field,
                                        FacebookData.class,
                                        Parameter.with("limit", 10)
                                );
                                List<FacebookData> filteredLikes = filter(entities);
                                log.debug("RECEIVED ENTITIES (already filtered): " + filteredLikes.toString());
                                activities.addAll(convertToActivities(userId, filteredLikes));
                            }
                        }
                        return activities;
                    }

                    private List<FacebookData> filter(Connection<FacebookData> entities) {
                        List<FacebookData> result = new ArrayList<FacebookData>();
                        for (FacebookData entity : entities.getData()) {
                            DateTime date = new DateTime(entity.getCreatedTime());
                            // TODO (high) persist and retrieve correctly
                            if (date.isAfter(getOldTimeStamp())) {
                                result.add(entity);
                            }
                        }
                        return result;
                    }
                })
                .split(body())
                .marshal().json(JsonLibrary.Jackson)
                .log(body().toString())
                .to("kestrel://{{kestrel.queue.social.url}}");
    }

    private long getOldTimeStamp() {
        return 1;
    }

    private List<Activity> convertToActivities(String userId, List<FacebookData> likes) {
        List<Activity> activities = new ArrayList<Activity>();
        for (FacebookData like : likes) {
            try {
                Activity activity = new Activity();
                // TODO (med)
                activity.setVerb(Verb.LIKE);
                tv.notube.commons.model.activity.Object object = new Object();
                object.setName(like.getName());
                object.setDescription(like.getCategory());
                object.setUrl(new URL("http://www.facebook.com/" + like.getId()));
                activity.setObject(object);
                Context context = new Context();
                context.setUsername(userId);
                context.setDate(new DateTime(like.getCreatedTime()));
                context.setService("facebook");
                activity.setContext(context);
                activities.add(activity);
            } catch (MalformedURLException e) {
                log.error("the url is malformed");
            }
        }
        return activities;
    }

    private String getAccessToken(String identifier) {
        String username;
        try {
            username = resolver.resolveUsername(identifier, SERVICE);
        } catch (ResolverException e) {
            final String errMsg = "Error while resolving username [" + identifier + "] on facebook";
            log.error(errMsg, e);
            throw new RuntimeException(errMsg, e);
        }
        User userObj;
        try {
            userObj = userManager.getUser(username);
        } catch (UserManagerException e) {
            final String errMsg = "Error while getting user with username [" + username + "]";
            log.error(errMsg, e);
            throw new RuntimeException(errMsg, e);
        }
        return userObj.getServices().get(SERVICE).getSession();
    }
}