package io.beancounter.profiler.rules;

import io.beancounter.commons.linking.LinkingEngine;
import io.beancounter.commons.model.Category;
import io.beancounter.commons.model.Interest;
import io.beancounter.commons.nlp.NLPEngine;

import java.util.List;
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

    public List<Interest> getInterests() throws ProfilingRuleException;

    public List<Category> getCategories() throws ProfilingRuleException;

}
