package tv.notube.commons.model.activity;

import org.joda.time.DateTime;
import tv.notube.commons.model.activity.adapters.DateTimeAdapterJAXB;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.Serializable;
import java.lang.*;
import java.lang.Object;
import java.net.URL;

/**
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
@XmlRootElement
public class Context implements Serializable {

    private static final long serialVersionUID = 325277757335L;

    private DateTime date;

    private URL service;

    private String mood;

    public Context() {}

    public Context(DateTime d) {
        date = d;
    }

    @XmlJavaTypeAdapter(DateTimeAdapterJAXB.class)
    public DateTime getDate() {
        return date;
    }

    public void setDate(DateTime date) {
        this.date = date;
    }

    public URL getService() {
        return service;
    }

    public void setService(URL service) {
        this.service = service;
    }

    public String getMood() {
        return mood;
    }

    public void setMood(String mood) {
        this.mood = mood;
    }

    @Override
    public String toString() {
        return "Context{" +
                "date=" + date +
                ", service=" + service +
                ", mood='" + mood + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Context)) return false;

        Context context = (Context) o;

        if (date != null ? !date.equals(context.date) : context.date != null) return false;
        if (service != null ? !service.equals(context.service) : context.service != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = date != null ? date.hashCode() : 0;
        result = 31 * result + (service != null ? service.hashCode() : 0);
        return result;
    }
}
