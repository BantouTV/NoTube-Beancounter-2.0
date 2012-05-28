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

    @Random
    public FakeBean(String string, Integer integer) {
        this.string = string;
        this.integer = integer;
    }

    public String getString() {
        return string;
    }

    public int getInteger() {
        return integer;
    }
}
