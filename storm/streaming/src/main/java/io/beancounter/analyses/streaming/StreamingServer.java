package io.beancounter.analyses.streaming;

import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIONamespace;
import com.corundumstudio.socketio.SocketIOServer;
import net.spy.memcached.MemcachedClient;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class StreamingServer {

    private static final Logger LOG = Logger.getLogger(StreamingServer.class);

    private static final int NTHREADS = 10;

    private static ExecutorService executor = Executors.newFixedThreadPool(NTHREADS);

    public static void main(String[] args) throws InterruptedException {
        if(args.length < 3) {
            LOG.error("USAGE: <server-ip> <server-port> <kestrel-ip> <kestrel-port> <list of kestrel queues to listen (at least one)>");
            throw new IllegalArgumentException("insufficient parameters");
        }
        Configuration config = new Configuration();
        config.setHostname(args[0]);
        config.setPort(Integer.valueOf(args[1]));
        SocketIOServer server = new SocketIOServer(config);
        Map<String, SocketIONamespace> namespaces = new HashMap<String, SocketIONamespace>();
        for(int i=4; i < args.length; i++) {
            SocketIONamespace namespace = server.addNamespace("/" + args[i]);
            namespaces.put(args[i], namespace);
        }
        server.start();

        MemcachedClient client;
        try {
            client = new MemcachedClient(new InetSocketAddress(args[2], Integer.valueOf(args[3])));
        } catch (IOException e) {
            final String errMsg = "Error while instantiating MemCached " + "interface to Kestrel queue with parameters [" + args[2] + ":" + args[3] + "]";
            throw new RuntimeException(errMsg, e);
        }

        List<KestrelListenerRunnable> runnables = new ArrayList<KestrelListenerRunnable>();
        for(String queue : namespaces.keySet()) {
            LOG.debug("Starting listeners");
            KestrelListenerRunnable runnable = new KestrelListenerRunnable(client, queue, namespaces.get(queue));
            runnable.start();
            executor.submit(runnable);
            runnables.add(runnable);
        }
        executor.shutdown();
        Thread.sleep(Integer.MAX_VALUE);
        LOG.debug("Starting Shutdown procedure");
        for(KestrelListenerRunnable runnable : runnables) {
            LOG.debug("Stopping listeners");
            runnable.stop();
        }
        client.shutdown();
        server.stop();
    }

}
