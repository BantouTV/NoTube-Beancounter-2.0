package tv.notube.commons.linking;

import java.net.URI;
import java.util.Collection;
import java.util.Map;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public interface LinkingEngine {

    public Map<URI, Collection<URI>> link(URI uri) throws LinkingEngineException;

}
