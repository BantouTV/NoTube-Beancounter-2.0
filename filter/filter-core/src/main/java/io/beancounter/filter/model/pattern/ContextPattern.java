package io.beancounter.filter.model.pattern;

import org.joda.time.DateTime;
import io.beancounter.commons.model.activity.Context;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class ContextPattern implements Pattern<Context> {

    public static final ContextPattern ANY = new ContextPattern(
            DateTimePattern.ANY,
            StringPattern.ANY,
            StringPattern.ANY,
            StringPattern.ANY
    );

    private DateTimePattern date;

    private StringPattern service;

    private StringPattern mood;

    private StringPattern username;

    public ContextPattern() {
        date = DateTimePattern.ANY;
        service = StringPattern.ANY;
        mood = StringPattern.ANY;
        username = StringPattern.ANY;
    }

    public ContextPattern(
            DateTimePattern date,
            StringPattern service,
            StringPattern mood,
            StringPattern username
    ) {
        this.date = date;
        this.service = service;
        this.mood = mood;
        this.username = username;
    }

    public void setDate(DateTimePattern date) {
        this.date = date;
    }

    public DateTimePattern getDate() {
        return date;
    }

    public StringPattern getService() {
        return service;
    }

    public void setService(StringPattern service) {
        this.service = service;
    }

    public StringPattern getMood() {
        return mood;
    }

    public void setMood(StringPattern mood) {
        this.mood = mood;
    }

    public StringPattern getUsername() {
        return username;
    }

    public void setUsername(StringPattern username) {
        this.username = username;
    }

    @Override
    public boolean matches(Context context) {
        DateTime date = context.getDate();
        String service = context.getService();
        String mood = context.getMood();
        String username = context.getUsername();
        return this.getDate().matches(date)
                && this.getService().matches(service)
                && this.getMood().matches(mood)
                && this.getUsername().matches(username);
    }
}
