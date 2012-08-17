package io.beancounter.commons.model;

import java.net.URL;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class OAuthToken {

    private URL redirectPage;

    public OAuthToken(URL redirectPage) {
        this.redirectPage = redirectPage;
    }

    public URL getRedirectPage() {
        return redirectPage;
    }
}
