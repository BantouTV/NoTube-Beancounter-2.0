package tv.notube.platform.validation;

import tv.notube.commons.model.User;
import tv.notube.platform.responses.StringPlatformResponse;
import tv.notube.usermanager.UserManager;
import tv.notube.usermanager.UserManagerException;

import javax.ws.rs.core.Response;
import java.util.Map;

import static tv.notube.platform.Service.USER;
import static tv.notube.platform.Service.USERNAME;
import static tv.notube.platform.validation.RequestValidator.error;

public class UsernameValidation implements Validation {

    private UserManager userManager;

    public UsernameValidation(UserManager userManager) {
        this.userManager = userManager;
    }

    @Override
    public Response validate(Map<String, Object> params) {
        String username = (String) params.get(USERNAME);
        User user;

        try {
            user = userManager.getUser(username);
        } catch (UserManagerException e) {
            final String errMsg = "Error while retrieving user [" + username + "]";
            return error(e, errMsg);
        }

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

        return null;
    }
}
