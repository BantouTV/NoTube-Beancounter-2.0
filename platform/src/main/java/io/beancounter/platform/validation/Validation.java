package io.beancounter.platform.validation;

import javax.ws.rs.core.Response;
import java.util.Map;

public interface Validation {

    Response validate(Map<String, Object> params);
}
