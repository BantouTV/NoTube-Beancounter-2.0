package io.beancounter.storm.analyses;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * An alternative to the KeywordsCountBolt which does not require the use of
 * Redis for storing intermediary results.
 *
 * Expects tuples of the form:
 *      [ word:string ]
 *
 * and emits tuples of the form:
 *      [ word:string, count:integer ]
 *
 * @author Alex Cowell
 */
public class KeywordCounter extends BaseRichBolt {

    private final Map<String, Integer> keywordCounts;

    private OutputCollector collector;

    public KeywordCounter(String... keywords) {
        this(Locale.ENGLISH, keywords);
    }

    public KeywordCounter(Locale locale, String... keywords) {
        keywordCounts = new HashMap<String, Integer>();
        for (String keyword : keywords) {
            keywordCounts.put(keyword.toLowerCase(locale), 0);
        }
    }

    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector collector) {
        this.collector = collector;
    }

    @Override
    public void execute(Tuple tuple) {
        String word = tuple.getString(0);
        collector.ack(tuple);

        Integer count = keywordCounts.get(word);
        if (count != null) {
            keywordCounts.put(word, ++count);
            collector.emit(new Values(word, count));
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("word", "count"));
    }
}
