package tv.notube.crawler.requester.request;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.FacebookApi;
import org.scribe.model.*;
import org.scribe.oauth.OAuthService;
import tv.notube.commons.model.auth.OAuthAuth;
import tv.notube.crawler.requester.DefaultRequest;
import tv.notube.crawler.requester.RequestException;
import tv.notube.crawler.requester.ServiceResponse;
import tv.notube.crawler.requester.request.facebook.FacebookResponse;
import tv.notube.crawler.requester.request.facebook.FacebookResponseAdapter;

import java.io.IOException;
import java.io.InputStreamReader;

/**
 * <a href="http://facebook.com>facebook.com</a> specific implementation of
 * {@link tv.notube.crawler.requester.DefaultRequest}.
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class FacebookRequest extends DefaultRequest {

    // TODO: make it configuarable
    private static final String CALLBACK = "http://moth.notube.tv:9090/notube-platform/rest/user/oauth/callback/facebook/";

    private Gson gson;

    public FacebookRequest() {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(
                FacebookResponse.class,
                new FacebookResponseAdapter()
        );
        gson = builder.create();
    }

    /**
     *
     * @return
     * @throws tv.notube.crawler.requester.RequestException
     */
    public ServiceResponse call() throws RequestException {
        String serviceEndpoint = service.getEndpoint().toString();
        OAuthAuth oaa = (OAuthAuth) auth;

        OAuthService facebookOAuth = new ServiceBuilder()
                .provider(FacebookApi.class)
                .apiKey(service.getApikey())
                .apiSecret(service.getSecret())
                .scope("offline_access,user_likes")
                .callback(CALLBACK)
                .build();

        OAuthRequest request = new OAuthRequest(
                Verb.GET, serviceEndpoint);
        facebookOAuth.signRequest(
                new Token(oaa.getSession(), oaa.getSecret()),
                request
        );
        Response response = request.send();
        InputStreamReader reader = new InputStreamReader(response.getStream());
        try {
            return gson.fromJson(reader, FacebookResponse.class);
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                throw new RequestException("Error while closing reader", e);
            }
        }
    }

}
