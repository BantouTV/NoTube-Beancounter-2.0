package tv.notube.filter;

import tv.notube.filter.model.Filter;
import tv.notube.filter.model.pattern.ActivityPattern;

import java.util.*;

/**
 * In memory implementation of {@link FilterManager}.
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class InMemoryFilterManager implements FilterManager {

    private Set<Filter> filters = new HashSet<Filter>();

    @Override
    public synchronized String register(
            String name,
            String description,
            ActivityPattern activityPattern
    ) throws FilterManagerException {
        Filter filter = new Filter(
                name,
                description,
                activityPattern
        );
        filters.add(filter);
        return name;
    }

    @Override
    public Filter get(String name) throws FilterManagerException {
        for(Filter filter : filters) {
            if(filter.getName().equals(name)) {
                return filter;
            }
        }
        throw new FilterManagerException("Filter with id [" + name + "] has not been found");
    }

    @Override
    public void delete(String name) throws FilterManagerException {
        Filter filter = get(name);
        filters.remove(filter);
    }

    @Override
    public synchronized void start(String name) throws FilterManagerException {
        Filter filter = get(name);
        filter.setActive(true);
        filters.remove(filter);
        filters.add(filter);
    }

    @Override
    public void stop(String name) throws FilterManagerException {
        Filter filter = get(name);
        filter.setActive(false);
        filters.remove(filter);
        filters.add(filter);
    }

    @Override
    public synchronized void update(Filter filter) throws FilterManagerException {
        filters.remove(filter.getName());
        filters.add(filter);
    }

    @Override
    public Collection<String> getRegisteredFilters() throws
            FilterManagerException {
        Collection<String> names = new ArrayList<String>();
        for(Filter filter : filters) {
            names.add(filter.getName());
        }
        return names;
    }
}
