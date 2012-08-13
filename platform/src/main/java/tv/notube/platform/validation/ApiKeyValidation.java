package tv.notube.platform.validation;

import tv.notube.applications.ApplicationsManager;
import tv.notube.applications.ApplicationsManagerException;
import tv.notube.platform.responses.StringPlatformResponse;

import javax.ws.rs.core.Response;
import java.util.Map;
import java.util.UUID;

import static tv.notube.platform.Service.API_KEY;
import static tv.notube.platform.Service.APPLICATION_ACTION;
import static tv.notube.platform.Service.APPLICATION_OBJECT;
import static tv.notube.platform.validation.RequestValidator.error;

public class ApiKeyValidation implements Validation {

    private ApplicationsManager applicationsManager;

    public ApiKeyValidation(ApplicationsManager applicationsManager) {
        this.applicationsManager = applicationsManager;
    }

    @Override
    public Response validate(Map<String, Object> params) {
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
                    (ApplicationsManager.Action) params.get(APPLICATION_ACTION),
                    (ApplicationsManager.Object) params.get(APPLICATION_OBJECT)
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

        return null;
    }
}
