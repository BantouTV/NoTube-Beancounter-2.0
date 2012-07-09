package tv.notube.listener.facebook;

import com.restfb.Connection;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Parameter;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.joda.time.DateTime;
import tv.notube.commons.model.activity.*;
import tv.notube.commons.model.activity.Object;
import tv.notube.listener.facebook.model.FacebookChange;
import tv.notube.listener.facebook.model.FacebookData;
import tv.notube.listener.facebook.model.FacebookNotification;

import javax.servlet.http.HttpServletRequest;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
public class FacebookListener extends RouteBuilder {

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
                        log.debug("hub.mode [" + request.getParameter("hub.mode") +
                                "] - hub.verify_token [" + request.getParameter("hub.verify_token") + "]");
                    }
                });

        from("direct:streaming")
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        //log.debug("streaming from facebook: " + exchange.getIn().getBody(String.class));
                    }
                })
                .unmarshal().json(JsonLibrary.Jackson, FacebookNotification.class)
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        FacebookNotification notification =
                                exchange.getIn().getBody(FacebookNotification.class);
                        log.debug("RECEIVED NOTIFICATION: " + notification.toString());
                        List<Activity> activities = new ArrayList<Activity>();
                        for (FacebookChange change : notification.getEntry()) {
                            String userId = change.getUid();
                            long newTimestamp = change.getTime();
                            String token = getAccessToken(userId);
                            log.debug("ACCESS TOKEN [ " + token + " ]");
                            FacebookClient client = new DefaultFacebookClient(token);
                            for (String field : change.getChangedFields()) {
                                long oldTimestamp = getTimestamp(userId, field);
                                log.debug("OLD TIMESTAMP [ " + oldTimestamp + " ]");
                                Connection<FacebookData> likes = client.fetchConnection(
                                        "me/likes",
                                        FacebookData.class,
                                        Parameter.with("limit", 10)
                                );
                                log.debug("RECEIVED LIKES: " + likes.getData().toString());
                                List<FacebookData> filteredLikes = new ArrayList<FacebookData>();
                                for (FacebookData l : likes.getData()) {
                                    DateTime date = new DateTime(l.getCreatedTime());
                                    if (date.isAfter(oldTimestamp)) {
                                        filteredLikes.add(l);
                                    }
                                }
                                log.debug(filteredLikes.toString());
                                activities.addAll(convertToActivities(userId, filteredLikes));
                            }
                        }
                        exchange.getIn().setBody(activities);
                    }
                })

                .split(body())
                .marshal().json(JsonLibrary.Jackson)
                .log(body().toString());

    }

    private List<Activity> convertToActivities(String userId, List<FacebookData> likes) {
        List<Activity> activities = new ArrayList<Activity>();
        for(FacebookData like : likes) {
            try {
                Activity activity = new Activity();
                activity.setVerb(Verb.LIKE);
                tv.notube.commons.model.activity.Object object = new Object();
                object.setName(like.getName());
                object.setDescription(like.getCategory());
                object.setUrl(new URL("http://www.facebook.com/" + like.getId()));
                activity.setObject(object);
                Context context = new Context();
                context.setUsername(userId);
                context.setDate(new DateTime(like.getCreatedTime()));
                context.setService(new URL("http://www.facebook.com"));
                activity.setContext(context);
                activities.add(activity);
            } catch (MalformedURLException e) {
                log.error("the url is malformed");
            }
        }
        return activities;
    }

    private String getAccessToken(String userId) {
        return "AAACEdEose0cBAA5IfZCEItsnzXMNKUIAcZCmtWH6vGYKrSVgXLigrZCYXOEyb12ArHZCA7WdOWGZBK9ZBsQRXsyJ0AwODQPsc1ZCZBeApVm9nAZDZD";
    }

    private long getTimestamp(String userId, String field) {
        // TODO some cool stuff with Jedis releasing also the pool if you have time
        return 1338933600000L;
    }
}