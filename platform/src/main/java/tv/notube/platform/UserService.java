package tv.notube.platform;

import com.google.inject.Inject;
import tv.notube.applications.Application;
import tv.notube.applications.ApplicationsManager;
import tv.notube.applications.ApplicationsManagerException;
import tv.notube.applications.Permission;
import tv.notube.commons.model.OAuthToken;
import tv.notube.commons.model.User;
import tv.notube.commons.model.UserProfile;
import tv.notube.commons.model.activity.Activity;
import tv.notube.crawler.Crawler;
import tv.notube.crawler.CrawlerException;
import tv.notube.crawler.Report;
import tv.notube.platform.responses.*;
import tv.notube.profiles.Profiles;
import tv.notube.profiles.ProfilesException;
import tv.notube.usermanager.UserManager;
import tv.notube.usermanager.UserManagerException;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.List;
import java.util.UUID;

/**
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
@Path("rest/user")
@Produces(MediaType.APPLICATION_JSON)
public class UserService extends JsonService {

    private ApplicationsManager applicationsManager;

    private UserManager userManager;

    private Profiles profiles;

    private Crawler crawler;

    @Inject
    public UserService(
            final ApplicationsManager am,
            final UserManager um,
            final Profiles ps,
            final Crawler cr
    ) {
        this.applicationsManager = am;
        this.userManager = um;
        this.crawler = cr;
        this.profiles = ps;
    }

    @POST
    @Path("/register")
    public Response signUp(
            @FormParam("name") String name,
            @FormParam("surname") String surname,
            @FormParam("username") String username,
            @FormParam("password") String password,
            @QueryParam("apikey") String apiKey
    ) {
        try {
            check(
                    this.getClass(),
                    "signUp",
                    name,
                    surname,
                    username,
                    password,
                    apiKey
            );
        } catch (ServiceException e) {
            return error(e, "Error while checking parameters");
        }
        boolean isAuth;
        try {
            isAuth = applicationsManager.isAuthorized(apiKey);
        } catch (ApplicationsManagerException e) {
            return error(e, "Error while authorizing your application");
        }
        if (!isAuth) {
            Response.ResponseBuilder rb = Response.serverError();
            rb.entity(new StringPlatformResponse(
                    StringPlatformResponse.Status.NOK,
                    "Your application is not authorized.Sorry.")
            );
            return rb.build();
        }
        try {
            if (userManager.getUser(username) != null) {
                final String errMsg = "username '" + username + "' is already taken";
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

        User user = new User();
        user.setName(name);
        user.setSurname(surname);
        user.setUsername(username);
        user.setPassword(password);
        try {
            userManager.storeUser(user);
        } catch (UserManagerException e) {
            final String errMsg = "Error while storing user '" + user + "'.";
            return error(e, errMsg);
        }
        Application application;
        try {
            application = applicationsManager.getApplicationByApiKey(apiKey);
        } catch (ApplicationsManagerException e) {
            return error(e, "Error while getting application with key '" + apiKey + "'");
        }
        if (application == null) {
            Response.ResponseBuilder rb = Response.serverError();
            rb.entity(new StringPlatformResponse(
                    StringPlatformResponse.Status.NOK,
                    "Application not found")
            );
            return rb.build();
        }

        try {
            applicationsManager.grantPermission(
                    application.getName(),
                    user.getId(),
                    Permission.Action.DELETE
            );
            applicationsManager.grantPermission(
                    application.getName(),
                    user.getId(),
                    Permission.Action.UPDATE
            );
        } catch (ApplicationsManagerException e) {
            return error(e, "Error while granting permissions on user " +  user.getId());
        }
        Response.ResponseBuilder rb = Response.ok();
        rb.entity(new UUIDPlatformResponse(
                UUIDPlatformResponse.Status.OK,
                "user successfully registered",
                user.getId())
        );
        return rb.build();
    }


    @GET
    @Path("/{username}")
    public Response getUser(
            @PathParam("username") String username,
            @QueryParam("apikey") String apiKey
    ) {
        try {
            check(
                    this.getClass(),
                    "getUser",
                    username,
                    apiKey
            );
        } catch (ServiceException e) {
            return error(e, "Error while checking parameters");
        }
        boolean isAuth;
        try {
            isAuth = applicationsManager.isAuthorized(apiKey);
        } catch (ApplicationsManagerException e) {
            return error(e, "Error while authenticating your application");
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
            final String errMsg = "Error while getting user '" + username + "'.";
            return error(e, errMsg);
        }
        if (user == null) {
            Response.ResponseBuilder rb = Response.serverError();
            rb.entity(
                    new StringPlatformResponse(
                            StringPlatformResponse.Status.NOK,
                            "user '" + username + "' not found"
                    )
            );
            return rb.build();
        }

        Response.ResponseBuilder rb = Response.ok();
        rb.entity(new UserPlatformResponse(
                UserPlatformResponse.Status.OK,
                "user '" + username + "' found",
                user)
        );
        return rb.build();
    }


    @GET
    @Path("/{username}/activities")
    public Response getActivities(
            @PathParam("username") String username,
            @QueryParam("apikey") String apiKey
    ) {
        try {
            check(
                    this.getClass(),
                    "getActivities",
                    username,
                    apiKey
            );
        } catch (ServiceException e) {
            return error(e, "Error while checking parameters");
        }

        boolean isAuth;
        try {
            isAuth = applicationsManager.isAuthorized(apiKey);
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
            return error(e, "Error while retrieving user '" + username + "'");
        }
        if (user == null) {
            Response.ResponseBuilder rb = Response.serverError();
            rb.entity(
                    new StringPlatformResponse(
                            StringPlatformResponse.Status.NOK,
                            "user with username '" + username + "' not found"
                    )
            );
            return rb.build();
        }
        List<Activity> activities;
        try {
            activities = userManager.getUserActivities(user.getId());
        } catch (UserManagerException e) {
            return error(e, "Error while getting user '" + username + "' activities");
        }
        Response.ResponseBuilder rb = Response.ok();
        rb.entity(new ActivitiesPlatformResponse(
                ActivitiesPlatformResponse.Status.OK,
                "user '" + username + "' activities found", activities)
        );
        return rb.build();
    }

    @DELETE
    @Path("/{username}")
    public Response deleteUser(
            @PathParam("username") String username,
            @QueryParam("apikey") String apiKey
    ) {
        try {
            check(
                    this.getClass(),
                    "deleteUser",
                    username,
                    apiKey
            );
        } catch (ServiceException e) {
            return error(e, "Error while checking parameters");
        }

        User user;
        try {
            user = userManager.getUser(username);
        } catch (UserManagerException e) {
            return error(e, "Error while retrieving user '" + username + "'");
        }
        if (user == null) {
            Response.ResponseBuilder rb = Response.serverError();
            rb.entity(
                    new StringPlatformResponse(
                            StringPlatformResponse.Status.NOK,
                            "user with username '" + username + "' not found")
            );
            return rb.build();
        }

        boolean isAuth;
        try {
            isAuth = applicationsManager.isAuthorized(
                    apiKey,
                    user.getId(),
                    Permission.Action.DELETE
            );
        } catch (ApplicationsManagerException e) {
            return error(e, "Error while authorizing your application");
        }
        if (!isAuth) {
            Response.ResponseBuilder rb = Response.serverError();
            rb.entity(new StringPlatformResponse(
                    StringPlatformResponse.Status.NOK,
                    "Sorry, you're not allowed to do that")
            );
            return rb.build();
        }

        try {
            userManager.deleteUser(user.getId());
        } catch (UserManagerException e) {
            throw new RuntimeException("Error while deleting user '" + username
                    + "'", e);
        }
        Response.ResponseBuilder rb = Response.ok();
        rb.entity(new StringPlatformResponse(
                StringPlatformResponse.Status.OK,
                "user with username '" + username + "' deleted")
        );
        return rb.build();
    }


    @POST
    @Path("/{username}/authenticate")
    public Response authenticate(
            @PathParam("username") String username,
            @FormParam("password") String password,
            @QueryParam("apikey") String apiKey
    ) {
        try {
            check(
                    this.getClass(),
                    "authenticate",
                    username,
                    password,
                    apiKey
            );
        } catch (ServiceException e) {
            return error(e, "Error while checking parameters");
        }
        boolean isAuth;
        try {
            isAuth = applicationsManager.isAuthorized(apiKey);
        } catch (ApplicationsManagerException e) {
            return error(e, "Error while authenticating your application");
        }
        if (!isAuth) {
            Response.ResponseBuilder rb = Response.serverError();
            rb.entity(new StringPlatformResponse(
                    StringPlatformResponse.Status.NOK,
                    "Sorry, you're not allowed to do that")
            );
            return rb.build();
        }
        User user;
        try {
            user = userManager.getUser(username);
        } catch (UserManagerException e) {
            return error(e, "Error while retrieving user '" + username + "'");
        }
        if (user == null) {
            Response.ResponseBuilder rb = Response.serverError();
            rb.entity(new StringPlatformResponse(
                    StringPlatformResponse.Status.NOK,
                    "user with username '" + username + "' not found")
            );
            return rb.build();
        }
        if (!user.getPassword().equals(password)) {
            Response.ResponseBuilder rb = Response.serverError();
            rb.entity(new StringPlatformResponse(
                    StringPlatformResponse.Status.NOK,
                    "password for '" + username + "' incorrect")
            );
            return rb.build();
        }
        Response.ResponseBuilder rb = Response.ok();
        rb.entity(new StringPlatformResponse(
                StringPlatformResponse.Status.OK,
                "user '" + username + "' authenticated")
        );
        return rb.build();
    }


    @GET
    @Path("/oauth/token/{service}/{username}")
    public Response getOAuthToken(
            @PathParam("service") String service,
            @PathParam("username") String username,
            @QueryParam("redirect") String finalRedirect
    ) {
        User userObj;
        try {
            userObj = userManager.getUser(username);
        } catch (UserManagerException e) {
            return error(e, "Error while retrieving user '" + username + "'");
        }
        OAuthToken oAuthToken;
        try {
            oAuthToken = userManager.getOAuthToken(service, userObj.getUsername());
        } catch (UserManagerException e) {
            return error(e, "Error while getting token for user '" + username + "' " +
                            "on service '" + service + "'");
        }
        URL finalRedirectUrl;
        try {
            finalRedirectUrl = new URL(finalRedirect);
        } catch (MalformedURLException e) {
            return error(e, "Error while getting token for user '" + username + "' " +
                            "on service '" + service + "'");
        }
        try {
            userManager.setUserFinalRedirect(userObj.getUsername(), finalRedirectUrl);
        } catch (UserManagerException e) {
            return error(e, "Error while setting temporary final redirect URL " +
                            "for user '" + username + "' " + "on service '" + service + "'");
        }
        URL redirect = oAuthToken.getRedirectPage();
        try {
            return Response.temporaryRedirect(redirect.toURI()).build();
        } catch (URISyntaxException e) {
            return error(e, "Malformed redirect URL");
        }
    }


    @GET
    @Path("/oauth/callback/facebook/{username}/")
    public Response handleFacebookAuthCallback(
            @PathParam("username") String username,
            @QueryParam("code") String verifier
    ) {
        // Facebook OAuth exchange quite different from Twitter's one.
        return handleOAuthCallback("facebook", username, null, verifier);
    }


    @GET
    @Path("/oauth/callback/{service}/{username}/")
    public Response handleOAuthCallback(
            @PathParam("service") String service,
            @PathParam("username") String username,
            @QueryParam("oauth_token") String token,
            @QueryParam("oauth_verifier") String verifier
    ) {
        User userObj;
        try {
            userObj = userManager.getUser(username);
        } catch (UserManagerException e) {
            return error(e, "Error while retrieving user '" + username + "'");
        }
        try {
            userManager.registerOAuthService(service, userObj, token, verifier);
        } catch (UserManagerException e) {
            return error(e, "Error while OAuth-like exchange for service: '" + service + "'");
        }
        URL finalRedirectUrl;
        try {
            finalRedirectUrl = userManager.consumeUserFinalRedirect(userObj.getUsername());
        } catch (UserManagerException e) {
            return error(e, "Error while getting final redirect URL for user '" + username + "' for service '" + service + "'");
        }
        try {
            return Response.temporaryRedirect(finalRedirectUrl.toURI()).build();
        } catch (URISyntaxException e) {
            return error(e, "Malformed redirect URL");
        }
    }


    @GET
    @Path("/auth/callback/{service}/{username}/{redirect}")
    public Response handleAuthCallback(
            @PathParam("service") String service,
            @PathParam("username") String username,
            @PathParam("redirect") String redirect,
            @QueryParam("token") String token
    ) {
        User userObj;
        try {
            userObj = userManager.getUser(username);
        } catch (UserManagerException e) {
            return error(e, "Error while retrieving user '" + username + "'");
        }
        try {
            userManager.registerService(service, userObj, token);
        } catch (UserManagerException e) {
            return error(e, "Error while OAuth-like exchange for service: '" + service + "'");
        }
        URL finalRedirectUrl;
        try {
            finalRedirectUrl = new URL(
                    "http://" + URLDecoder.decode(redirect, "UTF-8")
            );
        } catch (MalformedURLException e) {
            return error(e, "Error while getting token for user '" + username + "' " +
                            "on service '" + service + "'");
        } catch (UnsupportedEncodingException e) {
            return error(e, "Error while getting token for user '" + username + "' " +
                            "on service '" + service + "'");
        }
        try {
            return Response.temporaryRedirect(finalRedirectUrl.toURI()).build();
        } catch (URISyntaxException e) {
            return error(e, "Malformed redirect URL");
        }
    }

    @DELETE
    @Path("source/{username}/{service}")
    public Response removeSource(
            @PathParam("username") String username,
            @PathParam("service") String service,
            @QueryParam("apikey") String apiKey
    ) {
        try {
            check(
                    this.getClass(),
                    "removeSource",
                    username,
                    service,
                    apiKey
            );
        } catch (ServiceException e) {
            return error(e, "Error while checking parameters");
        }
        User userObj;
        try {
            userObj = userManager.getUser(username);
        } catch (UserManagerException e) {
            return error(e, "Error while retrieving user '" + username + "'");
        }

        boolean isAuth;
        try {
            isAuth = applicationsManager.isAuthorized(
                    apiKey,
                    userObj.getId(),
                    Permission.Action.UPDATE
            );
        } catch (ApplicationsManagerException e) {
            return error(e, "Error while asking for permissions");
        }
        if (!isAuth) {
            Response.ResponseBuilder rb = Response.serverError();
            rb.entity(new StringPlatformResponse(
                    StringPlatformResponse.Status.NOK,
                    "You're not allow to do that. Sorry.")
            );
            return rb.build();
        }

        try {
            userManager.deregisterService(service, userObj);
        } catch (UserManagerException e) {
            return error(e, "Error while retrieving user '" + username + "'");
        }
        Response.ResponseBuilder rb = Response.ok();
        rb.entity(new StringPlatformResponse(
                StringPlatformResponse.Status.OK,
                "service '" + service + "' removed from user '" + username + "'")
        );
        return rb.build();
    }


    @GET
    @Path("/{username}/profile")
    public Response getProfile(
            @PathParam("username") String username,
            @QueryParam("apikey") String apiKey
    ) {
        try {
            check(
                    this.getClass(),
                    "getProfile",
                    username,
                    apiKey
            );
        } catch (ServiceException e) {
            return error(e, "Error while checking parameters");
        }
        boolean isAuth;
        try {
            isAuth = applicationsManager.isAuthorized(apiKey);
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
        UserProfile up;
        try {
            // TODO (high) fix this.
            up = profiles.lookup(UUID.randomUUID());
        } catch (ProfilesException e) {
            return error(e, "Error while retrieving profile for user '" + username + "'");
        }
        Response.ResponseBuilder rb = Response.ok();
        rb.entity(new UserProfilePlatformResponse(
                UserProfilePlatformResponse.Status.OK,
                "profile for user '" + username + "' found",
                up
        )
        );
        return rb.build();
    }

    @GET
    @Path("/{username}/activities/update")
    public Response forceUserCrawl(
            @PathParam("username") String username,
            @QueryParam("apikey") String apiKey
    ) {
        try {
            check(
                    this.getClass(),
                    "forceUserCrawl",
                    username,
                    apiKey
            );
        } catch (ServiceException e) {
            return error(e, "Error while checking parameters");
        }
        boolean isAuth;
        try {
            isAuth = applicationsManager.isAuthorized(apiKey);
        } catch (ApplicationsManagerException e) {
            return error(e, "Error while authenticating your application");
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
            return error(e, "Error while getting user with username [" + username + "]");
        }
        Report report;
        try {
            report = crawler.crawl(user.getId());
        } catch (CrawlerException e) {
            return error(e, "Error while getting activities for user [" + username + "]");
        }
        Response.ResponseBuilder rb = Response.ok();
        rb.entity(new ReportPlatformResponse(
                ReportPlatformResponse.Status.OK,
                "activities updated for [" + username + "]",
                report
        )
        );
        return rb.build();
    }

    /**
    @GET
    @Path("/{username}/profile/update")
    public Response forceUserProfiling(
            @PathParam("username") String username,
            @QueryParam("apikey") String apiKey
    ) {
        try {
            check(
                    this.getClass(),
                    "forceUserProfiling",
                    username,
                    apiKey
            );
        } catch (ServiceException e) {
            return error(e, "Error while checking parameters");
        }
        boolean isAuth;
        try {
            isAuth = applicationsManager.isAuthorized(apiKey);
        } catch (ApplicationsManagerException e) {
            return error(e, "Error while authenticating your application");
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
            return error(e, "Error while getting user with username [" + username + "]");
        }
        if(user == null) {
            Response.ResponseBuilder rb = Response.serverError();
            rb.entity(new StringPlatformResponse(
                    StringPlatformResponse.Status.NOK,
                    "Sorry. User [" + username + "] has not been found")
            );
            return rb.build();
        }
        try {
            profiler.run(user.getId());
        } catch (ProfilerException e) {
            return error(e, "Error while forcing profiling for [" + username + "]");
        }
        Response.ResponseBuilder rb = Response.ok();
        rb.entity(
                new StringPlatformResponse(
                        StringPlatformResponse.Status.OK,
                        "profile updated for [" + username + "]"
                )
        );
        return rb.build();
    }  **/

    /**
    @GET
    @Path("/{username}/profile/status")
    public Response getProfilingStatus(
            @PathParam("username") String username,
            @QueryParam("apikey") String apiKey
    ) {
        try {
            check(
                    this.getClass(),
                    "getProfilingStatus",
                    username,
                    apiKey
            );
        } catch (ServiceException e) {
            return error(e, "Error while checking parameters");
        }
        boolean isAuth;
        try {
            isAuth = applicationsManager.isAuthorized(apiKey);
        } catch (ApplicationsManagerException e) {
            return error(e, "Error while authenticating your application");
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
            return error(e, "Error while getting user with username [" + username + "]");
        }
        if(user == null) {
            Response.ResponseBuilder rb = Response.serverError();
            rb.entity(new StringPlatformResponse(
                    StringPlatformResponse.Status.NOK,
                    "Sorry. User [" + username + "] has not been found")
            );
            return rb.build();
        }
        String status = profiler.profilingStatus(user.getId());
        Response.ResponseBuilder rb = Response.ok();
        rb.entity(
                new StringPlatformResponse(
                        StringPlatformResponse.Status.OK,
                        "[" + username + "] " + status
                )
        );
        return rb.build();
    }
            */

}