package io.beancounter.commons.model;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public interface Mergeable<T extends Topic> {

    public T merge(T nu, T old, int threshold);

}
