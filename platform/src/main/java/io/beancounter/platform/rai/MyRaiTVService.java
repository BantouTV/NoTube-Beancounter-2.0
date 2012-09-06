package io.beancounter.platform.rai;

import com.google.inject.Inject;
import io.beancounter.commons.model.User;
import io.beancounter.commons.model.auth.SimpleAuth;
import io.beancounter.platform.JsonService;
import io.beancounter.platform.PlatformResponse;
import io.beancounter.platform.responses.AtomicSignUpResponse;
import io.beancounter.platform.responses.StringPlatformResponse;
import io.beancounter.usermanager.AtomicSignUp;
import io.beancounter.usermanager.UserManager;
import io.beancounter.usermanager.UserManagerException;
import io.beancounter.usermanager.UserTokenManager;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.UUID;

/**
 * This class is exclusively responsible to handle signup and login procedures
 * for <a href="http://rai.tv/">myRAI</a> logins. This class will be not part of the
 * official <a href="http://api.beancounter.io>beancounter API</a>.
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
@Path("rest/rai")
@Produces(MediaType.APPLICATION_JSON)
public class MyRaiTVService extends JsonService {

    private UserManager userManager;

    private UserTokenManager tokenManager;

    private MyRaiTVAuthHandler authHandler;

    private static final String SERVICE_NAME = "myRai";

    @Inject
    public MyRaiTVService(
            UserManager userManager,
            UserTokenManager tokenManager,
            MyRaiTVAuthHandler authHandler
    ) {
        this.userManager = userManager;
        this.tokenManager = tokenManager;
        this.authHandler = authHandler;
    }

    @POST
    @Path("/login")
    public Response login(
            @FormParam("username") String username,
            @FormParam("password") String password
    ) {
        String token;
        try {
            token = authHandler.authOnRai(username, password);
        } catch (IOException e) {
            return  error(e, "Error while authenticating [" + username + "] on myRai auth service");
        }
        if (token.equals("ko")) {
            Response.ResponseBuilder rb = Response.serverError();
            rb.entity(new StringPlatformResponse(
                    StringPlatformResponse.Status.NOK,
                    "user [" + username + "] is not authorized from myRai auth service")
            );
            return rb.build();
        }

        return loginWithAuth(username, token);
    }

    @POST
    @Path("/login/auth")
    public Response loginWithAuth(
            @FormParam("username") String username,
            @FormParam("token") String token

    ) {
        User user;
        try {
            user = userManager.getUser(username);
        } catch (UserManagerException e) {
            return error(e, "Error while getting beancounter.io user with name [" + username + "]");
        }
        // user is not there, hence must be registered
        if (user == null) {
            try {
                user = getNewUser(username, token);
                userManager.storeUser(user);
            } catch (UserManagerException e) {
                return error(e, "error while storing user [" + username + "] on beancounter.io");
            }
            AtomicSignUp signUp = new AtomicSignUp();
            signUp.setReturning(false);
            signUp.setService(SERVICE_NAME);
            signUp.setUserId(user.getId());
            signUp.setUsername(username);
            signUp.setIdentifier(username);
            Response.ResponseBuilder rb = Response.ok();
            rb.entity(
                    new AtomicSignUpResponse(
                            PlatformResponse.Status.OK,
                            "user with user name [" + signUp.getUsername() + "] logged in with service [" + signUp.getService() + "]",
                            signUp
                    )
            );
            return rb.build();
        } else {
            AtomicSignUp signUp = new AtomicSignUp();
            signUp.setReturning(true);
            signUp.setService(SERVICE_NAME);
            signUp.setUserId(user.getId());
            signUp.setUsername(username);
            signUp.setIdentifier(username);
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
    }

    private User getNewUser(String username, String token) throws UserManagerException {
        User user = new User();
        user.setUsername(username);
        user.addService(SERVICE_NAME, new SimpleAuth(token, username));
        UUID userToken = tokenManager.createUserToken(username);
        user.setUserToken(userToken);
        return user;
    }
}