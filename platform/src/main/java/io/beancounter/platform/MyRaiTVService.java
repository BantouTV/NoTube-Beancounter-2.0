package io.beancounter.platform;

import com.google.inject.Inject;
import io.beancounter.applications.ApplicationsManager;
import io.beancounter.commons.model.User;
import io.beancounter.commons.model.auth.SimpleAuth;
import io.beancounter.platform.responses.AtomicSignUpResponse;
import io.beancounter.platform.responses.StringPlatformResponse;
import io.beancounter.platform.validation.RequestValidator;
import io.beancounter.platform.validation.UsernameValidation;
import io.beancounter.usermanager.AtomicSignUp;
import io.beancounter.usermanager.UserManager;
import io.beancounter.usermanager.UserManagerException;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;

import static io.beancounter.platform.validation.RequestValidator.createParams;

/**
 * This class is exclusively responsible to handle signup and login procedures
 * for <a href="http://rai.tv/>myRAI</a> logins. This class will be not part of the
 * official <a href="http://api.beancounter.io>beancounter API</a>.
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
@Path("rest/rai")
@Produces(MediaType.APPLICATION_JSON)
public class MyRaiTVService extends JsonService {

    private static String SERVICE_PATTERN = "http://www.rai.tv/MyRaiTv/login.do?username=%s&password=%s";

    private RequestValidator validator;

    private UserManager userManager;

    private static final String  SERVICE_NAME = "myRai" ;

    @Inject
    public MyRaiTVService(
            final UserManager um
    ) {
        this.userManager = um;
        validator = new RequestValidator();
        validator.addValidation(USERNAME, new UsernameValidation(userManager));
    }

    /**
     * @return
     */
    @POST
    @Path("/login")
    public Response login(
            @FormParam("username") String username,
            @FormParam("password") String password
    ) {
        String token;
        try {
            token = authOnRai(username, password);
        } catch (IOException e) {
            return  error(e, "Error while authenticating [" + username + "] on myRai auth service");
        }
        if(token.equals("ko")) {
            Response.ResponseBuilder rb = Response.serverError();
            rb.entity(new StringPlatformResponse(
                    StringPlatformResponse.Status.NOK,
                    "user [" + username + "] is not authorized from myRai auth service")
            );
            return rb.build();
        }
        User user;
        try {
            user = userManager.getUser(username);
        } catch (UserManagerException e) {
            return error(e, "Error while getting beancounter.io user with name [" + username + "]");
        }
        // user is not there, hence must be registered
        if (user == null) {
            user = getNewUser(username, token);
            try {
                userManager.storeUser(user);
            } catch (UserManagerException e) {
                return error(e, "error while storing user [" + username + "] on beancounter");
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

/**
     * @return
     */
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
            return error(e, "");
        }
        // user is not there, hence must be registered
        if (user == null) {
            user = getNewUser(username, token);
            try {
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

    private User getNewUser(String username, String token) {
        User user = new User();
        user.setUsername(username);
        user.addService(SERVICE_NAME, new SimpleAuth(token, username));
        return user;
    }

    private String authOnRai(String username, String password) throws IOException {
        URL url = new URL(
                String.format(SERVICE_PATTERN, username, password)
        );
        URLConnection urlConnection = url.openConnection();
        InputStream is = urlConnection.getInputStream();
        String response;
        try {
            response = new java.util.Scanner(is).useDelimiter("\\A").next();
        } finally {
            is.close();
        }
        String splitResponse[] = response.split("-");
        if(splitResponse.length != 2) {
            return "ko";
        }
        return splitResponse[0];
    }

}