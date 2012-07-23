package tv.notube.commons.linking;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tv.notube.commons.linking.utils.LineReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * This {@link LinkingEngine} is responsible of the mapping between
 * <i>Facebook OpenGraph protocol</i> categories and <i>Cogito</i> domains.
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class FacebookCogitoLinkingEngine implements LinkingEngine {

    private static final Logger logger = LoggerFactory.getLogger(FacebookCogitoLinkingEngine.class);

    private static final String FILE = "mappings.txt";

    private Map<String, String> mappings = new HashMap<String, String>();

    public FacebookCogitoLinkingEngine() {
        synchronized (this) {
            load();
        }
    }

    private void load() {
        InputStream is = FacebookCogitoLinkingEngine.class
                .getClassLoader()
                .getResourceAsStream(FILE);
        if (is == null) {
            final String errMsg = "could not open [" + FILE + "]";
            logger.error(errMsg);
            throw new RuntimeException(errMsg);
        }
        InputStreamReader reader = new InputStreamReader(is);
        int index = 0;
        for (String line : new LineReader(reader)) {
            String[] kv = line.split("=");
            if (kv.length != 2) {
                final String errMsg = "wrong mapping in [" + FILE + "] at line [" + index + "]. Skipping";
                logger.warn(errMsg);
                continue;
            }
            mappings.put(kv[0].trim().toLowerCase(), kv[1].trim().toLowerCase());
        }
        try {
            reader.close();
        } catch (IOException e) {
            final String errMsg = "error while closing mapping file input stream reader";
            logger.error(errMsg, e);
            throw new RuntimeException(errMsg, e);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                final String errMsg = "error while closing mapping file input stream";
                logger.error(errMsg, e);
                throw new RuntimeException(errMsg, e);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String link(String source) throws LinkingEngineException {
        String target = mappings.get(source);
        if (target == null) {
            final String errMsg = "unable to find a link for [" + source + "]";
            logger.error(errMsg);
            throw new LinkNotFoundException(errMsg);
        }
        return target;
    }

    @Override
    public void refresh() throws LinkingEngineException {
        synchronized (this) {
            mappings = new HashMap<String, String>();
            load();
        }
    }
}
