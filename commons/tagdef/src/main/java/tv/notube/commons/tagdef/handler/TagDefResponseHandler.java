package tv.notube.commons.tagdef.handler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import tv.notube.commons.tagdef.TagDefResponse;
import tv.notube.commons.tagdef.handler.adapter.TagDefResponseAdapter;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class TagDefResponseHandler implements ResponseHandler<TagDefResponse> {

    private Gson gson;

    public TagDefResponseHandler() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(
                TagDefResponse.class,
                new TagDefResponseAdapter()
        );
        gson = gsonBuilder.create();
    }

    public TagDefResponse handleResponse(HttpResponse httpResponse)
            throws ClientProtocolException, IOException {
        int status = httpResponse.getStatusLine().getStatusCode();
        if (status != HttpStatus.SC_OK) {
            return new TagDefResponse(TagDefResponse.Status.ERROR);
        }
        InputStream inputStream = httpResponse.getEntity().getContent();
        BufferedInputStream bis = new BufferedInputStream(inputStream);
        InputStreamReader reader = new InputStreamReader(bis);
        try {
            return gson.fromJson(reader, TagDefResponse.class);
        } finally {
            reader.close();
            bis.close();
            inputStream.close();
        }
    }
}
