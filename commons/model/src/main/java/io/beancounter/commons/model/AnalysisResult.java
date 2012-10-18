package io.beancounter.commons.model;

import org.joda.time.DateTime;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class AnalysisResult {

    private String analysisName;

    private DateTime lastUpdated;

    private Map<String, String> results = new HashMap<String, String>();

    public AnalysisResult() {
    }

    public AnalysisResult(String analysisName) {
        this.analysisName = analysisName;
    }

    public String getAnalysisName() {
        return analysisName;
    }

    public void setAnalysisName(String analysisName) {
        this.analysisName = analysisName;
    }

    public DateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(DateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public Map<String, String> getResults() {
        return results;
    }

    public String setValue(String key, String value) {
        return results.put(key, value);
    }

    public String setValue(String key, int value) {
        String valueStr = String.valueOf(value);
        return setValue(key, valueStr);
    }

    public String setValue(String key, long value) {
        String valueStr = String.valueOf(value);
        return setValue(key, valueStr);
    }

    public void setValue(String key, UUID value) {
        setValue(key, value.toString());
    }

    public void setValue(String label, double weight) {
        setValue(label, String.valueOf(weight));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AnalysisResult that = (AnalysisResult) o;

        if (analysisName != null ? !analysisName.equals(that.analysisName) : that.analysisName != null)
            return false;
        if (lastUpdated != null ? !lastUpdated.equals(that.lastUpdated) : that.lastUpdated != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = analysisName != null ? analysisName.hashCode() : 0;
        result = 31 * result + (lastUpdated != null ? lastUpdated.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "AnalysisResult{" +
                "analysisName=" + analysisName +
                ", lastUpdated=" + lastUpdated +
                ", results=" + results +
                '}';
    }

}
