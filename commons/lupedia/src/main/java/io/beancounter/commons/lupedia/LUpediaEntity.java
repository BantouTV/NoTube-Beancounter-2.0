package io.beancounter.commons.lupedia;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class LUpediaEntity {

    public int startOffset;

    public int endOffset;

    public String instanceUri;

    public String instanceClass;

    public String predicateUri;

    public double weight;

    public String getInstanceUri() {
        return instanceUri;
    }

    public void setInstanceUri(String instanceUri) {
        this.instanceUri = instanceUri;
    }

    public String getInstanceClass() {
        return instanceClass;
    }

    public void setInstanceClass(String instanceClass) {
        this.instanceClass = instanceClass;
    }

    public String getPredicateUri() {
        return predicateUri;
    }

    public void setPredicateUri(String predicateUri) {
        this.predicateUri = predicateUri;
    }

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
