package tv.notube.filter;

import java.util.Set;

import tv.notube.commons.model.activity.ResolvedActivity;

/**
 * This interface defines an engine able to execute {@link tv.notube.filter.model.Filter}.
 *
 * @author Bilgin Ibryam
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public interface FilterService {

    /**
     * Reloads all the started filters.
     *
     * @throws FilterServiceException
     * @see {@link tv.notube.filter.model.Filter}.
     */
    public void refresh() throws FilterServiceException;

    /**
     * Reloads in memory only the given filter.
     *
     * @param name
     * @throws FilterServiceException
     * @see {@link tv.notube.filter.model.Filter}.
     */
    public void refresh(String name) throws FilterServiceException;

    /**
     * It returns a set of addresses where the activity should be sent.
     *
     * @param resolvedActivity
     * @return
     * @throws FilterServiceException
     */
    public Set<String> processActivity(ResolvedActivity resolvedActivity)
            throws FilterServiceException;
}
