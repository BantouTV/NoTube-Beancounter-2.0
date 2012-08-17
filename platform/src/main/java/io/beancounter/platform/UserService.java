package io.beancounter.platform;

import com.google.inject.Inject;
import io.beancounter.platform.validation.ApiKeyValidation;
import io.beancounter.platform.validation.RequestValidator;
import io.beancounter.platform.validation.UsernameValidation;
import org.codehaus.jackson.map.ObjectMapper;
import io.beancounter.applications.ApplicationsManager;
import io.beancounter.applications.ApplicationsManagerException;
import io.beancounter.commons.model.OAuthToken;
import io.beancounter.commons.model.User;
import io.beancounter.commons.model.UserProfile;
import io.beancounter.commons.model.activity.Activity;
import io.beancounter.commons.model.activity.ResolvedActivity;
import io.beancounter.commons.model.auth.OAuthAuth;
import io.beancounter.platform.responses.*;
import io.beancounter.profiles.Profiles;
import io.beancounter.profiles.ProfilesException;
import io.beancounter.queues.Queues;
import io.beancounter.queues.QueuesException;
import io.beancounter.usermanager.AtomicSignUp;
import io.beancounter.usermanager.UserManager;
import io.beancounter.usermanager.UserManagerException;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.util.*;

import static io.beancounter.platform.validation.RequestValidator.createParams;

