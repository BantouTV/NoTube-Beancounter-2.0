package io.beancounter.storm.analyses;

import backtype.storm.Config;
import backtype.storm.StormSubmitter;
import backtype.storm.generated.AlreadyAliveException;
import backtype.storm.generated.InvalidTopologyException;
import backtype.storm.scheme.StringScheme;
import backtype.storm.spout.KestrelThriftSpout;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.tuple.Fields;
import storm.redis.JedisPoolConfigSerializable;
import storm.redis.RedisBolt;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class DebateAnalysesTopology {

    private static final String KESTREL = "10.166.6.30";

    private static final String REDIS = "10.166.6.30";

    private static final int NTHREADS = 5;

    public static void main(String[] args) {
        TopologyBuilder builder = new TopologyBuilder();
        builder.setSpout(
                "tweets",
                new KestrelThriftSpout(KESTREL, 2229, "social-web-activities", new StringScheme()),
                NTHREADS
        );
        JedisPoolConfigSerializable config = new JedisPoolConfigSerializable();
        config.setMaxIdle(32);
        config.setMaxActive(32);

        // define analysis bolts
        builder.setBolt("mentions", new KeywordsCountBolt(config, REDIS, "london", "shoreditch", "BBC", "tube"), NTHREADS).shuffleGrouping("tweets");
        builder.setBolt("unique-users", new UniqueUsersCountBolt(config, REDIS), NTHREADS).shuffleGrouping("tweets");
        builder.setBolt("tweet-counter", new CounterBolt()).globalGrouping("tweets");
        builder.setBolt("usernames", new MentionCountBolt(config, REDIS), NTHREADS).shuffleGrouping("tweets");

        // define bolts pushing results to kestrel
        builder.setBolt("mentions-to-kestrel", new KestrelBolt(KESTREL, 2229, "mentions"), 1).shuffleGrouping("mentions");
        builder.setBolt("unique-to-kestrel", new KestrelBolt(KESTREL, 2229, "unique"), 1).shuffleGrouping("unique-users");
        builder.setBolt("counter-to-kestrel", new KestrelBolt(KESTREL, 2229, "tweet-count"), 1).shuffleGrouping("tweet-counter");
        builder.setBolt("usernames-to-kestrel", new JsonKestrelBolt(KESTREL, 2229, "usernames"), 1).shuffleGrouping("usernames");

        // define bolts storing intermediate results
        builder.setBolt("mentions-storage", new RedisBolt(config, KESTREL, false), NTHREADS).shuffleGrouping("mentions");
        builder.setBolt("unique-storage", new RedisBolt(config, KESTREL, false), NTHREADS).shuffleGrouping("unique-users");
        builder.setBolt("usernames-storage", new RedisBolt(config, KESTREL, false), NTHREADS).shuffleGrouping("usernames");

        // Example use of the WordSplitter and KeywordCounter to avoid the use
        // of Redis for storing intermediate results.
        /*
        builder.setBolt("word-splitter", new WordSplitter(), 4)
                .shuffleGrouping("tweets");
        builder.setBolt("keyword-counter", new KeywordCounter("London", "Shoreditch", "BBC", "tube"), 4)
                .fieldsGrouping("word-splitter", new Fields("word"));
        */

        // Example use of the UsernameExtractor, PartialUniqueUsersCounter and
        // CounterBolt.
        /*
        builder.setBolt("username-extractor", new UsernameExtractor(), 4)
                .shuffleGrouping("tweets");
        builder.setBolt("partial-unique-users", new PartialUniqueUsersCounter(), 4)
                .fieldsGrouping("username-extractor", new Fields("username"));
        builder.setBolt("unique-users-counter", new CounterBolt())
                .globalGrouping("partial-unique-users");
        */

        Config conf = new Config();
        conf.setDebug(true);
        conf.setNumWorkers(2);

        try {
            StormSubmitter.submitTopology("analyses", conf, builder.createTopology());
        } catch (AlreadyAliveException e) {
            throw new RuntimeException("error while submitting topology", e);
        } catch (InvalidTopologyException e) {
            throw new RuntimeException("error while submitting topology", e);
        }

    }

}
