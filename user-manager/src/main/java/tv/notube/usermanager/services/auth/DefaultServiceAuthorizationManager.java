package tv.notube.usermanager.services.auth;

import com.google.inject.Inject;
import tv.notube.commons.model.Service;
import tv.notube.commons.model.User;
import tv.notube.commons.model.auth.AuthHandler;
import tv.notube.commons.model.auth.AuthHandlerException;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Properties;

/**
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public final class DefaultServiceAuthorizationManager
        extends AbstractServiceAuthorizationManager {

    private static final String SERVICE = "service";

    @Inject
    public DefaultServiceAuthorizationManager(Map<Service, AuthHandler> authHandlers) {
        for (Map.Entry<Service, AuthHandler> authHandler : authHandlers.entrySet()) {
            handlers.put(authHandler.getKey(), authHandler.getValue());
        }
    }

    public static Service buildService(String service, Properties properties) {
        Service serviceObj = new Service(service);

        serviceObj.setDescription(
                property(properties, service, "description")
        );
        try {
            serviceObj.setEndpoint(
                    new URL(property(properties, service, "endpoint"))
            );
        } catch (MalformedURLException e) {
            throw new RuntimeException("service [" + service + "] endpoint is not a valid URL", e);
        }
        String sessionStr = property(properties, true, service, "session");
        if (sessionStr != null) {
            try {
                serviceObj.setSessionEndpoint(
                        new URL(sessionStr)
                );
            } catch (MalformedURLException e) {
                throw new RuntimeException("service [" + service + "] session endpoint is not a valid URL", e);
            }
        }
        serviceObj.setApikey(property(properties, service, "apikey"));
        serviceObj.setSecret(property(properties, service, "secret"));
        try {
            serviceObj.setOAuthCallback(
                    new URL(property(properties, service, "oauthcallback"))
            );
        } catch (MalformedURLException e) {
            throw new RuntimeException("service [" + service + "] oauth callback endpoint is not a valid URL", e);
        }
        try {
            serviceObj.setAtomicOAuthCallback(
                    new URL(property(properties, service, "atomicoauth"))
            );
        } catch (MalformedURLException e) {
            throw new RuntimeException("service [" + service + "] oauth callback endpoint is not a valid URL", e);
        }

        return serviceObj;
    }

    private static String property(Properties properties, String... names) {
        return property(properties, false, names);
    }

    private static String property(
            Properties properties,
            boolean optional,
            String... names
    ) {
        String key = SERVICE;
        for(String name : names) {
            key += "." + name;
        }
        String result = properties.getProperty(key);
        if(!optional && result == null) {
            throw new RuntimeException("["+ key + "] is null");
        }
        return result;
    }

    public User register(User user, String service, String token)
            throws ServiceAuthorizationManagerException {
        AuthHandler ah = getHandler(service);
        try {
            return ah.auth(user, token);
        } catch (AuthHandlerException e) {
            final String errMsg = "Error while authorizing user [" + user.getId() + "] to service [" + service + "]";
            throw new ServiceAuthorizationManagerException(errMsg, e);
        }
    }
}
