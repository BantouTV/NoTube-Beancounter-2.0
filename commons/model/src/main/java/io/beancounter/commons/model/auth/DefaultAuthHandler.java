package io.beancounter.commons.model.auth;

import io.beancounter.commons.model.Service;

/**
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public abstract class DefaultAuthHandler implements AuthHandler {

    protected Service service;

    public DefaultAuthHandler(Service service) {
        this.service = service;
    }

}
