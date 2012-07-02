package tv.notube.platform;

import tv.notube.platform.responses.ReportPlatformResponse;

import javax.ws.rs.ext.Provider;

/**
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
@Provider
public class GenericExceptionMapper extends BaseExceptionMapper<RuntimeException> {

    public javax.ws.rs.core.Response toResponse(RuntimeException re) {
        return javax.ws.rs.core.Response.status(javax.ws.rs.core.Response.Status.BAD_REQUEST)
            .entity( new ReportPlatformResponse(ReportPlatformResponse.Status.NOK,
                    getErrorMessage(re), null) )
            .build();
    }

}