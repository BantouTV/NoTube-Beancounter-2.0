package tv.notube.activities;

import org.joda.time.DateTime;
import tv.notube.commons.model.activity.Activity;

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
    public void store(final UUID userId, final Activity activity) throws ActivityStoreException;

    /**
     * Stores a bulk of {@link Activity} committing them all.
     *
     * @param userId
     * @param activities
     * @throws ActivityStoreException
     */
    public void store(final UUID userId, final Collection<Activity> activities) throws ActivityStoreException;

    /**
     *
     * @param uuidId
     * @param max
     * @return
     * @throws ActivityStoreException
     */
    public Collection<Activity> getByUser(final UUID uuidId, final int max) throws ActivityStoreException;

    /**
     *
     * @param uuid
     * @param from
     * @param to
     * @return
     * @throws ActivityStoreException
     */
    public Collection<Activity> getByUserAndDateRange(
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
    public Map<UUID, Collection<Activity>> getByDateRange(
            final DateTime from,
            final DateTime to
    ) throws ActivityStoreException;

    /**
     *
     * @param userId
     * @return
     * @throws ActivityStoreException
     */
    public Collection<Activity> getByUser(
            final UUID userId
    ) throws ActivityStoreException;

    /**
     *
     * @param userId
     * @param activityId
     * @return
     * @throws ActivityStoreException
     */
    public Activity getByUser(
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
    public Collection<Activity> getByUser(
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
     * @return <code>size</code> activities relating to the specified page.
     * @throws ActivityStoreException Thrown if something goes wrong when
     * converting the JSON search results to Activity objects.
     */
    public Collection<Activity> getByUserPaginated(
            final UUID userId,
            final int pageNumber,
            final int size
    ) throws ActivityStoreException;

    /**
     * Releases any used resources.
     *
     * @throws ActivityStoreException
     */
    public void shutDown() throws ActivityStoreException;

    /**
     * Performs an exact match of the <i>value</i> parameter against the given
     * JSON path. Wildcard searches are not allowed.
     *
     * @param path The JSON path to match the value to.
     * @param value The value to search for.
     * @return Zero or more activities which are the results of executing the
     * search.
     * @throws ActivityStoreException Thrown if something goes wrong when
     * converting the JSON search results to Activity objects.
     * @throws WildcardSearchException Thrown if a wildcard search is attempted.
     */
    public Collection<Activity> search(
            String path,
            String value
    ) throws ActivityStoreException, WildcardSearchException;
}
