package tv.notube.commons.configuration.analytics;

import tv.notube.commons.storage.model.Query;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class AnalysisDescription implements Serializable {

    public String name;

    public String description;

    private Query query;

    private String className;

    private String resultClassName;

    private Set<MethodDescription> methodDescriptions = new HashSet<MethodDescription>();

    public AnalysisDescription() {}

    public AnalysisDescription(
            String name,
            String description,
            Query query,
            String className,
            String resultClassName,
            Set<MethodDescription> methodDescriptions
    ) {
        this.name = name;
        this.description = description;
        this.query = query;
        this.className = className;
        this.resultClassName = resultClassName;
        this.methodDescriptions = methodDescriptions;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Query getQuery() {
        return query;
    }

    public String getClassName() {
        return className;
    }

    public String getResultClassName() {
        return resultClassName;
    }

    public void addMethodDescription(
            String name,
            String description,
            String[] parameterTypes
    ) {
        methodDescriptions.add(
                new MethodDescription(name, description, parameterTypes)
        );
    }

    public Set<MethodDescription> getMethodDescriptions() {
        return methodDescriptions;
    }

    public MethodDescription[] getMethodDescriptions(String methodName) {
        List<MethodDescription> result = new ArrayList<MethodDescription>();
        for(MethodDescription methodDescription : methodDescriptions) {
            if(methodDescription.getName().equals(methodName)) {
                result.add(methodDescription);
            }
        }
        return result.toArray(new MethodDescription[result.size()]);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AnalysisDescription that = (AnalysisDescription) o;

        if (name != null ? !name.equals(that.name) : that.name != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "AnalysisDescription{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", query=" + query +
                ", className='" + className + '\'' +
                ", resultClassName='" + resultClassName + '\'' +
                ", methodDescriptions=" + methodDescriptions +
                '}';
    }
}
