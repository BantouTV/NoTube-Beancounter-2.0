package tv.notube.commons.tests;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public interface Randomiser<T> {

    public Class<T> type();

    public String name();

    public T getRandom();

}
