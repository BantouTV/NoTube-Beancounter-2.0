package io.beancounter.analyser.analysis;

import io.beancounter.commons.model.AnalysisResult;
import io.beancounter.commons.model.UserProfile;
import io.beancounter.commons.model.activity.Activity;

import java.util.UUID;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public abstract class Analysis {

    private UUID id = UUID.randomUUID();

    private String name;

    private String description;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

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

    public abstract AnalysisResult run(Activity activity) throws AnalysisException;

    public abstract AnalysisResult run(UserProfile userProfile) throws AnalysisException;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Analysis analysis = (Analysis) o;

        if (id != null ? !id.equals(analysis.id) : analysis.id != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Analysis{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
