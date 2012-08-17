package io.beancounter.listener.commons;

import io.beancounter.commons.model.activity.Activity;

import java.util.List;

/**
 * This interface defines the minimum behavior of a class responsible
 * of producing {@link Activity} from external Social stuff.
 *
 * @param <S>
 */
public interface ActivityConverter<S> {

    /**
     *
     * @param source
     * @return
     * @throws ActivityConverterException
     */
    public List<Activity> getActivities(S source)
            throws ActivityConverterException;
}
