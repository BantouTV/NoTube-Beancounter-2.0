package io.beancounter.commons.model;

import org.codehaus.jackson.annotate.JsonIgnore;

/**
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
public abstract class PrivateUser {
    @JsonIgnore abstract String getPassword();
}