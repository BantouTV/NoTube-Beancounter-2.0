package io.beancounter.platform;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import io.beancounter.commons.helper.UriUtils;
import io.beancounter.commons.model.activity.Activity;
import io.beancounter.commons.model.activity.ResolvedActivity;
import io.beancounter.commons.model.auth.AuthenticatedUser;
import io.beancounter.platform.validation.Validations;
import io.beancounter.queues.QueuesException;
import io.beancounter.usermanager.UserTokenManager;
import io.beancounter.applications.ApplicationsManager;
import io.beancounter.commons.model.OAuthToken;
import io.beancounter.commons.model.User;
import io.beancounter.commons.model.UserProfile;
import io.beancounter.commons.model.auth.OAuthAuth;
import io.beancounter.platform.responses.*;
import io.beancounter.profiles.Profiles;
import io.beancounter.profiles.ProfilesException;
import io.beancounter.queues.Queues;
import io.beancounter.usermanager.AtomicSignUp;
import io.beancounter.usermanager.UserManager;
import io.beancounter.usermanager.UserManagerException;
import io.beancounter.usermanager.grabber.ActivityGrabberManager;
import io.beancounter.usermanager.grabber.Callback;
import io.beancounter.usermanager.grabber.FacebookGrabber;
import io.beancounter.usermanager.grabber.TwitterGrabber;
import org.codehaus.jackson.map.ObjectMapper;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.util.List;
import java.util.UUID;

import static io.beancounter.applications.ApplicationsManager.Action.*;
import static io.beancounter.applications.ApplicationsManager.Object.USER;

