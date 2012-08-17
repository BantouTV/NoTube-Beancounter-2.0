package io.beancounter.listener.facebook.core.converter.custom;

import io.beancounter.commons.model.activity.Context;

/**
 * It defines the minimum contract of a Converter.
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public interface Converter<T, M extends io.beancounter.commons.model.activity.Object> {

    /**
     * It converts a response from Facebook into a <i>beancounter.io</i> model
     * object.
     *
     * @param t
     * @param isOpenGraph enables grabbing data from OpenGraph Protocol.
     * @return
     * @throws ConverterException
     */
    public M convert(T t, boolean isOpenGraph) throws ConverterException;

    /**
     * It wraps with a {@link Context} all the needed metadata coming from facebook.
     *
     *
     * @param t
     * @param userId
     * @return
     * @throws ConverterException
     */
    public Context getContext(T t, String userId) throws ConverterException;

}
