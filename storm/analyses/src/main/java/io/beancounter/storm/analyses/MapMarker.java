package io.beancounter.storm.analyses;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import com.google.common.collect.ImmutableMap;

import java.text.BreakIterator;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/**
 * Expects tuples of the form:
 *      [ lat:double, long:double, text:string ]
 *
 * then processes the text to categorise the tweet and outputs:
 *      [ lat:double, long:double, category:string ]
 *
 * @author Alex Cowell
 */
public class MapMarker extends BaseRichBolt {

    private OutputCollector collector;

    private final Map<String, String> categories;

    public MapMarker() {
        categories = ImmutableMap.of(
                "economy", "economy",
                "budget", "economy",
                "environment", "environment",
                "climate", "environment"
        );
    }

    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector collector) {
        this.collector = collector;
    }

    // TODO: Check if the coordinates are even in Italy! Could be done in the
    // GeoTagFilter?

    // TODO: Multiple categories in same text?

    // TODO: Check Italian text works

    @Override
    public void execute(Tuple tuple) {
        String text = tuple.getString(2).toLowerCase(Locale.ITALY);
        BreakIterator boundary = BreakIterator.getWordInstance(Locale.ITALY);
        boundary.setText(text);

        TreeMap<String, Integer> ranking = new TreeMap<String, Integer>();

        int start = boundary.first();
        for (int end = boundary.next(); end != BreakIterator.DONE; start = end, end = boundary.next()) {
            String word = text.substring(start, end);
            String category = categories.get(word);

            if (category != null) {
                Integer categoryScore = ranking.get(category);
                if (categoryScore == null) {
                    categoryScore = 0;
                }
                ranking.put(category, ++categoryScore);
            }
        }
        collector.emit(new Values(tuple.getDouble(0), tuple.getDouble(1), ranking.firstKey()));
        collector.ack(tuple);
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("lat", "long", "category"));
    }
}
