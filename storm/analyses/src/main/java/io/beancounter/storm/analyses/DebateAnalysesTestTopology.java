package io.beancounter.storm.analyses;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.scheme.StringScheme;
import backtype.storm.spout.KestrelThriftSpout;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.utils.Utils;
import storm.redis.JedisPoolConfigSerializable;
import storm.redis.RedisBolt;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class DebateAnalysesTestTopology {

    public static void main(String[] args) {
        TopologyBuilder builder = new TopologyBuilder();
        builder.setSpout(
                "tweets",
                new KestrelThriftSpout("46.4.89.183", 2229, "social-web-activities", new StringScheme()),
                1
        );
        JedisPoolConfigSerializable config = new JedisPoolConfigSerializable();
        config.setMaxIdle(32);
        config.setMaxActive(32);
        // define analysis bolts
        builder.setBolt("mentions", new KeywordsCountBolt(config, "46.4.89.183", "london", "shoreditch", "BBC", "tube"), 1).shuffleGrouping("tweets");
        builder.setBolt("unique-users", new UniqueUsersCountBolt(config, "46.4.89.183"), 1).shuffleGrouping("tweets");
        builder.setBolt("tweet-counter", new CounterBolt()).globalGrouping("tweets");
        builder.setBolt("usernames", new MentionCountBolt(config, "46.4.89.183"), 1).shuffleGrouping("tweets");
        // define bolts pushing results to kestrel
        builder.setBolt("mentions-to-kestrel", new KestrelBolt("46.4.89.183", 2229, "mentions"), 1).shuffleGrouping("mentions");
        builder.setBolt("unique-to-kestrel", new KestrelBolt("46.4.89.183", 2229, "unique"), 1).shuffleGrouping("unique-users");
        builder.setBolt("counter-to-kestrel", new KestrelBolt("46.4.89.183", 2229, "tweet-count"), 1).shuffleGrouping("tweet-counter");
        builder.setBolt("usernames-to-kestrel", new JsonKestrelBolt("46.4.89.183", 2229, "usernames"), 1).shuffleGrouping("usernames");
        // define bolts storing intermediate results
        builder.setBolt("mentions-storage", new RedisBolt(config, "46.4.89.183", false), 1).shuffleGrouping("mentions");
        builder.setBolt("unique-storage", new RedisBolt(config, "46.4.89.183", false), 1).shuffleGrouping("unique-users");
        builder.setBolt("usernames-storage", new RedisBolt(config, "46.4.89.183", false), 1).shuffleGrouping("usernames");

        Config conf = new Config();
        conf.setDebug(true);
        conf.setNumWorkers(1);

        LocalCluster cluster = new LocalCluster();
        cluster.submitTopology("analyses", conf, builder.createTopology());
        Utils.sleep(Integer.MAX_VALUE);
        cluster.killTopology("analyses");
        cluster.shutdown();
    }

}
