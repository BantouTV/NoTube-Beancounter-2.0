package io.beancounter.commons.model.auth;

import org.codehaus.jackson.annotate.JsonIgnore;

/**
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
public abstract class PrivateOAuth {
    @JsonIgnore abstract String getSecret();
    @JsonIgnore abstract String getSession();
}