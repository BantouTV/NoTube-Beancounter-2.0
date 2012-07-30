package tv.notube.usermanager.services.auth;

import tv.notube.commons.model.Service;
import tv.notube.commons.model.auth.AuthHandler;
import tv.notube.commons.model.auth.AuthHandlerException;
import tv.notube.commons.model.User;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

/**
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public final class DefaultServiceAuthorizationManager
        extends AbstractServiceAuthorizationManager {

    private static final String SERVICE = "service";

    public static ServiceAuthorizationManager build(Properties properties) {
        ServiceAuthorizationManager sam = new DefaultServiceAuthorizationManager();
        String declaredServices = properties.getProperty("services");
        if(declaredServices == null) {
            throw new RuntimeException("your sam.properties does not declare any service");
        }
        String[] services = declaredServices.split(",");
        for(String service : services) {
            service = service.trim();
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
            try {
                sam.addHandler(
                        serviceObj,
                        buildHandler(serviceObj, property(
                                properties,
                                service,
                                "handler")
                        )
                );
            } catch (ServiceAuthorizationManagerException e) {
                throw new RuntimeException("Error while adding service [" + service + "]", e);
            }
        }
        return sam;
    }

    private static String property(Properties properties, String... names) {
        return property(properties, false, names);
    }

    private static AuthHandler buildHandler(Service service, String name) {
        Class<? extends AuthHandler> handlerClass;
        try {
            handlerClass = (Class<? extends AuthHandler>) Class.forName(name);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("could not find [" + name + "] auth handler", e);
        }
        Constructor<? extends AuthHandler> constructor;
        try {
            constructor = handlerClass.getConstructor(Service.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("could not find [" + name + "] auth handler", e);
        }
        try {
            return constructor.newInstance(service);
        } catch (InstantiationException e) {
            throw new RuntimeException("Error while instantiating [" + name + "] auth handler", e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Error while instantiating [" + name + "] auth handler", e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException("Error while instantiating [" + name + "] auth handler", e);
        }
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
