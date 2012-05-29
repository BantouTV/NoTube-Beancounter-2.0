package tv.notube.commons.tests;

import org.joda.time.DateTime;
import tv.notube.commons.tests.randomisers.*;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class TestsBuilder {

    public static Tests build() {
        Tests tests = new Tests();
        tests.register(new StringRandomiser("string-randomizer", 2, 15, false));
        tests.register(new IntegerRandomiser("int-randomizer", 5, 10));
        tests.register(new DoubleRandomiser("double-randomizer", 5, 10));
        tests.register(new UUIDRandomiser("uuid-randomizer"));
        tests.register(new URLRandomiser("url-randomizer", true, true));
        tests.register(new JodaDateTimeRandomiser("dt-randomiser", DateTime.now().minusYears(1)));
        return tests;
    }

}