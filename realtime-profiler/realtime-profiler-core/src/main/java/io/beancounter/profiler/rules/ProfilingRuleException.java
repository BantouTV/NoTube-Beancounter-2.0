package io.beancounter.profiler.rules;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class ProfilingRuleException extends Exception {

    public ProfilingRuleException(String message, Exception e) {
        super(message, e);
    }
}
