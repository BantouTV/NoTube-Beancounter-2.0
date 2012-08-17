package io.beancounter.resolver;

import io.beancounter.commons.model.activity.Activity;

import java.util.List;
import java.util.UUID;

/**
 * This class models the minumum behavior a component with
 * the responsibility of resolving user names must have.
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public interface
        Resolver {

    /**
     * Given an anonymous {@link Activity}, it will be able to give the
     * corresponding <i>beancounter.io</i> user identifier.
     *
     * @param activity
     * @return
     */
    public UUID resolve(Activity activity) throws ResolverException;

    /**
     * Given a service and a user identifier in that service, it returns
     * the <i>beancounter.io</i> user identifier.
     *
     * @param identifier
     * @param service
     * @return
     * @throws ResolverException
     */
    public UUID resolveId(String identifier, String service) throws ResolverException;

    /**
     * Given a service and a user identifier in that service, it returns
     * the <i>beancounter.io</i> user identifier.
     *
     * @param identifier
     * @param service
     * @return
     * @throws ResolverException
     */
    public String resolveUsername(String identifier, String service) throws ResolverException;

    /**
     * It stores for an user identifier, the username for a given service.
     *
     * @param identifier
     * @param service
     * @param userId
     * @param username
     * @throws ResolverException
     */
    public void store(
            String identifier,
            String service,
            UUID userId,
            String username
    ) throws ResolverException;


    /**
     *
     * @param serviceName
     * @param start the starting index inclusive
     * @param stop the ending index inclusive
     * @return list of userIds
     */
    public List<String> getUserIdsFor(String serviceName, int start, int stop)
            throws ResolverException;



}
