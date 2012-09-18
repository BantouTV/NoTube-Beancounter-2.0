package io.beancounter.applications;

import io.beancounter.applications.model.Application;

import java.net.URL;
import java.util.UUID;

/**
 * This interface models the minimum behavior of a class
 * responsible to manage all the permissions of an application that
 * would be able to interact with the <i>beancounter.io</i> APIs.
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public interface ApplicationsManager {

    /**
     * This enumerates all the possible object types an {@link Action} could
     * be done on it.
     */
    public enum Object {
        PROFILE,
        ACTIVITIES,
        USER,
        FILTER,
    }

    /**
     * This represents the ownership of an object. The ownership is granted if
     * an application creates an object.
     */
    public enum Ownership {
        OWN,
        OTHER
    }

    /**
     * A possible action to be done on a {@link Object}
     */
    public enum Action {
        CREATE,
        RETRIEVE,
        UPDATE,
        DELETE
    }

    public UUID registerApplication(
            String name,
            String description,
            String email,
            URL callback
    ) throws ApplicationsManagerException;

    public boolean deregisterApplication(UUID key) throws ApplicationsManagerException;

    /**
     * Returns an {@link Application} by its key.
     *
     * @param key
     * @return
     * @throws ApplicationsManagerException
     */
    public Application getApplicationByApiKey(UUID key) throws ApplicationsManagerException;

    /**
     * Returns <code>true</code> if an application identified with the key
     * parameter, could perform the input action on that object type.
     *
     * @param key
     * @param action
     * @param object
     * @return
     * @throws ApplicationsManagerException
     */
    public boolean isAuthorized(
            UUID key,
            Action action,
            Object object
    ) throws ApplicationsManagerException;
}
