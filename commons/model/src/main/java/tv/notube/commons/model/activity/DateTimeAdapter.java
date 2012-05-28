package tv.notube.commons.model.activity;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.IOException;
import java.util.Date;

/**
 * This class is needed to serialize org.joda.time.DateTime
 *
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */

public class DateTimeAdapter extends JsonSerializer<DateTime> {

    private static DateTimeFormatter formatter = DateTimeFormat.forPattern("dd-MM-yyyy");

    @Override
    public void serialize(DateTime dateTime, JsonGenerator gen,
                          SerializerProvider ser)
            throws IOException, JsonProcessingException {
        gen.writeString(formatter.print(dateTime));
    }


    /*
    *
    * This should extends XmlAdapter<Date, DateTime>
    *


    @Override
    public DateTime unmarshal(Date date) throws Exception {
        return new DateTime(date.getTime());
    }

    @Override
    public Date marshal(DateTime dateTime) throws Exception {
        return new Date(dateTime.getMillis());
    }

    */

}