package io.beancounter.commons.tagdef.handler;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ResponseHandler;
import org.codehaus.jackson.map.ObjectMapper;
import io.beancounter.commons.tagdef.TagDefResponse;

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

    public TagDefResponse handleResponse(HttpResponse httpResponse)
            throws IOException {
        int status = httpResponse.getStatusLine().getStatusCode();
        if (status != HttpStatus.SC_OK) {
            return new TagDefResponse(TagDefResponse.Status.ERROR);
        }
        InputStream inputStream = httpResponse.getEntity().getContent();
        BufferedInputStream bis = new BufferedInputStream(inputStream);
        InputStreamReader reader = new InputStreamReader(bis);
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(reader, TagDefResponse.class);
        } finally {
            reader.close();
            bis.close();
            inputStream.close();
        }
    }
}
