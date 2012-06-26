package tv.notube.platform;

import com.google.inject.Inject;
import org.codehaus.jackson.map.ObjectMapper;
import tv.notube.applications.ApplicationsManager;
import tv.notube.applications.ApplicationsManagerException;
import tv.notube.commons.model.activity.Activity;
import tv.notube.platform.responses.StringPlatformResponse;
import tv.notube.platform.responses.UUIDPlatformResponse;
import tv.notube.queues.Queues;
import tv.notube.queues.QueuesException;
import tv.notube.usermanager.UserManager;
import tv.notube.usermanager.UserManagerException;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.UUID;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
@Path("rest/activities")
@Produces(MediaType.APPLICATION_JSON)
public class ActivitiesService extends JsonService {

    private ApplicationsManager applicationsManager;

    private Queues queues;

    private UserManager userManager;

    @Inject
    public ActivitiesService(
            final ApplicationsManager am,
            final UserManager um,
            final Queues queues
    ) {
        this.applicationsManager = am;
        this.userManager = um;
        this.queues = queues;
    }

    @POST
    @Path("/add/{username}")
    public Response addActivity(
            @PathParam("username") String username,
            @FormParam("activity") String jsonActivity,
            @QueryParam("apikey") String apiKey
    ) {
        try {
            check(
                    this.getClass(),
                    "addActivity",
                    username,
                    jsonActivity,
                    apiKey
            );
        } catch (ServiceException e) {
            return error(e, "Error while checking parameters");
        }
        boolean isAuth;
        try {
            isAuth = applicationsManager.isAuthorized(
                    UUID.fromString(apiKey),
                    ApplicationsManager.Action.CREATE,
                    ApplicationsManager.Object.ACTIVITIES
            );
        } catch (ApplicationsManagerException e) {
            return error(e, "Error while authorizing your application");
        }
        if (!isAuth) {
            Response.ResponseBuilder rb = Response.serverError();
            rb.entity(new StringPlatformResponse(
                    StringPlatformResponse.Status.NOK,
                    "application with key [" + apiKey + "] is not authorized")
            );
            return rb.build();
        }
        try {
            if (userManager.getUser(username) == null) {
                final String errMsg = "user with username [" + username + "] not found";
                Response.ResponseBuilder rb = Response.serverError();
                rb.entity(new StringPlatformResponse(
                        StringPlatformResponse.Status.NOK,
                        errMsg)
                );
                return rb.build();
            }
        } catch (UserManagerException e) {
            final String errMsg = "Error while calling the UserManager";
            return error(e, errMsg);
        }
        // deserialize the given activity
        Activity activity;
        try {
            activity = parse(jsonActivity);
        } catch (IOException e) {
            final String errMsg = "Error: cannot parse your input json";
            return error(e, errMsg);
        }
        try {
            jsonActivity = unparse(activity);
        } catch (IOException e) {
            final String errMsg = "Error while writing an identified JSON";
            return error(e, errMsg);
        }
        try {
            queues.push(jsonActivity);
        } catch (QueuesException e) {
            final String errMsg = "Error while sending the activity to the Queue";
            return error(e, errMsg);
        }
        Response.ResponseBuilder rb = Response.ok();
        rb.entity(new UUIDPlatformResponse(
                UUIDPlatformResponse.Status.OK,
                "activity successfully registered",
                activity.getId())
        );
        return rb.build();
    }

    private String unparse(Activity activity) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(activity);
    }

    private Activity parse(String jsonActivity) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(jsonActivity, Activity.class);
    }

}
