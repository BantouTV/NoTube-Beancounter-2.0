package tv.notube.commons.model.activity.adapters;

import org.joda.time.DateTime;
import tv.notube.commons.model.activity.bbc.BBCGenre;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.List;

/**
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
public class DateTimeAdapterJAXB extends XmlAdapter<String, DateTime> {

    @Override
    public DateTime unmarshal(String s) throws Exception {
        return new DateTime(s);
    }

    @Override
    public String marshal(DateTime dateTime) throws Exception {
        return dateTime.toString();
    }
}