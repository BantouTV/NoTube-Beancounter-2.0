package io.beancounter.analyser.analysis;

import io.beancounter.commons.model.AnalysisResult;
import io.beancounter.commons.model.UserProfile;
import io.beancounter.commons.model.activity.Activity;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public abstract class Analysis {

    private String name;

    private String description;

    // TODO (high) we should pass parameters
    protected Analysis() {}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public abstract AnalysisResult run(Activity activity, AnalysisResult previous) throws AnalysisException;

    public abstract AnalysisResult run(UserProfile userProfile, AnalysisResult previous) throws AnalysisException;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Analysis analysis = (Analysis) o;

        if (name != null ? !name.equals(analysis.name) : analysis.name != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Analysis{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
