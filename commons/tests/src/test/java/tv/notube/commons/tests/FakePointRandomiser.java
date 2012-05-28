package tv.notube.commons.tests;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class FakePointRandomiser implements Randomiser<FakePoint> {

    private String name;

    public FakePointRandomiser(String name) {
        this.name = name;
    }

    public Class<FakePoint> type() {
        return FakePoint.class;
    }

    public String name() {
        return name;
    }

    public FakePoint getRandom() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
