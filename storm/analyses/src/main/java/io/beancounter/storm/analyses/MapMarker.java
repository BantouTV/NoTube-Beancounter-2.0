package io.beancounter.storm.analyses;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multiset;

import java.text.BreakIterator;
import java.util.Locale;
import java.util.Map;

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

    /* Map of keyword->category */
    private final ImmutableMap<String, String> categories;

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

    // TODO: How to order equally ranked categories? Currently by hash order.

    // TODO: Check Italian text works

    @Override
    public void execute(Tuple tuple) {
        // TODO: Locale.ITALY or Locale.ITALIAN?
        String text = tuple.getString(2).toLowerCase(Locale.ITALY);
        BreakIterator boundary = BreakIterator.getWordInstance(Locale.ITALY);
        boundary.setText(text);

        Multiset<String> ranking = HashMultiset.create();

        int start = boundary.first();
        for (int end = boundary.next(); end != BreakIterator.DONE; start = end, end = boundary.next()) {
            String word = text.substring(start, end);
            if (word.trim().isEmpty()) continue;

            String category = categories.get(word);
            if (category != null) {
                ranking.add(category);
            }
        }

        if (!ranking.isEmpty()) {
            collector.emit(new Values(tuple.getDouble(0), tuple.getDouble(1), selectTopCategory(ranking)));
        }
        collector.ack(tuple);
    }

    private String selectTopCategory(Multiset<String> ranking) {
        String topCategory = null;
        for (String category : ranking) {
            if (ranking.count(category) > ranking.count(topCategory)) {
                topCategory = category;
            }
        }
        return topCategory;
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("lat", "long", "category"));
    }
}
