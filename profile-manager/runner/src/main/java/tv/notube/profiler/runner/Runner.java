package tv.notube.profiler.runner;

import org.joda.time.DateTime;
import tv.notube.profiler.DefaultProfilerFactory;
import tv.notube.profiler.DefaultProfilerImpl;
import org.apache.log4j.Logger;

import java.util.UUID;

/**
 * Main application's entry point. It implements a simple command
 * line interface.
 * 
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class Runner {

    private static Logger logger = Logger.getLogger(Runner.class);

    public static void main(String args[]) {
        DefaultProfilerImpl profiler = DefaultProfilerFactory.getInstance().build();
        // TODO (high) make it configurable
        logger.info("Asking for access to the synchronizer");

        UUID token;

        logger.info("Access granted");
        logger.info("Profiling started at [" + new DateTime() + "]");

        try {
            profiler.run();
        } catch (Exception e) {
            logger.error("Error while profiling process", e);
            System.exit(-1);
        }

        logger.info("Profiling ended at [" + new DateTime() + "]");
        logger.info("Releasing access");

        logger.info("Access released");
    }

}
