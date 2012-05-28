package tv.notube.commons.model.activity;

import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Date;

/**
 * This class is needed to serialize org.joda.time.DateTime
 *
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */

public class DateTimeAdapter implements JsonSerializer<DateTime> {

    private static DateTimeFormatter formatter = DateTimeFormat.forPattern("dd-MM-yyyy");

    /*
    @Override
    public void serialize(DateTime dateTime, JsonGenerator gen,
                          SerializerProvider ser)
            throws IOException, JsonProcessingException {
        gen.writeString(formatter.print(dateTime));
    }
    */

    @Override
    public JsonElement serialize(DateTime dateTime, Type type, JsonSerializationContext jsonSerializationContext) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}