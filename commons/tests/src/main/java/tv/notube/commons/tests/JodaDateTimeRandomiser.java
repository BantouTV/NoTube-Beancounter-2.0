package tv.notube.commons.tests;

import org.joda.time.DateTime;

import java.util.Random;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class JodaDateTimeRandomiser extends DefaultRandomiser<DateTime> {

    private Random random = new Random();

    private DateTime from;

    private DateTime to;

    public JodaDateTimeRandomiser(String name, DateTime from, DateTime to) {
        super(name);
        this.from = from;
        this.to = to;
    }

    public JodaDateTimeRandomiser(String name, DateTime from) {
        super(name);
        this.from = from;
        this.to = DateTime.now();
    }

    public Class<DateTime> type() {
        return DateTime.class;
    }

    public DateTime getRandom() {
        long millisec = (long) (from.getMillis() + ((random.nextDouble() * (to.getMillis() - from.getMillis()))));
        return new DateTime(millisec);
    }
}