/**
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
@Path("rest/user")
@Produces(MediaType.APPLICATION_JSON)
public class UserService extends JsonService {

    private ApplicationsManager applicationsManager;

    private UserManager userManager;

    private UserTokenManager tokenManager;

    private Profiles profiles;

    private Queues queues;

    private ActivityGrabberManager grabberManager;

    @Inject
    @Named("oauth.fail.redirect")
    String oAuthFailRedirect;

    @Inject
    public UserService(
            final ApplicationsManager am,
            final UserManager um,
            final UserTokenManager tokenManager,
            final Profiles ps,
            final Queues queues,
            final ActivityGrabberManager grabberManager
    ) {
        this.applicationsManager = am;
        this.tokenManager = tokenManager;
        this.userManager = um;
        this.profiles = ps;
        this.queues = queues;
        this.grabberManager = grabberManager;
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
        User user;
        try {
            Validations.checkNotEmpty(name);
            Validations.checkNotEmpty(surname);
            Validations.checkNotEmpty(username);
            Validations.checkNotEmpty(password);
            user = userManager.getUser(username);
            Validations.check(user == null, "username [" + username + "] is already taken");
            Validations.validateApiKey(apiKey, applicationsManager, CREATE, USER);
        } catch (Exception ex) {
            return error(ex.getMessage());
        }

        user = new User(name, surname, username, password);
        UUID userToken;
        try {
            userToken = tokenManager.createUserToken(username);
        } catch (UserManagerException e) {
            final String errMsg = "Error while getting token for user [" + user + "]";
            return error(e, errMsg);
        }
        user.setUserToken(userToken);
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
    @Path("/{username}/me")
    public Response getUserWithUserToken(
            @PathParam(USERNAME) String username,
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

        Response.ResponseBuilder rb = Response.ok();
        rb.entity(
                new UserPlatformResponse(
                        UserPlatformResponse.Status.OK,
                        "user [" + username + "] found",
                        user
                )
        );
        return rb.build();
    }

    @GET
    @Path("/{username}")
    public Response getUserWithApiKey(
            @PathParam(USERNAME) String username,
            @QueryParam(API_KEY) String apiKey
    ) {
        User user;
        try {
            Validations.checkNotEmpty(username);
            Validations.checkNotEmpty(apiKey, "Missing api key");
            Validations.validateApiKey(apiKey, applicationsManager, RETRIEVE, USER);
            user = userManager.getUser(username);
            Validations.checkNotNull(user, "user with username [" + username + "] not found");
        } catch (Exception ex) {
            return error(ex.getMessage());
        }

        Response.ResponseBuilder rb = Response.ok();
        rb.entity(
                new UserPlatformResponse(
                        UserPlatformResponse.Status.OK,
                        "user [" + username + "] found",
                        user
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
        User user;
        try {
            Validations.checkNotEmpty(username, "Must specify a username to delete");
            Validations.checkNotEmpty(apiKey, "Missing api key");
            Validations.validateApiKey(apiKey, applicationsManager, RETRIEVE, USER);
            user = userManager.getUser(username);
            Validations.checkNotNull(user, "user with username [" + username + "] not found");
        } catch (Exception ex) {
            return error(ex.getMessage());
        }

        try {
            userManager.deleteUser(user);
        } catch (UserManagerException e) {
            return error(e, "Error while deleting user [" + username + "]");
        }

        return success("user with username [" + username + "] deleted");
    }

    @GET
    @Path("/{username}/{service}/check")
    public Response checkToken(
            @PathParam("username") String username,
            @PathParam("service") String service,
            @QueryParam("apikey") String apiKey
    ) {
        User user;
        try {
            Validations.checkNotEmpty(username, "Must specify a username");
            Validations.checkNotEmpty(service, "Must specify a valid service");
            Validations.checkNotEmpty(apiKey, "Missing api key");
            Validations.validateApiKey(apiKey, applicationsManager, RETRIEVE, USER);
            user = userManager.getUser(username);
            Validations.checkNotNull(user, "user with username [" + username + "] not found");
        } catch (Exception ex) {
            return error(ex.getMessage());
        }

        OAuthAuth auth = (OAuthAuth) user.getAuth(service);
        if (auth == null) {
            return error("user with username [" + username + "] has not a token for service [" + service + "]");
        }
        if (auth.isExpired()) {
            return error("[" + service + "] token for [" + username + "] has expired");
        }
        return success("[" + service + "] token for [" + username + "] is valid");
    }

    @POST
    @Path("/{username}/authenticate")
    public Response authenticate(
            @PathParam("username") String username,
            @FormParam("password") String password,
            @QueryParam("apikey") String apiKey
    ) {
        User user;
        try {
            Validations.checkNotEmpty(username, "Must specify a username");
            Validations.checkNotEmpty(password, "Must specify a password");
            Validations.checkNotEmpty(apiKey, "Missing api key");
            Validations.validateApiKey(apiKey, applicationsManager, RETRIEVE, USER);
            user = userManager.getUser(username);
            Validations.checkNotNull(user, "user with username [" + username + "] not found");
        } catch (Exception ex) {
            return error(ex.getMessage());
        }
        if (!user.getPassword().equals(password)) {
            return error("password for [" + username + "] incorrect");
        }
        AtomicSignUp signUp = new AtomicSignUp(
                user.getId(),
                user.getUsername(),
                true,
                "beancounter",
                user.getUsername(),
                user.getUserToken()
        );
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
    @Path("/register/{service}/mobile")
    public Response signUpWithServiceMobile(
            @PathParam("service") String service
    ) {
        // get a token for an anonymous user
        OAuthToken oAuthToken;
        try {
            oAuthToken = userManager.getOAuthToken(service);
        } catch (UserManagerException e) {
            return error(e, "Error while getting token from [" + service + "]");
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
            return error(e, "Error while getting token from [" + service + "]");
        }
        LOGGER.info("redirecting user to [" + finalRedirect + "]");
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
        User user;
        try {
            Validations.checkNotEmpty(username, "Must specify a username");
            Validations.checkNotEmpty(service, "Must specify a valid service");
            Validations.checkNotEmpty(finalRedirect, "Must specify a valid final redirect URL");
            user = userManager.getUser(username);
            Validations.checkNotNull(user, "user with username [" + username + "] not found");
        } catch (Exception ex) {
            return error(ex.getMessage());
        }

        OAuthToken oAuthToken;
        try {
            oAuthToken = userManager.getOAuthToken(service, user.getUsername());
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
            return error(e, "Final redirect URL [" + finalRedirect + "] is ill-formed");
        }
        try {
            userManager.setUserFinalRedirect(user.getUsername(), finalRedirectUrl);
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

    @GET
    @Path("/oauth/atomic/callback/{service}/web/{redirect}")
    public Response handleAtomicOAuthCallbackWeb(
            @PathParam("service") String service,
            @PathParam("redirect") String finalRedirect,
            @QueryParam("oauth_token") String token,
            @QueryParam("oauth_verifier") String verifier,
            @QueryParam("error") String error,
            @QueryParam("denied") String denied
    ) {
        // check if the OAuth exchange is interrupted by the user
        if (denied != null || (error != null && error.equals("access_denied"))) {
            // facebook uses 'error' param, while twitter uses 'denied'
            try {
                return Response.temporaryRedirect(new URI(oAuthFailRedirect)).build();
            } catch (URISyntaxException e) {
                return error(e, "[" + oAuthFailRedirect + "] is not a well-formed URI - check your beancounter.properties file");
            }
        }
        String decodedFinalRedirect;
        try {
            decodedFinalRedirect = UriUtils.decodeBase64(finalRedirect);
        } catch (UnsupportedEncodingException uee) {
            return error(uee, "Error while decoding URL [" + finalRedirect + "]");
        }

        AtomicSignUp signUp;
        try {
            signUp = userManager.storeUserFromOAuth(service, token, verifier, decodedFinalRedirect);
        } catch (UserManagerException ume) {
            return error(ume, "Error while doing OAuth exchange for service: [" + service + "]");
        }
        LOGGER.info("grabbing first activities for user to [" + signUp.getUsername() + "]");
        try {
            grabInitialActivities(
                    signUp.getUsername(),
                    signUp.getService(),
                    signUp.getIdentifier(),
                    signUp.getUserId()
            );
        } catch (UserManagerException e) {
            final String errMsg = "Error while grabbing the first set of activities " +
                    "for user [" + signUp.getUsername() + "] on service [" + signUp.getService() + "]";
            LOGGER.error(errMsg, e);
            return error(e, errMsg);
        } catch (QueuesException e) {
            final String errMsg = "Error while pushing down the first set of activities " +
                    "for user [" + signUp.getUsername() + "] on service [" + signUp.getService() + "]";
            LOGGER.error(errMsg, e);
            return error(e, errMsg);
        }
        URI finalRedirectUri;
        try {
            finalRedirectUri = new URI(decodedFinalRedirect);
            if (finalRedirectUri.getQuery() == null) {
                finalRedirectUri = new URI(decodedFinalRedirect + "?username=" + signUp.getUsername() + "&token=" + signUp.getUserToken());
            } else {
                String[] paramsAndValue = finalRedirectUri.getQuery().split("&");
                for (String p : paramsAndValue) {
                    String[] param = p.split("=");
                    if (param[0].equals("username") || param[0].equals("token")) {
                        return error("[username] and [token] are reserved parameters");
                    }
                }
                finalRedirectUri = new URI(decodedFinalRedirect + "&username=" + signUp.getUsername() + "&token=" + signUp.getUserToken());
            }
        } catch (Exception ex) {
            return error(ex, "Malformed redirect URL");
        }
        return Response.temporaryRedirect(finalRedirectUri).build();
    }

    private void grabInitialActivities(String bcUsername, String service, String userIdOnService, UUID userId) throws UserManagerException, QueuesException {
        User user;
        try {
            user = userManager.getUser(bcUsername);
        } catch (UserManagerException e) {
            throw new UserManagerException("Error while retrieving user: [" + bcUsername + "]", e);
        }

        OAuthAuth auth = (OAuthAuth) user.getAuth(service);
        if (auth == null) {
            throw new UserManagerException("User [" + user.getUsername() + "] has no auth for service [" + service + "]");
        }
        if (auth.isExpired()) {
            throw new UserManagerException("OAuth token for [" + service + "] on user [" + user.getUsername() + "] has expired");
        }

        Callback<List<ResolvedActivity>> callback = new Callback<List<ResolvedActivity>>() {
            @Override
            public void complete(List<ResolvedActivity> result) {
                ObjectMapper mapper = new ObjectMapper();
                for (ResolvedActivity activity : result) {
                    String raJson;
                    try {
                        raJson = mapper.writeValueAsString(activity);
                    } catch (IOException e) {
                        // Just skip this one
                        LOGGER.warn("Error while serializing to JSON {}", activity);
                        continue;
                    }

                    try {
                        queues.push(raJson);
                    } catch (QueuesException e) {
                        LOGGER.error("Error while pushing down json " +
                                "resolved activity: [" + raJson + "] for user [" + activity.getUser().getUsername() + "] " +
                                "on service [" + activity.getActivity().getContext().getService() + "]", e);
                    }
                }
            }
        };

        if ("facebook".equals(service)) {
            grabberManager.submit(FacebookGrabber.create(user, userIdOnService), callback);
        } else if ("twitter".equals(service)) {
            grabberManager.submit(TwitterGrabber.create(user, userIdOnService), callback);
        } else {
            throw new UserManagerException("Service [" + service + "] is not supported");
        }
    }

    @GET
    @Path("/oauth/atomic/callback/facebook/web/{redirect}")
    public Response handleAtomicFacebookOAuthCallbackWeb(
            @PathParam("redirect") String finalRedirect,
            @QueryParam("code") String verifier,
            @QueryParam("error") String error
    ) {
        return handleAtomicOAuthCallbackWeb("facebook", finalRedirect, null, verifier, error, null);
    }

    @GET
    @Path("/oauth/atomic/callback/{service}/")
    public Response handleAtomicOAuthCallbackMobile(
            @PathParam("service") String service,
            @QueryParam("oauth_token") String token,
            @QueryParam("oauth_verifier") String verifier,
            @QueryParam("error") String error,
            @QueryParam("denied") String denied
    ) {
        // check if the OAuth exchange is interrupted by the user
        if (denied != null || (error != null && error.equals("access_denied"))) {
            // facebook uses error param, while twitter uses denied
            try {
                return Response.temporaryRedirect(new URI(oAuthFailRedirect)).build();
            } catch (URISyntaxException e) {
                return error(e, "[" + oAuthFailRedirect + "] is not a well-formed URI - check your beancounter.properties file");
            }
        }
        AtomicSignUp signUp;
        try {
            signUp = userManager.storeUserFromOAuth(service, token, verifier);
        } catch (UserManagerException ume) {
            return error(ume, "Error while doing OAuth exchange for service: [" + service + "]");
        }
        try {
            grabInitialActivities(
                    signUp.getUsername(),
                    signUp.getService(),
                    signUp.getIdentifier(),
                    signUp.getUserId()
            );
        } catch (UserManagerException e) {
            return error(e, "Error while grabbing the first set of activities " +
                    "for user [" + signUp.getUsername() + "] on service [" + signUp.getService() + "]");
        } catch (QueuesException e) {
            return error(e, "Error while grabbing the first set of activities " +
                    "for user [" + signUp.getUsername() + "] on service [" + signUp.getService() + "]");
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
    @Path("/oauth/atomic/callback/facebook/")
    public Response handleAtomicFacebookOAuthCallbackMobile(
            @QueryParam("code") String verifier,
            @QueryParam("error") String error
    ) {
        return handleAtomicOAuthCallbackMobile("facebook", null, verifier, error, null);
    }

    @GET
    @Path("/oauth/callback/facebook/{username}/")
    public Response handleFacebookAuthCallback(
            @PathParam("username") String username,
            @QueryParam("code") String verifier
    ) {
        try {
            Validations.checkNotEmpty(verifier, "Missing OAuth 2.0 code");
        } catch (Exception ex) {
            return error(ex.getMessage());
        }
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
        User user;
        try {
            Validations.checkNotEmpty(username, "Must specify a username");
            Validations.checkNotEmpty(service, "Must specify a valid service");
            Validations.checkNotEmpty(verifier, "Missing OAuth 1.0a verifier");
            user = userManager.getUser(username);
            Validations.checkNotNull(user, "user with username [" + username + "] not found");
        } catch (Exception ex) {
            return error(ex.getMessage());
        }
        AuthenticatedUser au;
        try {
            au = userManager.registerOAuthService(service, user, token, verifier);
        } catch (UserManagerException e) {
            return error(e, "Error while OAuth-like exchange for service: '" + service + "'");
        }
        user = au.getUser();

        try {
            grabInitialActivities(
                    username,
                    service,
                    au.getUserId(),
                    user.getId()
            );
        } catch (UserManagerException e) {
            return error(e, "Error while grabbing the first set of activities " +
                    "for user [" + username + "] on service [" + username + "]");
        } catch (QueuesException e) {
            return error(e, "Error while grabbing the first set of activities " +
                    "for user [" + username + "] on service [" + username + "]");
        }

        String finalRedirect;
        try {
            finalRedirect = userManager.consumeUserFinalRedirect(user.getUsername()).toString();
        } catch (UserManagerException e) {
            return error(e, "Error while getting final redirect URL for user '" + username + "' for service '" + service + "'");
        }

        URI finalRedirectUri;
        try {
            finalRedirectUri = new URI(finalRedirect);
            if (finalRedirectUri.getQuery() == null) {
                finalRedirectUri = new URI(finalRedirect + "?username=" + username + "&token=" + user.getUserToken());
            } else {
                String[] paramsAndValue = finalRedirectUri.getQuery().split("&");
                for (String p : paramsAndValue) {
                    String[] param = p.split("=");
                    if (param[0].equals("username") || param[0].equals("token")) {
                        return error("[username] and [token] are reserved parameters");
                    }
                }
                finalRedirectUri = new URI(finalRedirect + "&username=" + username + "&token=" + user.getUserToken());
            }
        } catch (Exception ex) {
            return error(ex, "Malformed redirect URL");
        }

        return Response.temporaryRedirect(finalRedirectUri).build();
    }

    @Deprecated
    @GET
    @Path("/auth/callback/{service}/{username}/{redirect}")
    public Response handleAuthCallback(
            @PathParam("service") String service,
            @PathParam("username") String username,
            @PathParam("redirect") String redirect,
            @QueryParam("token") String token
    ) {
        User user;
        try {
            Validations.checkNotEmpty(username, "Must specify a username");
            Validations.checkNotEmpty(service, "Must specify a valid service");
            Validations.checkNotEmpty(redirect, "Must specify a valid redirect URL");
            user = userManager.getUser(username);
            Validations.checkNotNull(user, "user with username [" + username + "] not found");
        } catch (Exception ex) {
            return error(ex.getMessage());
        }

        try {
            userManager.registerService(service, user, token);
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
        User user;
        try {
            Validations.checkNotEmpty(username, "Must specify a username");
            Validations.checkNotEmpty(service, "Must specify a valid service");
            Validations.checkNotEmpty(apiKey, "Missing api key");
            Validations.validateApiKey(apiKey, applicationsManager, UPDATE, USER);
            user = userManager.getUser(username);
            Validations.checkNotNull(user, "User [" + username + "] not found!");
        } catch (Exception ex) {
            return error(ex.getMessage());
        }

        try {
            userManager.deregisterService(service, user);
        } catch (UserManagerException e) {
            return error(e, "Error while retrieving user [" + username + "]");
        }

        return success("service [" + service + "] removed from user [" + username + "]");
    }

    @GET
    @Path("/{username}/profile")
    public Response getProfile(
            @PathParam(USERNAME) String username,
            @QueryParam(USER_TOKEN) String token
    ) {
        User user;
        try {
            Validations.checkNotEmpty(username, "Must specify a username");
            user = userManager.getUser(username);
            Validations.checkNotNull(user, "user with username [" + username + "] not found");
            Validations.validateUserToken(token, user, tokenManager);
        } catch (Exception ex) {
            return error(ex.getMessage());
        }

        UserProfile up;
        try {
            up = profiles.lookup(user.getId());
        } catch (ProfilesException e) {
            return error(e, "Error while retrieving profile for user [" + username + "]");
        }

        if (up == null) {
            return error("Profile for user [" + username + "] not found");
        }

        Response.ResponseBuilder rb = Response.ok();
        rb.entity(
                new UserProfilePlatformResponse(
                        UserProfilePlatformResponse.Status.OK,
                        "profile for user [" + username + "] found",
                        up
                )
        );
        return rb.build();
    }
}