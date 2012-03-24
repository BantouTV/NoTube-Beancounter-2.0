package tv.notube.crawler.requester.request.twitter.bbc;

import com.rosaloves.bitlyj.Bitly;
import com.rosaloves.bitlyj.Url;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.rdfxml.RDFXMLParser;
import tv.notube.commons.model.activity.bbc.BBCProgramme;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class BBC {

    private HttpClient httpClient;

    private RDFXMLParser rdfxmlParser;

    private BBCRDFHandler rdfHandler;

    private Bitly.Provider bitly;

    public BBC() {
        httpClient = new DefaultHttpClient();
        rdfxmlParser = new RDFXMLParser();
        rdfHandler = new BBCRDFHandler();
        rdfxmlParser.setRDFHandler(rdfHandler);
        bitly = Bitly.as("o_2o020v3qri", "R_5767821f37e587175d9319f645ec92c3");
    }

    public BBCProgramme getProgramme(URL programmeURL) throws BBCException {
        InputStream is = invokeURL(programmeURL);
        try {
            rdfxmlParser.parse(is, programmeURL.toString());
        } catch (IOException e) {
            throw new BBCException("I/O error", e);
        } catch (RDFParseException e) {
            throw new BBCException("Error while parsing RDF/XML", e);
        } catch (RDFHandlerException e) {
            throw new BBCException("Error while handling RDF", e);
        }
        try {
            return rdfHandler.getProgramme();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                throw new BBCException("Error while closing stream", e);
            }
        }
    }

    public String getProgrammeId(URL url) throws BBCException {
        Url response = bitly.call(
                Bitly.expand(url.toString())
        );
        String longUrl = response.getLongUrl();
        String programmeId = longUrl.substring(
                "http://www.bbc.co.uk/iplayer/episode/".length(),
                longUrl.length()
        ).split("/")[0];
        // ex: http://www.bbc.co.uk/iplayer/episode/b0183nyn/EastEnders_08_12_2011/
        return programmeId;
    }

    public boolean isBBC(URL url) {
        // ex: http://bbc.in/uBkv2y
        if (url.toString().startsWith("http://bbc.in/")) {
            return true;
        }
        return false;
    }

    private InputStream invokeURL(URL programmeURI) throws BBCException {
        HttpGet method = new HttpGet(programmeURI.toString());
        HttpResponse httpResponse;
        try {
            httpResponse = httpClient.execute(method);
        } catch (IOException e) {
            throw new BBCException("Error while accessing BBC /programmes", e);
        }
        if (httpResponse.getStatusLine().getStatusCode() != 200) {
            throw new BBCException("BBC /programmes didn't reply 200 OK");
        }
        try {
            return httpResponse.getEntity().getContent();
        } catch (IOException e) {
            throw new BBCException("Error while accessing http response", e);
        } finally {
            httpClient.getConnectionManager().closeExpiredConnections();
        }
    }
}
