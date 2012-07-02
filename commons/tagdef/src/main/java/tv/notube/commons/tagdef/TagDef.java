package tv.notube.commons.tagdef;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import tv.notube.commons.tagdef.handler.TagDefResponseHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class TagDef {

    private final String API_PATTERN = "http://api.tagdef.com/%s.json";

    private HttpClient httpClient = new DefaultHttpClient();;

    public List<String> getDefinitions(String hashTag) throws TagDefException {
        if (hashTag == null) {
            throw new IllegalArgumentException("Parameter hashTag cannot be " +
                    "null");
        }
        HttpGet method = new HttpGet(String.format(API_PATTERN, hashTag));
        ResponseHandler<TagDefResponse> trrh = new TagDefResponseHandler();
        TagDefResponse tagDefResponse;
        try {
             tagDefResponse = httpClient.execute(method, trrh);
        } catch (IOException e) {
            final String errMsg = "Error while calling RegexAPI";
            throw new TagDefException(errMsg, e);
        } finally {
            httpClient.getConnectionManager().closeExpiredConnections();
        }
        List<String> defs = new ArrayList<String>();
        for(Def def : tagDefResponse.getDefs()) {
            defs.add(def.getText());
        }
        return defs;
    }

}
