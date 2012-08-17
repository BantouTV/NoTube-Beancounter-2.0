package io.beancounter.commons.linking;

/**
 * This interface defines the minimum contract a linking engine must
 * satisfy.
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public interface LinkingEngine {

    /**
     * It provides an exact match
     * @param source
     * @return
     * @throws LinkingEngineException
     */
    public String link(String source) throws LinkingEngineException;

    /**
     * Refreshes all the in memory mappings.
     *
     * @throws LinkingEngineException
     */
    public void refresh() throws LinkingEngineException;

}
