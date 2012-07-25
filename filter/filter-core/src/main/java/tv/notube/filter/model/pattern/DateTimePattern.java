package tv.notube.filter.model.pattern;

import org.joda.time.DateTime;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class DateTimePattern implements Pattern<DateTime> {

    public static final DateTimePattern ANY = new DateTimePattern(
            new DateTime(1L),
            Bool.AFTER
    );

    private DateTime date;

    private Bool bool;

    public enum Bool {
        BEFORE(1),
        EQUALS(0),
        AFTER(-1);
        private int value;
        private Bool(int value) {
            this.value = value;
        }
        public int value() {
            return value;
        }
    }

    public DateTimePattern() {}

    public DateTimePattern(DateTime date, Bool bool) {
        this.date = date;
        this.bool = bool;
    }

    public DateTime getDate() {
        return date;
    }

    public Bool getBool() {
        return bool;
    }

    public void setDate(DateTime date) {
        this.date = date;
    }

    public void setBool(Bool bool) {
        this.bool = bool;
    }

    @Override
    public boolean matches(DateTime dateTime) {
        long millis = dateTime.getMillis();
        return ((this.date.getMillis() - millis) / Math.abs(this.date.getMillis() - millis)) == this.bool.value() || this.equals(ANY);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DateTimePattern that = (DateTimePattern) o;

        if (bool != that.bool) return false;
        if (date != null ? !date.equals(that.date) : that.date != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = date != null ? date.hashCode() : 0;
        result = 31 * result + (bool != null ? bool.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "DateTimePattern{" +
                "date=" + date +
                ", bool=" + bool +
                '}';
    }
}
