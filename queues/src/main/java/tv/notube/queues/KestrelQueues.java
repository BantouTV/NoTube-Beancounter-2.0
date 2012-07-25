package tv.notube.queues;

import net.spy.memcached.MemcachedClient;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Properties;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public final class KestrelQueues implements Queues {

    private Properties properties;

    private MemcachedClient client;

    public KestrelQueues(Properties properties) {
        this.properties = properties;
        String host = this.properties.getProperty("kestrel.process.host");
        int port = Integer.parseInt(this.properties.getProperty("kestrel.process.port"));
        try {
            client = new MemcachedClient(new InetSocketAddress(host, port));
        } catch (IOException e) {
            final String errMsg = "Error while instantiating MemCached " + "interface to Kestrel queue with parameters [" + host + ":" + port + "]";
            throw new RuntimeException(errMsg, e);
        }
    }

    @Override
    public void shutDown() throws QueuesException {
        try {
            client.shutdown();
        } catch (Exception e) {
            final String errMsg = "Error while shutting down connection to Kestrel";
            throw new QueuesException(errMsg, e);
        }
    }

    @Override
    public void push(String json) throws QueuesException {
        String queueName =  properties.getProperty("kestrel.process.queue");
        client.set(queueName, 3600, json);
    }

}