/**
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
@Path("rest/user")
@Produces(MediaType.APPLICATION_JSON)
public class UserService extends JsonService {

    private ApplicationsManager applicationsManager;

    private UserManager userManager;

    private Profiles profiles;

    private Queues queues;

    private RequestValidator validator;

    @Inject
    public UserService(
            final ApplicationsManager am,
            final UserManager um,
            final Profiles ps,
            final Queues queues
    ) {
        this.applicationsManager = am;
        this.userManager = um;
        this.profiles = ps;
        this.queues = queues;

        validator = new RequestValidator();
        validator.addValidation(API_KEY, new ApiKeyValidation(am));
        validator.addValidation(USERNAME, new UsernameValidation(um));
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
        Map<String, Object> params = createParams(
                "name", name,
                "surname", surname,
                USERNAME, username,
                "password", password,
                API_KEY, apiKey
        );

        // TODO: eurgh....
        validator.removeValidation(USERNAME);

        Response error = validator.validateRequest(
                this.getClass(),
                "signUp",
                ApplicationsManager.Action.CREATE,
                ApplicationsManager.Object.USER,
                params
        );

        validator.addValidation(USERNAME, new UsernameValidation(userManager));

        if (error != null) {
            return error;
        }

        try {
            if (userManager.getUser(username) != null) {
                final String errMsg = "username [" + username + "] is already taken";
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
            final String errMsg = "Error while storing user [" + user + "]";
            return error(e, errMsg);
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
        Map<String, Object> params = createParams(
                USERNAME, username,
                API_KEY, apiKey
        );

        Response error = validator.validateRequest(
                this.getClass(),
                "getUser",
                ApplicationsManager.Action.RETRIEVE,
                ApplicationsManager.Object.USER,
                params
        );

        if (error != null) {
            return error;
        }

        Response.ResponseBuilder rb = Response.ok();
        rb.entity(
                new UserPlatformResponse(
                    UserPlatformResponse.Status.OK,
                    "user [" + username + "] found",
                    (User) params.get(USER)
                )
        );
        return rb.build();
    }

    @DELETE
    @Path("/{username}")
    public Response deleteUser(
            @PathParam("username") String username,
            @QueryParam("apikey") String apiKey
    ) {
        Map<String, Object> params = createParams(
                USERNAME, username,
                API_KEY, apiKey
        );

        Response error = validator.validateRequest(
                this.getClass(),
                "deleteUser",
                ApplicationsManager.Action.DELETE,
                ApplicationsManager.Object.USER,
                params
        );

        if (error != null) {
            return error;
        }

        try {
            User user = (User) params.get(USER);
            userManager.deleteUser(user.getUsername());
        } catch (UserManagerException e) {
            throw new RuntimeException(
                    "Error while deleting user [" + username + "]",
                    e
            );
        }

        Response.ResponseBuilder rb = Response.ok();
        rb.entity(new StringPlatformResponse(
                StringPlatformResponse.Status.OK,
                "user with username [" + username + "] deleted")
        );
        return rb.build();
    }

    @GET
    @Path("/{username}/{service}/check")
    public Response checkToken(
            @PathParam("username") String username,
            @PathParam("service") String service,
            @QueryParam("apikey") String apiKey
    ) {
        Map<String, Object> params = createParams(
                USERNAME, username,
                "service", service,
                API_KEY, apiKey
        );

        Response error = validator.validateRequest(
                this.getClass(),
                "checkToken",
                ApplicationsManager.Action.RETRIEVE,
                ApplicationsManager.Object.USER,
                params
        );

        if (error != null) {
            return error;
        }

        User user = (User) params.get(USER);
        OAuthAuth auth = (OAuthAuth) user.getAuth(service);
        if (auth == null) {
            Response.ResponseBuilder rb = Response.serverError();
            rb.entity(new StringPlatformResponse(
                    StringPlatformResponse.Status.NOK,
                    "user with username [" + username + "] has not a token for service [" + service + "]")
            );
            return rb.build();
        }
        if (auth.isExpired()) {
            Response.ResponseBuilder rb = Response.serverError();
            rb.entity(new StringPlatformResponse(
                    StringPlatformResponse.Status.NOK,
                    "[" + service + "] token for [" + username + "] has expired")
            );
            return rb.build();
        }
        Response.ResponseBuilder rb = Response.ok();
        rb.entity(new StringPlatformResponse(
                StringPlatformResponse.Status.OK,
                "[" + service + "] token for [" + username + "] is valid")
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
        Map<String, Object> params = createParams(
                USERNAME, username,
                "password", password,
                API_KEY, apiKey
        );

        Response error = validator.validateRequest(
                this.getClass(),
                "authenticate",
                ApplicationsManager.Action.RETRIEVE,
                ApplicationsManager.Object.USER,
                params
        );

        if (error != null) {
            return error;
        }

        User user = (User) params.get(USER);
        if (!user.getPassword().equals(password)) {
            Response.ResponseBuilder rb = Response.serverError();
            rb.entity(new StringPlatformResponse(
                    StringPlatformResponse.Status.NOK,
                    "password for [" + username + "] incorrect")
            );
            return rb.build();
        }
        Response.ResponseBuilder rb = Response.ok();
        rb.entity(new StringPlatformResponse(
                StringPlatformResponse.Status.OK,
                "user [" + username + "] authenticated")
        );
        return rb.build();
    }

    @GET
    @Path("/register/{service}/mobile")
    public Response signUpWithServiceMobile(
            @PathParam("service") String service
    ) {
        // get a token for an anonymous user
        OAuthToken oAuthToken;
        try {
            oAuthToken = userManager.getOAuthToken(service);
        } catch (UserManagerException e) {
            return error(
                    e,
                    "Error while getting token from [" + service + "]"
            );
        }
        // token asked, let's redirect
        URL redirect = oAuthToken.getRedirectPage();
        try {
            return Response.temporaryRedirect(redirect.toURI()).build();
        } catch (URISyntaxException e) {
            return error(e, "Malformed redirect URL");
        }
    }

    @GET
    @Path("/register/{service}/web")
    public Response signUpWithServiceWeb(
            @PathParam("service") String service,
            @QueryParam("redirect") String finalRedirect
    ) {
        URL finalRedirectURL;
        try {
            finalRedirectURL = new URL(finalRedirect);
        } catch (MalformedURLException e) {
            return error(e, "[" + finalRedirect + "] is not a valid URL");
        }
        OAuthToken oAuthToken;
        try {
            oAuthToken = userManager.getOAuthToken(service, finalRedirectURL);
        } catch (UserManagerException e) {
            return error(
                    e,
                    "Error while getting token from [" + service + "]"
            );
        }
        // token asked, let's redirect
        URL redirect = oAuthToken.getRedirectPage();
        try {
            return Response.temporaryRedirect(redirect.toURI()).build();
        } catch (URISyntaxException e) {
            return error(e, "Malformed redirect URL");
        }
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
            if (userObj == null) {
                Response.ResponseBuilder rb = Response.serverError();
                rb.entity(new StringPlatformResponse(
                        StringPlatformResponse.Status.NOK,
                        "user with username [" + username + "] not found")
                );
                return rb.build();
            }
        } catch (UserManagerException e) {
            return error(e, "Error while retrieving user [" + username + "]");
        }
        OAuthToken oAuthToken;
        try {
            oAuthToken = userManager.getOAuthToken(service, userObj.getUsername());
        } catch (UserManagerException e) {
            return error(
                    e,
                    "Error while getting token for user [" + username + "] on service [" + service + "]"
            );
        }
        URL finalRedirectUrl;
        try {
            finalRedirectUrl = new URL(finalRedirect);
        } catch (MalformedURLException e) {
            return error(
                    e,
                    "Final redirect URL [" + finalRedirect + "] is ill-formed"
            );
        }
        try {
            userManager.setUserFinalRedirect(userObj.getUsername(), finalRedirectUrl);
        } catch (UserManagerException e) {
            return error(
                    e,
                    "Error while setting temporary final redirect URL for user '" + username + "' " + "on service '" + service + "'"
            );
        }
        URL redirect = oAuthToken.getRedirectPage();
        try {
            return Response.temporaryRedirect(redirect.toURI()).build();
        } catch (URISyntaxException e) {
            return error(e, "Malformed redirect URL");
        }
    }
              //  /rest/user/oauth/atomic/callback/facebook/web/
    @GET
    @Path("/oauth/atomic/callback/{service}/web/{redirect}")
    public Response handleAtomicAuthCallbackWeb(
        @PathParam("service") String service,
        @PathParam("redirect") String finalRedirect,
        @QueryParam("code") String verifier
    ) {
        String decodedFinalRedirect;
        try {
            decodedFinalRedirect = URLDecoder.decode(finalRedirect, "UTF-8");
            decodedFinalRedirect = URLDecoder.decode(decodedFinalRedirect, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return error(e, "error while url decoding [" + finalRedirect + "]");
        }
        AtomicSignUp signUp;
        try {
            signUp = userManager.storeUserFromOAuth(service, verifier, decodedFinalRedirect);
        } catch (UserManagerException e) {
            return error(e, "Error while OAuth exchange for service: [" + service + "]");
        }
        User user;
        try {
            user = userManager.getUser(signUp.getUsername());
        } catch (UserManagerException e) {
            return error(e, "Error while retrieving user: [" + signUp.getUsername() + "]");
        }
        final int LIMIT = 40;
        List<Activity> activities;
        try {
            activities = userManager.grabUserActivities(
                    user,
                    signUp.getIdentifier(),
                    signUp.getService(),
                    LIMIT
            );
        } catch (UserManagerException e) {
            return error(e, "Error while retrieving user: [" + signUp.getUsername() + "] initial activities");
        }
        ObjectMapper mapper = new ObjectMapper();
        for (Activity activity : activities) {
            ResolvedActivity ra = new ResolvedActivity();
            ra.setActivity(activity);
            ra.setUserId(signUp.getUserId());
            String raJson;
            try {
                raJson = mapper.writeValueAsString(ra);
            } catch (IOException e) {
                // just skip this one
                continue;
            }
            try {
                queues.push(raJson);
            } catch (QueuesException e) {
                return error(e, "Error while pushing down json resolved activity: [" + raJson + "] for user [" + signUp.getUsername() + "] on service [" + signUp.getService() + "]");
            }
        }
        URI finalRedirectUri;
        try {
            finalRedirectUri = new URI(decodedFinalRedirect + "?username=" + signUp.getUsername());
        } catch (URISyntaxException e) {
            return error(e, "Malformed redirect URL");
        }
        return Response.temporaryRedirect(finalRedirectUri).build();
    }

    @GET
    @Path("/oauth/atomic/callback/{service}/")
    public Response handleAtomicAuthCallbackMobile(
            @PathParam("service") String service,
            @QueryParam("code") String verifier
    ) {
        AtomicSignUp signUp;
        try {
            signUp = userManager.storeUserFromOAuth(service, verifier);
        } catch (UserManagerException e) {
            return error(e, "Error while OAuth exchange for service: [" + service + "]");
        }
        User user;
        try {
            user = userManager.getUser(signUp.getUsername());
        } catch (UserManagerException e) {
            return error(e, "Error while retrieving user: [" + signUp.getUsername() + "]");
        }
        final int LIMIT = 40;
        List<Activity> activities;
        try {
            activities = userManager.grabUserActivities(
                    user,
                    signUp.getIdentifier(),
                    signUp.getService(),
                    LIMIT
            );
        } catch (UserManagerException e) {
            return error(e, "Error while retrieving user: [" + signUp.getUsername() + "] initial activities");
        }
        ObjectMapper mapper = new ObjectMapper();
        for (Activity activity : activities) {
            ResolvedActivity ra = new ResolvedActivity();
            ra.setActivity(activity);
            ra.setUserId(signUp.getUserId());
            String raJson;
            try {
                raJson = mapper.writeValueAsString(ra);
            } catch (IOException e) {
                // just skip this one
                continue;
            }
            try {
                queues.push(raJson);
            } catch (QueuesException e) {
                return error(e, "Error while pushing down json resolved activity: [" + raJson + "] for user [" + signUp.getUsername() + "] on service [" + signUp.getService() + "]");
            }
        }
        Response.ResponseBuilder rb = Response.ok();
        rb.entity(
                new AtomicSignUpResponse(
                        PlatformResponse.Status.OK,
                        "user with user name [" + signUp.getUsername() + "] logged in with service [" + signUp.getService() + "]",
                        signUp
                )
        );
        return rb.build();
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
            return error(e, "Error while retrieving user [" + username + "]");
        }
        try {
            userManager.registerService(service, userObj, token);
        } catch (UserManagerException e) {
            return error(
                    e,
                    "Error while OAuth-like exchange for service: [" + service + "]"
            );
        }
        URL finalRedirectUrl;
        try {
            finalRedirectUrl = new URL(
                    "http://" + URLDecoder.decode(redirect, "UTF-8")
            );
        } catch (MalformedURLException e) {
            return error(
                    e,
                    "Error while getting token for user [" + username + " ] on service [" + service + "]"
            );
        } catch (UnsupportedEncodingException e) {
            return error(
                    e,
                    "Error while getting token for user [" + username + "] on service [" + service + "]"
            );
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
        try {
            UUID.fromString(apiKey);
        } catch (IllegalArgumentException e) {
            return error(e, "Your apikey is not well formed");
        }
        User userObj;
        try {
            userObj = userManager.getUser(username);
            if (userObj == null) {
                return error(new NullPointerException(), "User [" + username + "] not found!");
            }
        } catch (UserManagerException e) {
            return error(e, "Error while retrieving user '" + username + "'");
        }
        boolean isAuth;
        try {
            isAuth = applicationsManager.isAuthorized(
                    UUID.fromString(apiKey),
                    ApplicationsManager.Action.UPDATE,
                    ApplicationsManager.Object.USER
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
            return error(e, "Error while retrieving user [" + username + "]");
        }
        Response.ResponseBuilder rb = Response.ok();
        rb.entity(new StringPlatformResponse(
                StringPlatformResponse.Status.OK,
                "service [" + service + "] removed from user [" + username + "]")
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
        try {
            UUID.fromString(apiKey);
        } catch (IllegalArgumentException e) {
            return error(e, "Your apikey is not well formed");
        }
        boolean isAuth;
        try {
            isAuth = applicationsManager.isAuthorized(
                    UUID.fromString(apiKey),
                    ApplicationsManager.Action.RETRIEVE,
                    ApplicationsManager.Object.PROFILE
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
        User userObj;
        try {
            userObj = userManager.getUser(username);
        } catch (UserManagerException e) {
            return error(e, "Error while retrieving user '" + username + "'");
        }
        if (userObj == null) {
            Response.ResponseBuilder rb = Response.serverError();
            rb.entity(new StringPlatformResponse(
                    StringPlatformResponse.Status.NOK,
                    "Sorry. User [" + username + "] has not been found")
            );
            return rb.build();
        }
        UserProfile up;
        try {
            up = profiles.lookup(userObj.getId());
        } catch (ProfilesException e) {
            return error(e, "Error while retrieving profile for user [" + username + "]");
        }
        if (up == null) {
            return error(new RuntimeException(), "Profile for user [" + username + "] not found");
        }
        Response.ResponseBuilder rb = Response.ok();
        rb.entity(new UserProfilePlatformResponse(
                UserProfilePlatformResponse.Status.OK,
                "profile for user [" + username + "] found",
                up
        )
        );
        return rb.build();
    }

}