package tv.notube.usermanager.services.auth.lastfm.handlers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.log4j.Logger;
import tv.notube.usermanager.services.auth.lastfm.LastFmResponse;
import tv.notube.usermanager.services.auth.lastfm.handlers.gson.LastFmResponseAdapter;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class LastFmResponseHandler implements ResponseHandler<LastFmResponse> {

    private static Logger logger = Logger.getLogger(LastFmResponseHandler.class);

    private Gson gson;

    public LastFmResponseHandler() {
        GsonBuilder builder = new GsonBuilder();
        LastFmResponseAdapter adapter = new LastFmResponseAdapter();
        builder.registerTypeAdapter(LastFmResponse.class, adapter);
        gson = builder.create();
    }

    public LastFmResponse handleResponse(HttpResponse httpResponse)
            throws ClientProtocolException, IOException {
        if(httpResponse.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
            final String errMsg = "Unexpected reply from Lastfm: HTTP CODE: '"
                    + httpResponse.getStatusLine().getStatusCode() + "'";
            logger.error(errMsg);
            throw new ClientProtocolException(errMsg);
        }
        InputStream is = httpResponse.getEntity().getContent();
        BufferedInputStream bis = new BufferedInputStream(is);
        InputStreamReader reader = new InputStreamReader(bis);
         try {
            return gson.fromJson(reader, LastFmResponse.class);
        } finally {
            is.close();
            bis.close();
            reader.close();
        }
    }
}
