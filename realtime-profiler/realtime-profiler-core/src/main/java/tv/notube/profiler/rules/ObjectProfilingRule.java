package tv.notube.profiler.rules;

import tv.notube.commons.linking.LinkingEngine;
import tv.notube.commons.nlp.NLPEngine;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public abstract class ObjectProfilingRule<T extends tv.notube.commons.model.activity.Object>
        implements ProfilingRule {

    private T object;

    private NLPEngine nlpEngine;

    private LinkingEngine linkingEngine;

    protected ObjectProfilingRule(NLPEngine nlpEngine, LinkingEngine linkingEngine) {
        this.nlpEngine = nlpEngine;
        this.linkingEngine = linkingEngine;
    }

    public ObjectProfilingRule(T object, NLPEngine nlpEngine, LinkingEngine linkingEngine) {
        this(nlpEngine, linkingEngine);
        this.object = object;
    }

    protected T getObject() {
        return object;
    }

    public NLPEngine getNLPEngine() {
        return nlpEngine;
    }

    public LinkingEngine getLinkingEngine() {
        return linkingEngine;
    }
}
