package tv.notube.profiler.rules;

import tv.notube.commons.linking.LinkingEngine;
import tv.notube.commons.nlp.NLPEngine;

import java.net.URI;
import java.util.Collection;
import java.util.Properties;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public interface ProfilingRule {

    public NLPEngine getNLPEngine();

    public LinkingEngine getLinkingEngine();

    public void run(Properties properties) throws ProfilingRuleException;

    public Collection<URI> getResult() throws ProfilingRuleException;

}
