package tv.notube.platform.validation;

import tv.notube.applications.ApplicationsManager;
import tv.notube.platform.Service;
import tv.notube.platform.ServiceException;
import tv.notube.platform.responses.StringPlatformResponse;
import tv.notube.usermanager.UserManager;

import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import static tv.notube.platform.Service.*;

/**
 * Validates REST calls and also resolves any required parameters.
 */
public class RequestValidator {

    private Map<String, Validation> validations;

    public RequestValidator() {
        validations = new HashMap<String, Validation>();
    }

    public Response validateRequest(
            Class<? extends Service> serviceClass,
            String methodName,
            ApplicationsManager.Action action,
            ApplicationsManager.Object object,
            Map<String, Object> params
    ) {
        try {
            check(
                    serviceClass,
                    methodName,
                    params.values().toArray()
            );
        } catch (ServiceException e) {
            return error(e, "Error while checking parameters");
        }

        Collection<String> paramsToValidate = new LinkedList<String>(params.keySet());

        params.put(APPLICATION_ACTION, action);
        params.put(APPLICATION_OBJECT, object);

        for (String param : paramsToValidate) {
            if (validations.containsKey(param)) {
                Response error = validations.get(param).validate(params);
                if (error != null) {
                    return error;
                }
            }
        }

        return null;
    }

    public void addValidation(String param, Validation validation) {
        validations.put(param, validation);
    }

    public void removeValidation(String param) {
        validations.remove(param);
    }

    public static Response error(
            Exception e,
            String message
    ) {
        Response.ResponseBuilder rb = Response.serverError();
        rb.entity(
                new StringPlatformResponse(
                        StringPlatformResponse.Status.NOK,
                        message,
                        e.getMessage()
                )
        );
        return rb.build();
    }

    public static Map<String, Object> createParams(Object... objects) {
        Map<String, Object> params = new LinkedHashMap<String, Object>();

        for (int i = 0; i < objects.length - 1; i += 2) {
            params.put(String.valueOf(objects[i]), objects[i + 1]);
        }

        return params;
    }
}
