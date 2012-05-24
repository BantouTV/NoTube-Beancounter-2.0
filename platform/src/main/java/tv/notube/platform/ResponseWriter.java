package tv.notube.platform;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Inject;
import org.joda.time.DateTime;
import tv.notube.commons.model.gson.DateTimeAdapter;
import tv.notube.platform.PlatformResponse;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
@Produces(MediaType.APPLICATION_JSON)
@Provider
public class ResponseWriter implements MessageBodyWriter<PlatformResponse> {

    public boolean isWriteable(
            Class<?> aClass,
            Type type,
            Annotation[] annotations,
            MediaType mediaType
    ) {
        return PlatformResponse.class.isAssignableFrom(aClass);
    }

    public long getSize(
            PlatformResponse platformResponse,
            Class<?> aClass,
            Type type,
            Annotation[] annotations,
            MediaType mediaType
    ) {
        return -1;
    }

    public void writeTo(
            PlatformResponse platformResponse,
            Class<?> aClass,
            Type type,
            Annotation[] annotations,
            MediaType mediaType,
            MultivaluedMap<String, Object> stringObjectMultivaluedMap,
            OutputStream outputStream
    ) throws IOException, WebApplicationException {
        GsonBuilder builder = new GsonBuilder();
        builder.excludeFieldsWithoutExposeAnnotation();
        builder.setPrettyPrinting();
        DateTimeAdapter dta = new DateTimeAdapter();
        builder.registerTypeAdapter(DateTime.class, dta);
        Gson gson = builder.create();
        String json = gson.toJson(platformResponse);
        BufferedOutputStream baos = new BufferedOutputStream(outputStream);
        baos.write(json.getBytes());
        baos.close();
    }
}