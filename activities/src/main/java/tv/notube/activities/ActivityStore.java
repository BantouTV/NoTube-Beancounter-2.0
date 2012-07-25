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
     * It performs an exact match of <i>value</i> parameter against the given
     * json path.
     *
     * @param path
     * @throws ActivityStoreException
     */
    public Collection<Activity>  search(String path, String value) throws ActivityStoreException;

    // TODO (high) remove as soon as #search is ready
    public Collection<Activity> getByOnEvent(String value) throws ActivityStoreException;
}
