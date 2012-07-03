package tv.notube.commons.helper.es;

/**
 * Just a POJO to hold any/all information about a node.
 *
 * @author Alex Cowell ( alxcwll@gmail.com )
 */
public class NodeInfo {

    private String host;

    private int port;

    public NodeInfo(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public String toString() {
        return "NodeInfo{" +
                "host='" + host + '\'' +
                ", port=" + port +
                '}';
    }
}
