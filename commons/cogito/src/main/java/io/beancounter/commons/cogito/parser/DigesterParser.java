package io.beancounter.commons.cogito.parser;

import org.apache.commons.digester3.Digester;
import org.xml.sax.SAXException;
import io.beancounter.commons.cogito.model.Response;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
public class DigesterParser {

    private Digester digester = new Digester();

    public DigesterParser() {
        CogitoRuleSet ruleSet = new CogitoRuleSet();
        ruleSet.addRuleInstances(digester);
    }

    public synchronized Response parse(InputStream inputStream) throws DigesterParserException {
        Response response;
        try {
            response = digester.parse(inputStream);
        } catch (SAXException e) {
            final String errMsg = "Error while parsing XML response";
            throw new DigesterParserException(errMsg, e);
        } catch (IOException e) {
            final String errMsg = "Error while reading from stream";
            throw new DigesterParserException(errMsg, e);
        }
        return response;
    }
}