package io.beancounter.storm.analyses;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

import java.text.BreakIterator;
import java.util.HashMap;
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
    private final Map<String, String> keywords;

    public MapMarker() {
        keywords = new HashMap<String, String>(109);
        addKeywords(keywords, "economy", "economy", "budget", "finmeccanica", "industria", "fiat", "economia", "marchionne", "termini imerese", "fiom", "cgil", "sindacati", "sindacato", "sciopero", "scioperi", "metalmeccanici", "ilva", "startups", "turismo", "landini", "camusso", "chrysler", "ferrari", "montezemolo", "airaudo", "cisl", "uil", "btp", "bce", "merkel", "tremonti", "euro", "pil", "spagna", "bailout", "industriale", "draghi", "contratto", "contratti", "fabbrica", "lavoro", "debito", "grilli");
        addKeywords(keywords, "civil-rights", "vendola", "senonoraquando", "bullismo", "chinascequiédiqui", "lesbiche", "donne", "gay", "giovani", "precari", "studenti", "rosa", "immigrazione", "sbarchi", "lampedusa", "cittadinanza", "rifugiati", "bossi-fini", "castelvolturno", "capolarato");
        addKeywords(keywords, "crime", "falcone", "borsellino", "corruzione", "criminalità", "mafia", "parlamentari", "sobrietà", "crimine", "camorra", "ndrangheta", "casalesi", "napoli", "crimine", "scampia", "secondigliano", "saviano", "omicidio", "trattativa");
        addKeywords(keywords, "environment", "environment", "climate", "tav", "energia", "enel", "eni", "petrolio", "inceneritore", "inceneritori", "acerra", "gas", "eolico", "solare", "rigassificatori", "spazzatura", "rifiuti", "nucleare", "ambiente", "alluvione", "alluvioni", "terremoto", "terremoti", "cemento", "cementificazione", "clima", "greenpeace", "veolia");
        addKeywords(keywords, "education", "scuola", "università", "lavoro", "sanità", "scioperi", "sciopero", "concorsi", "precariato", "riforma", "manifestazione", "manifestazioni", "cisl", "uil", "cgil", "ospedali", "declino", "concorsi", "tagli", "asl", "donne", "ichino", "reddito", "redditi", "giovanile", "disoccupazione");
        addKeywords(keywords, "taxes", "equitalia", "tasse", "fisco", "redditometro", "befera", "iva", "irpef", "pensione", "pensioni", "precariato", "imu", "esodati", "fornero", "inps", "irpeg", "tassazione", "economica");
        addKeywords(keywords, "public-funding", "vitalizi", "fiorito", "marucci", "lusi", "margherita", "m5s", "idv", "polverini", "capogruppo", "lazio", "lombardia", "parlamentare", "parlamentari", "regionale", "giunta", "regione", "travaglio");
    }

    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector collector) {
        this.collector = collector;
    }

    // TODO: Check if the coordinates are even in Italy! Could be done in the
    // GeoTagFilter?

    // TODO: Phrases

    @Override
    public void execute(Tuple tuple) {
        String text = tuple.getString(2).toLowerCase(Locale.ITALY);
        BreakIterator boundary = BreakIterator.getWordInstance(Locale.ITALY);
        boundary.setText(text);

        Multiset<String> ranking = HashMultiset.create();

        int start = boundary.first();
        for (int end = boundary.next(); end != BreakIterator.DONE; start = end, end = boundary.next()) {
            String word = text.substring(start, end);
            if (word.trim().isEmpty()) continue;

            String category = keywords.get(word);
            if (category != null) {
                ranking.add(category);
            }
        }

        collector.emit(new Values(tuple.getDouble(0), tuple.getDouble(1), selectTopCategory(ranking)));
        collector.ack(tuple);
    }

    private String selectTopCategory(Multiset<String> ranking) {
        String topCategory = "other";
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

    private void addKeywords(Map<String, String> keywords, String category, String... keywordsToAdd) {
        for (String keyword : keywordsToAdd) {
            keywords.put(keyword, category);
        }
    }
}
