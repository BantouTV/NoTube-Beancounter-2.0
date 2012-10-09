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
import io.beancounter.commons.model.activity.rai.Comment;
import io.beancounter.commons.model.notifies.Notify;
import io.beancounter.platform.responses.*;
import io.beancounter.platform.validation.*;
import io.beancounter.queues.Queues;
import io.beancounter.queues.QueuesException;
import io.beancounter.usermanager.UserManager;
import io.beancounter.usermanager.UserTokenManager;
import org.codehaus.jackson.map.ObjectMapper;
import org.joda.time.DateTime;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static io.beancounter.applications.ApplicationsManager.Action.DELETE;
import static io.beancounter.applications.ApplicationsManager.Action.RETRIEVE;
import static io.beancounter.applications.ApplicationsManager.Object.ACTIVITIES;

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

    private ApplicationsManager applicationsManager;

    private UserManager userManager;

    private UserTokenManager tokenManager;

    @Inject
    public ActivitiesService(
            final ApplicationsManager am,
            final UserManager um,
            final Queues queues,
            final ActivityStore activities,
            final UserTokenManager tokenManager
    ) {
        applicationsManager = am;
        userManager = um;
        this.queues = queues;
        this.activities = activities;
        this.tokenManager = tokenManager;
    }

    @POST
    @Path("/add/{username}")
    public Response addActivity(
            @PathParam(USERNAME) String username,
            @FormParam(ACTIVITY) String jsonActivity,
            @QueryParam(USER_TOKEN) String token
    ) {
        User user;
        try {
            Validations.checkNotEmpty(username);
            user = userManager.getUser(username);
            Validations.checkNotNull(user, "user with username [" + username + "] not found");
            Validations.validateUserToken(token, user, tokenManager);
        } catch (Exception ex) {
            return error(ex.getMessage());
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
            @PathParam(IS_VISIBLE) boolean isVisible,
            @QueryParam(API_KEY) String apiKey
    ) {
        try {
            Validations.checkNotEmpty(activityId);
            Validations.checkNotNull(isVisible);
            Validations.checkNotEmpty(apiKey);
            Validations.validateApiKey(apiKey, applicationsManager, DELETE, ACTIVITIES);
        } catch (Exception ex) {
            return error(ex.getMessage());
        }

        UUID activityIdObj;
        ResolvedActivity resolvedActivity;
        try {
            activityIdObj = UUID.fromString(activityId);
            resolvedActivity = activities.getActivity(activityIdObj);
        } catch (ActivityStoreException e) {
            return error(e, "Error while getting activity [" + activityId + "] from the storage");
        }

        try {
            activities.setVisible(activityIdObj, isVisible);
        } catch (ActivityStoreException e) {
            return error(e, "Error modifying the visibility of activity with id [" + activityId + "]");
        }

        // these notifications should be properly managed - this is
        // just a workaround for the 1.4 release.
        String jsonNotifyMessage;
        try {
            jsonNotifyMessage = getDeleteMessage(activityIdObj, isVisible, resolvedActivity);
        } catch (IOException e) {
            return error(e, "Error serializing the notification for [" + activityId + "] to [" + isVisible + "]");
        }
        try {
            queues.push(jsonNotifyMessage, "social");
        } catch (QueuesException e) {
            return error(e, "Error pushing [" + jsonNotifyMessage + "] down queue with name [social]");
        }
        // the code above should be soon-ish replaced with something more general.

        return success("activity [" + activityId + "] visibility has been modified to [" + isVisible + "]");
    }

    private String getDeleteMessage(UUID activityIdObj, boolean vObj, ResolvedActivity resolvedActivity) throws IOException {
        Notify notify = new Notify(
                "setVisibility",
                activityIdObj.toString(),
                String.valueOf(vObj)
        );
        if (resolvedActivity != null) {
            Comment comment;
            try {
                comment = (Comment) resolvedActivity.getActivity().getObject();
                notify.addMetadata("onEvent", comment.getOnEvent());
            } catch (ClassCastException e) {
                // just avoid to set the onEvent
            }
        }
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(notify);
    }

    @GET
    @Path("/{activityId}")
    public Response getActivity(
            @PathParam(ACTIVITY_ID) String activityId,
            @QueryParam(API_KEY) String apiKey
    ) {
        try {
            Validations.checkNotEmpty(activityId, "Missing activity id parameter");
            Validations.checkNotEmpty(apiKey, "Missing api key parameter");
            Validations.validateApiKey(apiKey, applicationsManager, DELETE, ACTIVITIES);
        } catch (Exception ex) {
            return error(ex.getMessage());
        }

        ResolvedActivity activity;
        try {
            activity = activities.getActivity(UUID.fromString(activityId));
        } catch (ActivityStoreException e) {
            return error(e, "Error while getting activity [" + activityId + "]");
        }

        if (activity == null) {
            return success("no activity with id [" + activityId + "]", activity);
        } else {
            return success("activity with id [" + activityId + "] found", activity);
        }
    }

    @GET
    @Path("/{username}/{activityId}")
    public Response getUserActivity(
            @PathParam(USERNAME) String username,
            @PathParam(ACTIVITY_ID) String activityId,
            @QueryParam(USER_TOKEN) String token
    ) {
        User user;
        try {
            Validations.checkNotEmpty(username);
            Validations.checkNotEmpty(activityId);
            user = userManager.getUser(username);
            Validations.checkNotNull(user, "user with username [" + username + "] not found");
            Validations.validateUserToken(token, user, tokenManager);
        } catch (Exception ex) {
            return error(ex.getMessage());
        }

        ResolvedActivity activity;
        try {
            activity = activities.getActivity(UUID.fromString(activityId));
        } catch (Exception e) {
            return error(e, "Error while getting activity [" + activityId + "]");
        }

        if (activity == null) {
            return success("no activity with id [" + activityId + "]", activity);
        }

        if (!user.getId().equals(activity.getUserId())) {
            return error("User [" + username + "] is not authorized to see activity [" + activityId + "]");
        }

        return success("activity with id [" + activityId + "] found", activity);
    }

    @GET
    @Path("/search/me")
    public Response searchWithToken(
            @QueryParam(PATH) String path,
            @QueryParam(VALUE) String value,
            @QueryParam(PAGE) @DefaultValue("0") int page,
            @QueryParam(ORDER) @DefaultValue("desc") String order,
            @QueryParam("filter") List<String> filters,
            @QueryParam(USER_TOKEN) String token
    ) {
        try {
            Validations.checkNotEmpty(path);
            Validations.checkNotEmpty(value);
            Validations.check(page >= 0, "Page must be at least 0 (zero)");
            Validations.check(
                    tokenManager.checkTokenExists(UUID.fromString(token)),
                    "User token [" + token + "] is not valid"
            );
        } catch (Exception ex) {
            return error(ex.getMessage());
        }

        return doSearch(path, value, page, order, filters);
    }

    @GET
    @Path("/search")
    public Response search(
            @QueryParam(PATH) String path,
            @QueryParam(VALUE) String value,
            @QueryParam(PAGE) @DefaultValue("0") int page,
            @QueryParam(ORDER) @DefaultValue("desc") String order,
            @QueryParam("filter") List<String> filters,
            @QueryParam(API_KEY) String apiKey
    ) {
        try {
            Validations.checkNotEmpty(path);
            Validations.checkNotEmpty(value);
            Validations.check(page >= 0, "Page must be at least 0 (zero)");
            Validations.checkNotEmpty(apiKey);
            Validations.validateApiKey(apiKey, applicationsManager, RETRIEVE, ACTIVITIES);
        } catch (Exception ex) {
            return error(ex.getMessage());
        }

        return doSearch(path, value, page, order, filters);
    }

    private Response doSearch(
            String path,
            String value,
            int page,
            String order,
            List<String> filters
    ) {
        Collection<ResolvedActivity> activitiesRetrieved;
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

        return success(
                (activitiesRetrieved.isEmpty())
                        ? "search for [" + path + "=" + value + "] found no "
                            + (page != 0 ? "more " : "") + "activities."
                        : "search for [" + path + "=" + value + "] found activities.",
                activitiesRetrieved
        );
    }

    @GET
    @Path("/all/{username}")
    public Response getAllActivities(
            @PathParam(USERNAME) String username,
            @QueryParam(PAGE) @DefaultValue("0") int page,
            @QueryParam(ORDER) @DefaultValue("desc") String order,
            @QueryParam(USER_TOKEN) String token
    ) {
        User user;
        try {
            Validations.checkNotEmpty(username);
            user = userManager.getUser(username);
            Validations.checkNotNull(user, "user with username [" + username + "] not found");
            Validations.validateUserToken(token, user, tokenManager);
        } catch (Exception ex) {
            return error(ex.getMessage());
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

        return success(
                (allActivities.isEmpty())
                        ? "user '" + username + "' has no " + (page != 0 ? "more " : "") + "activities."
                        : "user '" + username + "' activities found.",
                allActivities
        );
    }

    private static Response success(String message, Collection<ResolvedActivity> activities) {
        Response.ResponseBuilder rb = Response.ok();
        rb.entity(
                new ResolvedActivitiesPlatformResponse(
                        PlatformResponse.Status.OK,
                        message,
                        activities
                )
        );
        return rb.build();
    }

    private static Response success(String message, ResolvedActivity activity) {
        Response.ResponseBuilder rb = Response.ok();
        rb.entity(
                new ResolvedActivityPlatformResponse(
                        PlatformResponse.Status.OK,
                        message,
                        activity
                )
        );
        return rb.build();
    }
}
