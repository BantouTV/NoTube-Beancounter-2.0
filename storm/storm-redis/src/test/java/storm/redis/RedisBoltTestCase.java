package storm.redis;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.testing.TestWordSpout;
import backtype.storm.topology.IBasicBolt;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.utils.Utils;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import redis.clients.jedis.JedisPoolConfig;


import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class RedisBoltTestCase {

    private Config conf;

    private TopologyBuilder builder;

    private LocalCluster cluster;

    private TestSpout spoutMock;

    @BeforeTest
    public void setUp() {
        builder = new TopologyBuilder();

        spoutMock = new TestSpout();

		//add mocked spout to the builder
		builder.setSpout("spout", spoutMock, 2);
        JedisPoolConfigSerializable config = new JedisPoolConfigSerializable();
        config.setMaxIdle(16);
        config.setMaxActive(16);
        RedisBolt redisBolt = new RedisBolt(config, "46.4.89.183", false);
        builder.setBolt("redisBolt", redisBolt, 1).shuffleGrouping("spout");
        conf = new Config();
		conf.setDebug(true);
		cluster = new LocalCluster();
    }

    @AfterTest
    public void tearDown() {
        cluster.shutdown();
    }

    @Test
    public void test() {
        cluster.submitTopology("sample-workflow", conf, builder.createTopology());
        Utils.sleep(5000);
    }

}
