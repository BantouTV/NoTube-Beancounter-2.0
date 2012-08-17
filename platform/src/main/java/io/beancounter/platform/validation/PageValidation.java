package io.beancounter.platform.validation;

import javax.ws.rs.core.Response;
import java.util.Map;

import static io.beancounter.platform.Service.PAGE_NUMBER;
import static io.beancounter.platform.Service.PAGE_STRING;

public class PageValidation implements Validation {

    @Override
    public Response validate(Map<String, Object> params) {
        int page;
        String pageString = (String) params.get(PAGE_STRING);

        try {
            page = Integer.parseInt(pageString, 10);
        } catch (IllegalArgumentException e) {
            return RequestValidator.error(e, "Your page number is not well formed");
        }

        params.put(PAGE_NUMBER, page);

        return null;
    }
}
