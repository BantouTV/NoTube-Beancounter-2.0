package tv.notube.commons.model;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class Service implements Serializable {

    private static final long serialVersionUID = 4514345235L;

    private String name;

    private String description;

    private URL endpoint;

    private String apikey;

    private String secret;

    private String authRequest;

    private String sessionEndpoint;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public URL getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(URL endpoint) {
        this.endpoint = endpoint;
    }

    public String getApikey() {
        return apikey;
    }

    public void setApikey(String apikey) {
        this.apikey = apikey;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public URL getAuthRequest() {
        try {
            return new URL(String.format(authRequest, apikey));
        } catch (MalformedURLException e) {
            throw new RuntimeException("Malformed authorization URL '"  + authRequest + "'");
        }
    }

    public void setAuthRequest(String authRequest) {
        this.authRequest = authRequest;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Service)) return false;

        Service service = (Service) o;

        if (name != null ? !name.equals(service.name) : service.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    public void setSessionEndpoint(String sessionEndpoint) {
        this.sessionEndpoint = sessionEndpoint;
    }

    public String getSessionEndpoint() {
        return sessionEndpoint;
    }
}
