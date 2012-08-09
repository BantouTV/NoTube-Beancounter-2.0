package tv.notube.platform;

import com.google.inject.Inject;
import org.codehaus.jackson.map.ObjectMapper;
import tv.notube.activities.ActivityStore;
import tv.notube.activities.ActivityStoreException;
import tv.notube.activities.InvalidOrderException;
import tv.notube.activities.WildcardSearchException;
import tv.notube.applications.ApplicationsManager;
import tv.notube.applications.ApplicationsManagerException;
import tv.notube.commons.model.User;
import tv.notube.commons.model.activity.Activity;
import tv.notube.commons.model.activity.ResolvedActivity;
import tv.notube.platform.responses.ResolvedActivitiesPlatformResponse;
import tv.notube.platform.responses.ResolvedActivityPlatformResponse;
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
 * This service implements all the <i>REST</i> APIs needed to manage
 * user's activities.
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
@Path("rest/activities")
@Produces(MediaType.APPLICATION_JSON)
public class ActivitiesService extends JsonService {

    // TODO (mid) this should be configurable
    private static final int ACTIVITIES_LIMIT = 20;

    private static final String ACTIVITY= "activity";
    private static final String ACTIVITY_ID = "activityId";
    private static final String ACTIVITY_ID_OBJ = "activityIdObj";
    private static final String API_KEY = "apikey";
    private static final String IS_VISIBLE = "isVisible";
    private static final String ORDER = "order";
    private static final String PAGE_STRING = "page";
    private static final String PAGE_NUMBER = "pageNumber";
    private static final String PATH = "path";
    private static final String USER = "user";
    private static final String USERNAME = "username";
    private static final String VALUE = "value";
    private static final String VISIBILITY_OBJ = "vObj";

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
            @PathParam(USERNAME) String username,
            @FormParam(ACTIVITY) String jsonActivity,
            @QueryParam(API_KEY) String apiKey
    ) {
        Map<String, Object> params = new LinkedHashMap<String, Object>();
        params.put(USERNAME, username);
        params.put(ACTIVITY, jsonActivity);
        params.put(API_KEY, apiKey);

        Response error = validateRequest("addActivity", ApplicationsManager.Action.CREATE, params);

        if (error != null) {
            return error;
        }

        // deserialize the given activity
        Activity activity;
        try {
            activity = parse(jsonActivity);
        } catch (IOException e) {
            final String errMsg = "Error: cannot parse your input json";
            return error(e, errMsg);
        }
        User user = (User) params.get(USER);
        ResolvedActivity resolvedActivity = new ResolvedActivity(user.getId(), activity, user);
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

    @PUT
    @Path("/{activityId}/visible/{isVisible}")
    public Response setVisibility(
            @PathParam(ACTIVITY_ID) String activityId,
            @PathParam(IS_VISIBLE) String isVisible,
            @QueryParam(API_KEY) String apiKey
    ) {
        Map<String, Object> params = new LinkedHashMap<String, Object>();
        params.put(ACTIVITY_ID, activityId);
        params.put(IS_VISIBLE, isVisible);
        params.put(API_KEY, apiKey);

        Response error = validateRequest("setVisibility", ApplicationsManager.Action.DELETE, params);

        if (error != null) {
            return error;
        }

        boolean vObj = (Boolean) params.get(VISIBILITY_OBJ);
        try {
            activities.setVisible((UUID) params.get(ACTIVITY_ID_OBJ), vObj);
        } catch (ActivityStoreException e) {
            return error(e, "Error modifying the visibility of activity with id [" + activityId + "]");
        }

        Response.ResponseBuilder rb = Response.ok();
        rb.entity(
                new StringPlatformResponse(
                        StringPlatformResponse.Status.OK,
                        "activity [" + activityId + "] visibility has been modified to [" + vObj + "]"
                )
        );
        return rb.build();
    }

    // TODO (med): This no longer requires the username, just the activityId.
    @GET
    @Path("/get/{username}/{activityId}")
    public Response getActivity(
            @PathParam(USERNAME) String username,
            @PathParam(ACTIVITY_ID) String activityId,
            @QueryParam(API_KEY) String apiKey
    ) {
        Map<String, Object> params = new LinkedHashMap<String, Object>();
        params.put(USERNAME, username);
        params.put(ACTIVITY_ID, activityId);
        params.put(API_KEY, apiKey);

        Response error = validateRequest("getActivity", ApplicationsManager.Action.RETRIEVE, params);

        if (error != null) {
            return error;
        }

        ResolvedActivity activity;
        try {
            activity = activities.getActivity(UUID.fromString(activityId));
        } catch (ActivityStoreException e) {
            return error(
                    e,
                    "Error while getting activity [" + activityId + "] for user [" + username + "]"
            );
        }

        Response.ResponseBuilder rb = Response.ok();
        if (activity == null) {
            rb.entity(
                    new ResolvedActivityPlatformResponse(
                            ResolvedActivityPlatformResponse.Status.OK,
                            "user '" + username + "' has no activity with id [" + activityId + "]",
                            activity
                    )
            );
        } else {
            rb.entity(
                    new ResolvedActivityPlatformResponse(
                            ResolvedActivityPlatformResponse.Status.OK,
                            "user '" + username + "' activity with id [" + activityId + "] found",
                            activity
                    )
            );
        }
        return rb.build();
    }

    @GET
    @Path("/search")
    public Response search(
            @QueryParam(PATH) String path,
            @QueryParam(VALUE) String value,
            @QueryParam(PAGE_STRING) @DefaultValue("0") String pageString,
            @QueryParam(ORDER) @DefaultValue("desc") String order,
            @QueryParam(API_KEY) String apiKey
    ) {
        Map<String, Object> params = new LinkedHashMap<String, Object>();
        params.put(PATH, path);
        params.put(VALUE, value);
        params.put(PAGE_STRING, pageString);
        params.put(ORDER, order);
        params.put(API_KEY, apiKey);

        Response error = validateRequest("search", ApplicationsManager.Action.RETRIEVE, params);

        if (error != null) {
            return error;
        }

        Collection<ResolvedActivity> activitiesRetrieved;
        int page = (Integer) params.get(PAGE_NUMBER);
        try {
            activitiesRetrieved = activities.search(path, value, page, ACTIVITIES_LIMIT, order);
        } catch (ActivityStoreException ase) {
            return error(ase, "Error while getting page " + page
                    + " of activities where [" + path + "=" + value +"]");
        } catch (WildcardSearchException wse) {
            Response.ResponseBuilder rb = Response.serverError();
            rb.entity(new StringPlatformResponse(
                    StringPlatformResponse.Status.NOK,
                    wse.getMessage())
            );
            return rb.build();
        } catch (InvalidOrderException ioe) {
            Response.ResponseBuilder rb = Response.serverError();
            rb.entity(new StringPlatformResponse(
                    StringPlatformResponse.Status.NOK,
                    ioe.getMessage())
            );
            return rb.build();
        }
        Response.ResponseBuilder rb = Response.ok();
        rb.entity(
                new ResolvedActivitiesPlatformResponse(
                        ResolvedActivitiesPlatformResponse.Status.OK,
                        (activitiesRetrieved.isEmpty())
                                ? "search for [" + path + "=" + value + "] found no "
                                    + (page != 0 ? "more " : "") + "activities."
                                : "search for [" + path + "=" + value + "] found activities.",
                        activitiesRetrieved
                )
        );
        return rb.build();
    }

    @GET
    @Path("/all/{username}")
    public Response getAllActivities(
            @PathParam(USERNAME) String username,
            @QueryParam(PAGE_STRING) @DefaultValue("0") String pageString,
            @QueryParam(ORDER) @DefaultValue("desc") String order,
            @QueryParam(API_KEY) String apiKey
    ) {
        Map<String, Object> params = new LinkedHashMap<String, Object>();
        params.put(USERNAME, username);
        params.put(PAGE_STRING, pageString);
        params.put(ORDER, order);
        params.put(API_KEY, apiKey);

        Response error = validateRequest("getAllActivities", ApplicationsManager.Action.RETRIEVE, params);

        if (error != null) {
            return error;
        }

        User user = (User) params.get(USER);
        int page = (Integer) params.get(PAGE_NUMBER);

        Collection<ResolvedActivity> allActivities;
        try {
            allActivities = activities.getByUserPaginated(user.getId(), page, ACTIVITIES_LIMIT, order);
        } catch (ActivityStoreException e) {
            return error(
                    e,
                    "Error while getting page " + page + " of all the activities for user [" + username + "]"
            );
        } catch (InvalidOrderException ioe) {
            Response.ResponseBuilder rb = Response.serverError();
            rb.entity(new StringPlatformResponse(
                    StringPlatformResponse.Status.NOK,
                    ioe.getMessage())
            );
            return rb.build();
        }
        Response.ResponseBuilder rb = Response.ok();
        rb.entity(
                new ResolvedActivitiesPlatformResponse(
                        ResolvedActivitiesPlatformResponse.Status.OK,
                        (allActivities.isEmpty())
                            ? "user '" + username + "' has no " + (page != 0 ? "more " : "") + "activities."
                            : "user '" + username + "' activities found.",
                        allActivities
                )
        );
        return rb.build();
    }

    private Response validateRequest(
            String method,
            ApplicationsManager.Action action,
            Map<String, Object> params
    ) {
        try {
            check(
                    this.getClass(),
                    method,
                    params.values().toArray()
            );
        } catch (ServiceException e) {
            return error(e, "Error while checking parameters");
        }

        String apiKey = (String) params.get(API_KEY);

        try {
            UUID.fromString(apiKey);
        } catch (IllegalArgumentException e) {
            return error(e, "Your apikey is not well formed");
        }

        boolean isAuth;
        try {
            isAuth = applicationsManager.isAuthorized(
                    UUID.fromString(apiKey),
                    action,
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

        if (params.containsKey(ACTIVITY_ID)) {
            String activityId = (String) params.get(ACTIVITY_ID);
            UUID activityIdObj;
            try {
                activityIdObj = UUID.fromString(activityId);
            } catch (IllegalArgumentException e) {
                return error(e, "Your activityId is not well formed");
            }
            params.put(ACTIVITY_ID_OBJ, activityIdObj);
        }

        if (params.containsKey(USERNAME)) {
            String username = (String) params.get(USERNAME);
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
                params.put(USER, user);
            } catch (UserManagerException e) {
                final String errMsg = "Error while calling the UserManager";
                return error(e, errMsg);
            }
        }

        if (params.containsKey(IS_VISIBLE)) {
            boolean vObj;
            String visibility = (String) params.get(IS_VISIBLE);
            try {
                vObj = Boolean.valueOf(visibility);
            } catch (Exception e) {
                return error(e, "visibility parameter must be {true, false} and not [" + visibility + "]");
            }
            params.put(VISIBILITY_OBJ, vObj);
        }

        if (params.containsKey(PAGE_STRING)) {
            int page;
            String pageString = (String) params.get(PAGE_STRING);
            try {
                page = Integer.parseInt(pageString, 10);
            } catch (IllegalArgumentException e) {
                return error(e, "Your page number is not well formed");
            }
            params.put(PAGE_NUMBER, page);
        }

        return null;
    }
}
