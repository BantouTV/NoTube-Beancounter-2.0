package io.beancounter.platform.validation;

import javax.ws.rs.core.Response;
import java.util.Map;
import java.util.UUID;

import static io.beancounter.platform.Service.ACTIVITY_ID;
import static io.beancounter.platform.Service.ACTIVITY_ID_OBJ;

public class ActivityIdValidation implements Validation {

    @Override
    public Response validate(Map<String, Object> params) {
        String activityId = (String) params.get(ACTIVITY_ID);
        UUID activityIdObj;

        try {
            activityIdObj = UUID.fromString(activityId);
        } catch (IllegalArgumentException e) {
            return RequestValidator.error(e, "Your activityId is not well formed");
        }

        params.put(ACTIVITY_ID_OBJ, activityIdObj);

        return null;
    }
}
