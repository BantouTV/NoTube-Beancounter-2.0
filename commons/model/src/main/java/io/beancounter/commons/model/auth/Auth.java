package io.beancounter.commons.model.auth;

import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonTypeInfo;

import java.io.Serializable;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
@JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include=JsonTypeInfo.As.PROPERTY, property="type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = OAuthAuth.class, name = "OAuth"),
        @JsonSubTypes.Type(value = SimpleAuth.class, name = "SimpleAuth")
})
public abstract class Auth implements Serializable {

    private static final long serialVersionUID = 11251145235L;

    private String session;

    public Auth() {}

    public Auth(String session) {
        this.session = session;
    }

    public String getSession() {
        return session;
    }

    public void setSession(String session) {
        this.session = session;
    }

    @Override
    public String toString() {
        return "Auth{" +
                "session='" + session + '\'' +
                '}';
    }
}
