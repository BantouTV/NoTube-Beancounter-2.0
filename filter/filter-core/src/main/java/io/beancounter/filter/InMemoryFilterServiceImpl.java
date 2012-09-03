package io.beancounter.filter;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.beancounter.commons.model.activity.ResolvedActivity;
import io.beancounter.filter.manager.FilterManager;
import io.beancounter.filter.manager.FilterManagerException;
import io.beancounter.filter.model.Filter;

/**
 * In-memory, default implementation of {@link FilterService}.
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public final class InMemoryFilterServiceImpl implements FilterService {

    private static final Logger LOGGER = LoggerFactory.getLogger(InMemoryFilterServiceImpl.class);

    @Inject
    private FilterManager filterManager;

    private Set<Filter> filters = new HashSet<Filter>();

    @Override
    public synchronized void refresh() throws FilterServiceException {
        Collection<String> filters;
        try {
            filters = filterManager.getRegisteredFilters();
        } catch (FilterManagerException e) {
            final String errMsg = "Error while getting registered filters";
            LOGGER.error(errMsg, e);
            throw new FilterServiceException(errMsg, e);
        }
        for (String filter : filters) {
            Filter filterObj;
            try {
                filterObj = filterManager.get(filter);
            } catch (FilterManagerException e) {
                final String errMsg = "Error while getting filter [" + filter + "]";
                LOGGER.error(errMsg, e);
                throw new FilterServiceException(errMsg, e);
            }
            this.filters.add(filterObj);
        }
    }

    @Override
    public synchronized void refresh(String name) throws FilterServiceException {
        Filter filter;
        try {
            filter = filterManager.get(name);
        } catch (FilterManagerException e) {
            final String errMsg = "Error while getting filter [" + name + "]";
            LOGGER.error(errMsg, e);
            throw new FilterServiceException(errMsg, e);
        }
        if (filter == null) {
            return;
        }
        if (filter.isActive()) {
            filters.add(filter);
        } else {
            filters.remove(filter);
        }
    }

    @Override
    public Set<String> processActivity(ResolvedActivity resolvedActivity) {
        LOGGER.debug("processing activity {}", resolvedActivity);
        Set<String> result = new HashSet<String>();
        for (Filter filter : filters) {
            // TODO (high) this exception is thrown when we have multiple active filters
            // if thrown the activities filtered are lost
            // understand why this is happening
            try {
                if(filter.getActivityPattern().matches(resolvedActivity)) {
                    LOGGER.debug("activity {} filtered", resolvedActivity);
                    result.addAll(filter.getQueues());
                }
            } catch (Exception e) {
                LOGGER.error("Error while trying to match the activity with the filter [{}]", filter.getName(), e);
            }
        }
        return result;
    }
}
