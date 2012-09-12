package io.beancounter.platform;

import com.google.inject.Inject;
import io.beancounter.activities.ActivityStore;
import io.beancounter.activities.ActivityStoreException;
import io.beancounter.activities.InvalidOrderException;
import io.beancounter.activities.WildcardSearchException;
import io.beancounter.applications.ApplicationsManager;
import io.beancounter.commons.model.User;
import io.beancounter.commons.model.activity.Activity;
import io.beancounter.commons.model.activity.ResolvedActivity;
import io.beancounter.commons.model.notifies.Notify;
import io.beancounter.platform.responses.*;
import io.beancounter.platform.validation.*;
import io.beancounter.queues.Queues;
import io.beancounter.queues.QueuesException;
import io.beancounter.usermanager.UserManager;
import io.beancounter.usermanager.UserManagerException;
import io.beancounter.usermanager.UserTokenManager;
import org.codehaus.jackson.map.ObjectMapper;
import org.joda.time.DateTime;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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

    private Queues queues;

    private ActivityStore activities;

    private UserManager userManager;

    private UserTokenManager tokenManager;

    private RequestValidator validator;

    @Inject
    public ActivitiesService(
            final ApplicationsManager am,
            final UserManager um,
            final Queues queues,
            final ActivityStore activities,
            final UserTokenManager tokenManager
    ) {
        userManager = um;
        this.queues = queues;
        this.activities = activities;
        this.tokenManager = tokenManager;

        validator = new RequestValidator();
        validator.addValidation(API_KEY, new ApiKeyValidation(am));
        validator.addValidation(ACTIVITY_ID, new ActivityIdValidation());
        validator.addValidation(IS_VISIBLE, new VisibilityValidation());
        validator.addValidation(PAGE_STRING, new PageValidation());
        validator.addValidation(USERNAME, new UsernameValidation(um));
    }

    @POST
    @Path("/add/{username}")
    public Response addActivity(
            @PathParam(USERNAME) String username,
            @FormParam(ACTIVITY) String jsonActivity,
            @QueryParam(USER_TOKEN) String token
    ) {
        // TODO (high): Simplify request parameter validation.
        User user;
        try {
            user = userManager.getUser(username);
        } catch (UserManagerException e) {
            final String errMsg = "Error while retrieving user [" + username + "]";
            return error(e, errMsg);
        }

        if (user == null) {
            return error("user with username [" + username + "] not found");
        }

        try {
            UUID userToken = UUID.fromString(token);
            if (!userToken.equals(user.getUserToken()) || !tokenManager.checkTokenExists(userToken)) {
                return error("User token [" + token + "] is not valid");
            }
        } catch (Exception ex) {
            return error(ex, "Error validating user token [" + token + "]");
        }

        // deserialize the given activity
        Activity activity;
        try {
            activity = parse(jsonActivity);
        } catch (IOException e) {
            final String errMsg = "Error: cannot parse your input json";
            return error(e, errMsg);
        }

        // if the activity has not been provided, then set it to now
        if (activity.getContext().getDate() == null) {
            activity.getContext().setDate(DateTime.now());
        }

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
        Map<String, Object> params = RequestValidator.createParams(
                ACTIVITY_ID, activityId,
                IS_VISIBLE, isVisible,
                API_KEY, apiKey
        );

        Response error = validator.validateRequest(
                this.getClass(),
                "setVisibility",
                ApplicationsManager.Action.DELETE,
                ApplicationsManager.Object.ACTIVITIES,
                params
        );

        if (error != null) {
            return error;
        }

        boolean vObj = (Boolean) params.get(VISIBILITY_OBJ);
        UUID activityIdObj = (UUID) params.get(ACTIVITY_ID_OBJ);
        try {
            activities.setVisible(activityIdObj, vObj);
        } catch (ActivityStoreException e) {
            return error(e, "Error modifying the visibility of activity with id [" + activityId + "]");
        }

        // these notifications should be properly managed - this is
        // just a workaround for the 1.4 release.
        String jsonNotifyMessage;
        try {
            jsonNotifyMessage = getDeleteMessage(activityIdObj, vObj);
        } catch (IOException e) {
            return error(e, "Error serializing the notification for [" + activityId + "] to [" + vObj + "]");
        }
        try {
            queues.push(jsonNotifyMessage, "social");
        } catch (QueuesException e) {
            return error(e, "Error pushing [" + jsonNotifyMessage + "] down queue with name [social]");
        }
        // the code above should be soon-ish replaced with something more general.

        Response.ResponseBuilder rb = Response.ok();
        rb.entity(
                new StringPlatformResponse(
                        StringPlatformResponse.Status.OK,
                        "activity [" + activityId + "] visibility has been modified to [" + vObj + "]"
                )
        );
        return rb.build();
    }

    private String getDeleteMessage(UUID activityIdObj, boolean vObj) throws IOException {
        Notify notify = new Notify(
                "setVisibility",
                activityIdObj.toString(),
                String.valueOf(vObj)
        );
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(notify);
    }

    @GET
    @Path("/{activityId}")
    public Response getActivity(
            @PathParam(ACTIVITY_ID) String activityId,
            @QueryParam(API_KEY) String apiKey
    ) {
        Map<String, Object> params = RequestValidator.createParams(
                ACTIVITY_ID, activityId,
                API_KEY, apiKey
        );

        Response error = validator.validateRequest(
                this.getClass(),
                "getActivity",
                ApplicationsManager.Action.RETRIEVE,
                ApplicationsManager.Object.ACTIVITIES,
                params
        );

        if (error != null) {
            return error;
        }

        ResolvedActivity activity;
        try {
            activity = activities.getActivity(UUID.fromString(activityId));
        } catch (ActivityStoreException e) {
            return error(e, "Error while getting activity [" + activityId + "]");
        }

        Response.ResponseBuilder rb = Response.ok();
        if (activity == null) {
            rb.entity(
                    new ResolvedActivityPlatformResponse(
                            ResolvedActivityPlatformResponse.Status.OK,
                            "no activity with id [" + activityId + "]",
                            activity
                    )
            );
        } else {
            rb.entity(
                    new ResolvedActivityPlatformResponse(
                            ResolvedActivityPlatformResponse.Status.OK,
                            "activity with id [" + activityId + "] found",
                            activity
                    )
            );
        }
        return rb.build();
    }

    @GET
    @Path("/{username}/{activityId}")
    public Response getUserActivity(
            @PathParam(USERNAME) String username,
            @PathParam(ACTIVITY_ID) String activityId,
            @QueryParam(USER_TOKEN) String token
    ) {
        // TODO (high): Simplify request parameter validation.
        User user;
        try {
            user = userManager.getUser(username);
        } catch (UserManagerException e) {
            final String errMsg = "Error while retrieving user [" + username + "]";
            return error(e, errMsg);
        }

        if (user == null) {
            return error("user with username [" + username + "] not found");
        }

        try {
            UUID userToken = UUID.fromString(token);
            if (!userToken.equals(user.getUserToken()) || !tokenManager.checkTokenExists(userToken)) {
                return error("User token [" + token + "] is not valid");
            }
        } catch (Exception ex) {
            return error(ex, "Error validating user token [" + token + "]");
        }

        ResolvedActivity activity;
        try {
            activity = activities.getActivity(UUID.fromString(activityId));
        } catch (Exception e) {
            return error(e, "Error while getting activity [" + activityId + "]");
        }

        if (activity == null) {
            Response.ResponseBuilder rb = Response.ok();
            rb.entity(
                    new ResolvedActivityPlatformResponse(
                            ResolvedActivityPlatformResponse.Status.OK,
                            "no activity with id [" + activityId + "]",
                            activity
                    )
            );
            return rb.build();
        }

        if (!user.getId().equals(activity.getUserId())) {
            return error("User [" + username + "] is not authorized to see activity [" + activityId + "]");
        }

        Response.ResponseBuilder rb = Response.ok();
        rb.entity(
                new ResolvedActivityPlatformResponse(
                        ResolvedActivityPlatformResponse.Status.OK,
                        "activity with id [" + activityId + "] found",
                        activity
                )
        );
        return rb.build();
    }

    @GET
    @Path("/search/me")
    public Response searchWithToken(
            @QueryParam(PATH) String path,
            @QueryParam(VALUE) String value,
            @QueryParam(PAGE_STRING) @DefaultValue("0") String pageString,
            @QueryParam(ORDER) @DefaultValue("desc") String order,
            @QueryParam("filter") List<String> filters,
            @QueryParam(USER_TOKEN) String token
    ) {
        Map<String, Object> params = RequestValidator.createParams(
                PATH, path,
                VALUE, value,
                PAGE_STRING, pageString,
                ORDER, order,
                "filters", filters
        );
        Response error = validator.validateRequest(
                this.getClass(),
                "search",
                ApplicationsManager.Action.RETRIEVE,
                ApplicationsManager.Object.ACTIVITIES,
                params
        );

        if (error != null) {
            return error;
        }

        try {
            UUID userToken = UUID.fromString(token);
            if (!tokenManager.checkTokenExists(userToken)) {
                return error("User token [" + token + "] is not valid");
            }
        } catch (Exception ex) {
            return error(ex, "Error validating user token [" + token + "]");
        }

        return doSearch(params, path, value, order, filters);
    }

    @GET
    @Path("/search")
    public Response search(
            @QueryParam(PATH) String path,
            @QueryParam(VALUE) String value,
            @QueryParam(PAGE_STRING) @DefaultValue("0") String pageString,
            @QueryParam(ORDER) @DefaultValue("desc") String order,
            @QueryParam("filter") List<String> filters,
            @QueryParam(API_KEY) String apiKey
    ) {
        Map<String, Object> params = RequestValidator.createParams(
                PATH, path,
                VALUE, value,
                PAGE_STRING, pageString,
                ORDER, order,
                "filters", filters,
                API_KEY, apiKey
        );
        Response error = validator.validateRequest(
                this.getClass(),
                "search",
                ApplicationsManager.Action.RETRIEVE,
                ApplicationsManager.Object.ACTIVITIES,
                params
        );

        if (error != null) {
            return error;
        }

        return doSearch(params, path, value, order, filters);
    }

    private Response doSearch(
            Map<String, Object> params,
            String path,
            String value,
            String order,
            List<String> filters
    ) {
        Collection<ResolvedActivity> activitiesRetrieved;
        int page = (Integer) params.get(PAGE_NUMBER);
        try {
            activitiesRetrieved = activities.search(path, value, page, ACTIVITIES_LIMIT, order, filters);
        } catch (ActivityStoreException ase) {
            return error(ase, "Error while getting page " + page
                    + " of activities where [" + path + "=" + value + "]");
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
            @QueryParam(USER_TOKEN) String token
    ) {
        // TODO (high): Simplify request parameter validation.
        User user;
        try {
            user = userManager.getUser(username);
        } catch (UserManagerException e) {
            final String errMsg = "Error while retrieving user [" + username + "]";
            return error(e, errMsg);
        }

        if (user == null) {
            return error("user with username [" + username + "] not found");
        }

        try {
            UUID userToken = UUID.fromString(token);
            if (!userToken.equals(user.getUserToken()) || !tokenManager.checkTokenExists(userToken)) {
                return error("User token [" + token + "] is not valid");
            }
        } catch (Exception ex) {
            return error(ex, "Error validating user token [" + token + "]");
        }

        int page;
        try {
            page = Integer.parseInt(pageString, 10);
        } catch (IllegalArgumentException e) {
            return error(e, "Your page number is not well formed");
        }

        Collection<ResolvedActivity> allActivities;
        try {
            allActivities = activities.getByUserPaginated(user.getId(), page, ACTIVITIES_LIMIT, order);
        } catch (ActivityStoreException e) {
            return error(
                    e,
                    "Error while getting page " + page + " of all the activities for user [" + username + "]"
            );
        } catch (InvalidOrderException ioe) {
            return error(ioe.getMessage());
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
}
