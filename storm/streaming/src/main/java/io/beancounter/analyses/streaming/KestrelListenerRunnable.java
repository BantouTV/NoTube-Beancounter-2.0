package io.beancounter.analyses.streaming;

import com.corundumstudio.socketio.BroadcastOperations;
import com.corundumstudio.socketio.SocketIONamespace;
import net.spy.memcached.MemcachedClient;
import org.apache.log4j.Logger;


/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class KestrelListenerRunnable implements Runnable {

    private static final Logger LOG = Logger.getLogger(KestrelListenerRunnable.class);

    private MemcachedClient client;

    private String queue;

    private SocketIONamespace namespace;

    private boolean running = false;

    public KestrelListenerRunnable(MemcachedClient client, String queue, SocketIONamespace namespace) {
        this.client = client;
        this.queue = queue;
        this.namespace = namespace;
    }

    public void run() {
        while (running) {
            String resultJson = (String) client.get(queue);
            if (resultJson != null) {
                BroadcastOperations operations = namespace.getBroadcastOperations();
                if (operations != null) {
                    LOG.debug("sending [" + resultJson + "] to [" + queue + "]");
                    operations.sendMessage(resultJson);
                }
            }
        }
    }

    public void start() {
        this.running = true;
    }

    public void stop() {
        this.running = false;
    }
}
