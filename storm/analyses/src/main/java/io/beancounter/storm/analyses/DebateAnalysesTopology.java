package io.beancounter.storm.analyses;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.scheme.StringScheme;
import backtype.storm.spout.KestrelThriftSpout;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.tuple.Fields;
import backtype.storm.utils.Utils;
import storm.redis.JedisPoolConfigSerializable;
import storm.redis.RedisBolt;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class DebateAnalysesTopology {

    public static void main(String[] args) {
        TopologyBuilder builder = new TopologyBuilder();
        builder.setSpout(
                "tweets",
                new KestrelThriftSpout("46.4.89.183", 2229, "social-web-activities", new StringScheme()),
                1
        );
        JedisPoolConfigSerializable config = new JedisPoolConfigSerializable();
        config.setMaxIdle(16);
        config.setMaxActive(16);
        builder.setBolt("tweets-count", new MentionCountBolt(config, "46.4.89.183", "london", "shoreditch", "BBC", "tube"), 1).shuffleGrouping("tweets");
        builder.setBolt("to-natty", new SocketIoBolt("46.4.89.183", 9090, "mentions"), 1).shuffleGrouping("tweets-count");
        builder.setBolt("storage", new RedisBolt(config, "46.4.89.183", false), 1).shuffleGrouping("tweets-count");
        Config conf = new Config();
        conf.setDebug(false);
        conf.setNumWorkers(1);

        LocalCluster cluster = new LocalCluster();
        cluster.submitTopology("analyses", conf, builder.createTopology());
        Utils.sleep(Integer.MAX_VALUE);
        cluster.killTopology("analyses");
        cluster.shutdown();
    }

}
