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

}
