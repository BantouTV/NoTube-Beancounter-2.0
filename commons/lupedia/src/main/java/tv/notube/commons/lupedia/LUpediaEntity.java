package tv.notube.commons.lupedia;

import java.net.URI;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class LUpediaEntity {

    private int startOffset;

    private int endOffset;

    private URI instanceUri;

    private URI instanceClass;

    private URI predicateUri;

    private double weight;

    public int getStartOffset() {
        return startOffset;
    }

    public void setStartOffset(int startOffset) {
        this.startOffset = startOffset;
    }

    public int getEndOffset() {
        return endOffset;
    }

    public void setEndOffset(int endOffset) {
        this.endOffset = endOffset;
    }

    public URI getInstanceUri() {
        return instanceUri;
    }

    public void setInstanceUri(URI instanceUri) {
        this.instanceUri = instanceUri;
    }

    public URI getInstanceClass() {
        return instanceClass;
    }

    public void setInstanceClass(URI instanceClass) {
        this.instanceClass = instanceClass;
    }

    public URI getPredicateUri() {
        return predicateUri;
    }

    public void setPredicateUri(URI predicateUri) {
        this.predicateUri = predicateUri;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }
}

/**
 *
 *
 * [
  {
    "startOffset": 1,
    "endOffset": 5,
    "instanceUri": "http:\/\/dbpedia.org\/resource\/Rome",
    "instanceClass": "http:\/\/dbpedia.org\/ontology\/Settlement",
    "predicateUri": "http:\/\/www.w3.org\/2000\/01\/rdf-schema#label",
    "weight": 0.99
  },
  {
    "startOffset": 10,
    "endOffset": 16,
    "instanceUri": "http:\/\/dbpedia.org\/resource\/London",
    "instanceClass": "http:\/\/dbpedia.org\/ontology\/Settlement",
    "predicateUri": "http:\/\/www.w3.org\/2000\/01\/rdf-schema#label",
    "weight": 0.99
  }
]
 *
 *
 */
