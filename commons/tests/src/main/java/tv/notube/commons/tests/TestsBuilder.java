package tv.notube.commons.tests;

import org.joda.time.DateTime;
import tv.notube.commons.tests.randomisers.*;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class TestsBuilder {

    private static TestsBuilder instance = new TestsBuilder();

    private Tests tests;

    private TestsBuilder() {
        tests = new Tests();
        tests.register(new StringRandomiser("string-randomizer", 2, 15, true));
        tests.register(new IntegerRandomiser("int-randomizer", 5, 10));
        tests.register(new DoubleRandomiser("double-randomizer", 5, 10));
        tests.register(new UUIDRandomiser("uuid-randomizer"));
        tests.register(new URLRandomiser("url-randomizer", true, true));
        tests.register(new JodaDateTimeRandomiser("dt-randomizer", DateTime.now().minusYears(1)));
        tests.register(new LongRandomiser("long-randomizer", 100000000L));
    }

    public static synchronized TestsBuilder getInstance() {
        if (instance == null) {
            instance = new TestsBuilder();
        }
        return instance;
    }

    public synchronized Tests build() {
        return tests;
    }
}