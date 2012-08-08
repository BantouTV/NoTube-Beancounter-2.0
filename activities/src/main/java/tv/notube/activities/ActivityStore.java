package tv.notube.activities;

import org.joda.time.DateTime;
import tv.notube.commons.model.activity.Activity;
import tv.notube.commons.model.activity.ResolvedActivity;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

/**
 * This interface represents the minimum contract that every class able to do
 * <i>CRUD</i> on {@link Activity} must implement.
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public interface ActivityStore {

    /**
     * Store a single {@link Activity}, committing it immediately.
     *
     * @param userId
     * @param activity
     * @throws ActivityStoreException
     */
    void store(final UUID userId, final ResolvedActivity activity) throws ActivityStoreException;

    /**
     * Stores a bulk of {@link Activity} committing them all.
     *
     * @param userId
     * @param activities
     * @throws ActivityStoreException
     */
    void store(final UUID userId, final Collection<ResolvedActivity> activities) throws ActivityStoreException;

    /**
     *
     * @param uuidId
     * @param max
     * @return
     * @throws ActivityStoreException
     */
    Collection<ResolvedActivity> getByUser(final UUID uuidId, final int max) throws ActivityStoreException;

    /**
     *
     * @param uuid
     * @param from
     * @param to
     * @return
     * @throws ActivityStoreException
     */
    Collection<ResolvedActivity> getByUserAndDateRange(
            final UUID uuid,
            final DateTime from,
            final DateTime to
    ) throws ActivityStoreException;

    /**
     *
     * @param from
     * @param to
     * @return
     * @throws ActivityStoreException
     */
    Map<UUID, Collection<ResolvedActivity>> getByDateRange(
            final DateTime from,
            final DateTime to
    ) throws ActivityStoreException;

    /**
     *
     * @param userId
     * @param activityId
     * @return
     * @throws ActivityStoreException
     */
    ResolvedActivity getByUser(
            final UUID userId,
            final UUID activityId
    ) throws ActivityStoreException;

    /**
     *
     * @param userId
     * @param activityIds
     * @return
     * @throws ActivityStoreException
     */
    Collection<ResolvedActivity> getByUser(
            final UUID userId,
            final Collection<UUID> activityIds
    ) throws ActivityStoreException;

    /**
     * Retrieves a user's activities in a paginated format.
     * Page numbering begins at 0 (for the first page).
     *
     * @param userId The id of the user whose activities should be returned.
     * @param pageNumber The number of the page of results to return.
     * @param size The number of results displayed on each page.
     * @param order The order in which the results should be given. Either "asc"
     * for ascending (earliest activities first) or "desc" for descending
     * (latest activities first).
     * @return <code>size</code> activities relating to the specified page.
     * @throws ActivityStoreException Thrown if something goes wrong when
     * converting the JSON search results to Activity objects.
     * @throws InvalidOrderException When the specified order is invalid (ie.
     * not "asc" or "desc").
     */
    Collection<ResolvedActivity> getByUserPaginated(
            UUID userId,
            int pageNumber,
            int size,
            String order
    ) throws ActivityStoreException, InvalidOrderException;

    /**
     * Performs an exact match of the <i>value</i> parameter against the given
     * JSON path and provides results in a paginated format. Wildcard searches
     * are not allowed.
     *
     * @param path The JSON path to match the value to.
     * @param value The value to search for.
     * @param pageNumber The number of the page of results to return.
     * @param size The number of results displayed on each page.
     * @param order The order in which the results should be given. Either "asc"
     * for ascending (earliest activities first) or "desc" for descending
     * (latest activities first).
     * @return Zero or more activities which are the results of executing the
     * search.
     * @throws ActivityStoreException Thrown if something goes wrong when
     * converting the JSON search results to Activity objects.
     * @throws WildcardSearchException Thrown if a wildcard search is attempted.
     * @throws InvalidOrderException When the specified order is invalid (ie.
     * not "asc" or "desc").
     */
    Collection<ResolvedActivity> search(
            String path,
            String value,
            int pageNumber,
            int size,
            String order
    ) throws ActivityStoreException, WildcardSearchException, InvalidOrderException;

    /**
     * This method sets an internal {@link Activity} <code>boolean</code> flag to make
     * it no longer visible (or to make it visible) in all the other search methods.
     *
     * @param activityId
     * @param visible
     * @throws ActivityStoreException
     */
    void setVisible(final UUID activityId, boolean visible) throws ActivityStoreException;

    /**
     * Releases any used resources.
     *
     * @throws ActivityStoreException
     */
    void shutDown() throws ActivityStoreException;
}
