package tv.notube.extension.profilingline.skos;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import tv.notube.extension.profilingline.skos.http.SkosLookupResponse;
import tv.notube.extension.profilingline.skos.http.SkosLookupResponseHandler;

import java.io.IOException;
import java.net.URI;
import java.util.List;

/**
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class SkosResolver {

    private final static String SKOS = "http://moth.notube.tv:9090/skos-lookup/rest/skos/";

    private final static String TYPE = "http://moth.notube.tv:9090/skos-lookup/rest/type/";

    private HttpClient httpClient;

    public SkosResolver() {
        httpClient = new DefaultHttpClient();
    }

    public List<URI> getSkos(URI resource) throws SkosResolverException {
        String name = resource.toString().substring(
                "http://dbpedia.org/resource/".length(),
                resource.toString().length()
        );
        String queryUrl = SKOS + name;
        HttpGet method = new HttpGet(queryUrl);
        SkosLookupResponse response;
        ResponseHandler<SkosLookupResponse> lrh = new SkosLookupResponseHandler();
        try {
            response = httpClient.execute(method, lrh);
        } catch (IOException e) {
            throw new SkosResolverException(
                    "Error while calling skos SkosLookup with resource: '"
                            + resource + "'",
                    e
            );
        }
        try {
        return response.getUris();
        } finally {
            httpClient.getConnectionManager().closeExpiredConnections();
        }
    }

    public URI getTypes(URI resource) throws SkosResolverException {
        String name = resource.toString().substring(
                "http://dbpedia.org/resource/".length(),
                resource.toString().length()
        );
        String queryUrl = TYPE + name;
        HttpGet method = new HttpGet(queryUrl);
        SkosLookupResponse response;
        ResponseHandler<SkosLookupResponse> lrh = new SkosLookupResponseHandler();
        try {
            response = httpClient.execute(method, lrh);
        } catch (IOException e) {
            throw new SkosResolverException(
                    "Error while calling type SkosLookup with resource: '"
                            + resource + "'",
                    e
            );
        }
        try {
            return response.getUris().get(0);
        } finally {
            httpClient.getConnectionManager().closeExpiredConnections();
        }
    }
}
