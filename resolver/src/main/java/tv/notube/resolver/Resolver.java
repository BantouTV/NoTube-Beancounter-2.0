package tv.notube.resolver;

import tv.notube.commons.model.activity.Activity;

import java.util.UUID;

/**
 * This class models the minumum behavior a component with
 * the responsibility of resolving user names must have.
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public interface Resolver {

    /**
     * Given an anonymous {@link Activity}, it will be able to give the
     * corresponding <i>beancounter.io</i> user identifier.
     *
     * @param activity
     * @return
     */
    public UUID resolve(Activity activity) throws ResolverException;

    /**
     * It stores for an user identifier, the username for a given service.
     *
     * @param username
     * @param service
     * @param userId
     * @throws ResolverException
     */
    public void store(String username, String service, UUID userId) throws ResolverException;

}
