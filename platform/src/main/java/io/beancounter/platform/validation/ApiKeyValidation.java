package io.beancounter.platform.validation;

import io.beancounter.applications.ApplicationsManager;
import io.beancounter.applications.ApplicationsManagerException;
import io.beancounter.platform.responses.StringPlatformResponse;

import javax.ws.rs.core.Response;
import java.util.Map;
import java.util.UUID;

import static io.beancounter.platform.Service.API_KEY;
import static io.beancounter.platform.Service.APPLICATION_ACTION;
import static io.beancounter.platform.Service.APPLICATION_OBJECT;

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
            return RequestValidator.error(e, "Your apikey is not well formed");
        }

        boolean isAuth;
        try {
            isAuth = applicationsManager.isAuthorized(
                    UUID.fromString(apiKey),
                    (ApplicationsManager.Action) params.get(APPLICATION_ACTION),
                    (ApplicationsManager.Object) params.get(APPLICATION_OBJECT)
            );
        } catch (ApplicationsManagerException e) {
            return RequestValidator.error(e, "Error while authorizing your application");
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
