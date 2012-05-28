package tv.notube.commons.tests;

import tv.notube.commons.tests.annotations.Random;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class FakeBean {

    private String string;

    private int integer;

    private FakePoint fakePoint;

    @Random(names = {"string", "integer", "fakePoint"})
    public FakeBean(String string, Integer integer, FakePoint fakePoint) {
        this.string = string;
        this.integer = integer;
        this.fakePoint = fakePoint;
    }

    public String getString() {
        return string;
    }

    public int getInteger() {
        return integer;
    }

    public FakePoint getFakePoint() {
        return fakePoint;
    }
}
