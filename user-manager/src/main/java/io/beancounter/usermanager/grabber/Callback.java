package io.beancounter.usermanager.grabber;

/**
 * @author Alex Cowell
 */
public interface Callback<E> {

    void complete(E result);
}
