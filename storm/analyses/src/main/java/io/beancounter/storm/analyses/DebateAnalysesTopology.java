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
        JedisPoolConfigSerializable config = new JedisPoolConfigSerializable();
        config.setMaxIdle(16);
        config.setMaxActive(16);

        TopologyBuilder builder = new TopologyBuilder();
        builder.setSpout(
                "tweets",
                new KestrelThriftSpout("46.4.89.183", 2229, "social-web-activities", new StringScheme()),
                1
        );

        // 1. Total Number of Tweets
        builder.setBolt("tweet-counter", new CounterBolt(), 1)
                .shuffleGrouping("tweets");
        builder.setBolt("tweet-counter-kestrel", new KestrelBolt("46.4.89.183", 2229, "tweet-count"), 1)
                .shuffleGrouping("tweet-counter");

        // 3. Counting Mentions
        builder.setBolt("mentions-count", new MentionCountBolt(config, "46.4.89.183", "london", "shoreditch", "BBC", "tube"), 1)
                .shuffleGrouping("tweets");
        builder.setBolt("mentions-count-kestrel", new KestrelBolt("46.4.89.183", 2229, "mentions"), 1)
                .shuffleGrouping("mentions-count");
        builder.setBolt("storage", new RedisBolt(config, "46.4.89.183", false), 1)
                .shuffleGrouping("mentions-count");

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
