package tv.notube.profiler.runner;

import org.joda.time.DateTime;
import tv.notube.profiler.DefaultProfilerFactory;
import tv.notube.profiler.DefaultProfilerImpl;
import org.apache.log4j.Logger;
import tv.notube.synch.client.Helper;
import tv.notube.synch.client.SynchronizerClientException;

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
        Helper helper = Helper.getInstance("http://moth.notube.tv:9090/service-1.0-SNAPSHOT/rest/synch");
        logger.info("Asking for access to the synchronizer");

        UUID token;
        try {
            token = helper.access("profiler");
        } catch (SynchronizerClientException e) {
            logger.error("Error while waiting for getting access", e);
            System.exit(-1);
            return;
        }

        logger.info("Access granted");
        logger.info("Profiling started at [" + new DateTime() + "]");

        try {
            profiler.run();
        } catch (Exception e) {
            try {
                helper.release("profiler", token);
            } catch (SynchronizerClientException e1) {
                logger.error("Error while waiting for releasing resource", e);
                System.exit(-1);
                return;
            }
            logger.error("Error while profiling process", e);
            System.exit(-1);
        }

        logger.info("Profiling ended at [" + new DateTime() + "]");
        logger.info("Releasing access");

        try {
            helper.release("profiler", token);
        } catch (SynchronizerClientException e) {
            logger.error("Error while waiting for releasing resource", e);
            System.exit(-1);
            return;
        }

        logger.info("Access released");
    }

}
