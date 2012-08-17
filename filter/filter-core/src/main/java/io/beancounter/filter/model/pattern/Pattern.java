package io.beancounter.filter.model.pattern;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public interface Pattern<T> {

    public boolean matches(T t);

}
