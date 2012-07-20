package tv.notube.listener.facebook.converter;

/**
 * It defines the minimum contract of a Converter.
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public interface Converter<T, M> {

    /**
     * @param t
     * @param isOpenGraph enables grabbing data from OpenGraph Protocol.
     * @return
     * @throws ConverterException
     */
    public M convert(T t, boolean isOpenGraph) throws ConverterException;

}
