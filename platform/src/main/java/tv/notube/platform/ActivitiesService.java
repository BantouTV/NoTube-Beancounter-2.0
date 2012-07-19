package tv.notube.platform;

import com.google.inject.Inject;
import org.codehaus.jackson.map.ObjectMapper;
import tv.notube.activities.ActivityStore;
import tv.notube.activities.ActivityStoreException;
import tv.notube.applications.ApplicationsManager;
import tv.notube.applications.ApplicationsManagerException;
import tv.notube.commons.model.User;
import tv.notube.commons.model.activity.Activity;
import tv.notube.commons.model.activity.ResolvedActivity;
import tv.notube.platform.responses.ActivitiesPlatformResponse;
import tv.notube.platform.responses.ActivityPlatformResponse;
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
import java.util.*;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
@Path("rest/activities")
@Produces(MediaType.APPLICATION_JSON)
public class ActivitiesService extends JsonService {

    // TODO (mid) this should be configurable
    private static final int ACTIVITIES_LIMIT = 20;

    private ApplicationsManager applicationsManager;

    private Queues queues;

    private UserManager userManager;

    private ActivityStore activities;

    @Inject
    public ActivitiesService(
            final ApplicationsManager am,
            final UserManager um,
            final Queues queues,
            final ActivityStore activities
    ) {
        this.applicationsManager = am;
        this.userManager = um;
        this.queues = queues;
        this.activities = activities;
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
        try {
            UUID.fromString(apiKey);
        } catch (IllegalArgumentException e) {
            return error(e, "Your apikey is not well formed");
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
        User user;
        try {
            user = userManager.getUser(username);
            if (user == null) {
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
        ResolvedActivity resolvedActivity = new ResolvedActivity(user.getId(), activity);
        String jsonResolvedActivity;
        try {
            jsonResolvedActivity = parseResolvedActivity(resolvedActivity);
        } catch (IOException e) {
            final String errMsg = "Error while writing an identified JSON for the resolved activity";
            return error(e, errMsg);
        }
        try {
            queues.push(jsonResolvedActivity);
        } catch (QueuesException e) {
            final String errMsg = "Error while sending the resolved activity to the Queue";
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

    private String parseResolvedActivity(ResolvedActivity resolvedActivity) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(resolvedActivity);
    }

    private Activity parse(String jsonActivity) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(jsonActivity, Activity.class);
    }

    @GET
    @Path("/get/{username}/{activityId}")
    public Response getActivity(
            @PathParam("username") String username,
            @PathParam("activityId") String activityId,
            @QueryParam("apikey") String apiKey
    ) {
        try {
            check(
                    this.getClass(),
                    "getActivity",
                    username,
                    activityId,
                    apiKey
            );
        } catch (ServiceException e) {
            return error(e, "Error while checking parameters");
        }
        try {
            UUID.fromString(apiKey);
        } catch (IllegalArgumentException e) {
            return error(e, "Your apikey is not well formed");
        }
        try {
            UUID.fromString(activityId);
        } catch (IllegalArgumentException e) {
            return error(e, "Your activityId is not well formed");
        }
        boolean isAuth;
        try {
            isAuth = applicationsManager.isAuthorized(
                    UUID.fromString(apiKey),
                    ApplicationsManager.Action.RETRIEVE,
                    ApplicationsManager.Object.ACTIVITIES
            );
        } catch (ApplicationsManagerException e) {
            return error(e, "Error while authenticating you application");
        }
        if (!isAuth) {
            Response.ResponseBuilder rb = Response.serverError();
            rb.entity(new StringPlatformResponse(
                    StringPlatformResponse.Status.NOK,
                    "Sorry. You're not allowed to do that.")
            );
            return rb.build();
        }
        User user;
        try {
            user = userManager.getUser(username);
        } catch (UserManagerException e) {
            return error(e, "Error while retrieving user [" + username + "]");
        }
        if (user == null) {
            Response.ResponseBuilder rb = Response.serverError();
            rb.entity(
                    new StringPlatformResponse(
                            StringPlatformResponse.Status.NOK,
                            "user with username [" + username + "] not found"
                    )
            );
            return rb.build();
        }
        Activity activity;
        try {
            activity = activities.getByUser(
                    user.getId(),
                    UUID.fromString(activityId)
            );
        } catch (ActivityStoreException e) {
            return error(
                    e,
                    "Error while getting activity [" + activityId + "] for user [" + username + "]"
            );
        }
        Response.ResponseBuilder rb = Response.ok();
        if(activity == null) {
            rb.entity(
                    new ActivityPlatformResponse(
                            ActivityPlatformResponse.Status.OK,
                            "user '" + username + "' has no activity with id [" + activityId + "]",
                            activity
                    )
            );
        } else {
            rb.entity(
                    new ActivityPlatformResponse(
                            ActivityPlatformResponse.Status.OK,
                            "user '" + username + "' activity with id [" + activityId + "] found",
                            activity
                    )
            );
        }
        return rb.build();
    }


    @GET
    @Path("/all/{username}")
    public Response getAllActivities(
            @PathParam("username") String username,
            @QueryParam("page") @DefaultValue("0") String pageString,
            @QueryParam("apikey") String apiKey
    ) {
        try {
            check(
                    this.getClass(),
                    "getAllActivities",
                    username,
                    pageString,
                    apiKey
            );
        } catch (ServiceException e) {
            return error(e, "Error while checking parameters");
        }
        try {
            UUID.fromString(apiKey);
        } catch (IllegalArgumentException e) {
            return error(e, "Your apikey is not well formed");
        }
        int page;
        int showedActivities;
        try {
            page = Integer.parseInt(pageString);
            showedActivities = page * ACTIVITIES_LIMIT;
        } catch (IllegalArgumentException e) {
            return error(e, "Your page number is not well formed");
        }
        boolean isAuth;
        try {
            isAuth = applicationsManager.isAuthorized(
                    UUID.fromString(apiKey),
                    ApplicationsManager.Action.RETRIEVE,
                    ApplicationsManager.Object.ACTIVITIES
            );
        } catch (ApplicationsManagerException e) {
            return error(e, "Error while authenticating you application");
        }
        if (!isAuth) {
            Response.ResponseBuilder rb = Response.serverError();
            rb.entity(new StringPlatformResponse(
                    StringPlatformResponse.Status.NOK,
                    "Sorry. You're not allowed to do that.")
            );
            return rb.build();
        }
        User user;
        try {
            user = userManager.getUser(username);
        } catch (UserManagerException e) {
            return error(e, "Error while retrieving user [" + username + "]");
        }
        if (user == null) {
            Response.ResponseBuilder rb = Response.serverError();
            rb.entity(
                    new StringPlatformResponse(
                            StringPlatformResponse.Status.NOK,
                            "user with username [" + username + "] not found"
                    )
            );
            return rb.build();
        }
        Collection<Activity> allActivities;
        try {
            allActivities = activities.getByUser(user.getId());
        } catch (ActivityStoreException e) {
            return error(
                    e,
                    "Error while getting activity all the activies for user [" + username + "]"
            );
        }
        Response.ResponseBuilder rb = Response.ok();
        if(allActivities.size()==0) {
            rb.entity(
                    new ActivitiesPlatformResponse(
                            ActivitiesPlatformResponse.Status.OK,
                            "user '" + username + "' has no activities",
                            allActivities
                    )
            );
        } else {
            Collection<Activity> trimmedActivities = trimActivities(allActivities, showedActivities);
            rb.entity(
                    new ActivitiesPlatformResponse(
                            ActivitiesPlatformResponse.Status.OK,
                            "user '" + username + "' activities found.",
                            trimmedActivities
                    )
            );
        }
        return rb.build();
    }

    private Collection<Activity> trimActivities(Collection<Activity> activities, int page) {
        if(activities.size() < ACTIVITIES_LIMIT) {
            return activities;
        }
        List<Activity> list = new ArrayList<Activity>(activities);
        Collections.sort(list, new ActivityComparator());
        List<Activity> trimmed = new ArrayList<Activity>();
        if (page < list.size()) {
            int limit = page + ACTIVITIES_LIMIT;
            while (page < limit && page < list.size()) {
                trimmed.add(list.get(page));
                page++;
            }
        }
        return trimmed;
    }
}

class ActivityComparator implements Comparator<Activity> {
    @Override
    public int compare(Activity a1, Activity a2) {
        return a1.getContext().getDate().compareTo(a2.getContext().getDate());
    }
}
