package tv.notube.commons.model.activity.adapters;

import org.joda.time.DateTime;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
public class DateTimeAdapterJAXB extends XmlAdapter<Long, DateTime> {

    @Override
    public DateTime unmarshal(Long l) throws Exception {
        return new DateTime(l);
    }

    @Override
    public Long marshal(DateTime dateTime) throws Exception {
        return dateTime.getMillis();
    }
}