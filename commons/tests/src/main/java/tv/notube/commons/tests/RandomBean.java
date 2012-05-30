package tv.notube.commons.tests;

import java.util.HashMap;
import java.util.Map;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class RandomBean<T> {

    private T object;

    private Map<String, Object> values = new HashMap<String, Object>();

    public RandomBean(T object) {
        this.object = object;
    }

    public T getObject() {
        return object;
    }

    public void putValue(String s, Object o) {
        values.put(s, o);
    }

    public Object getValue(String s) {
        return values.get(s);
    }

    public Map<String, Object> getValues() {
        return values;
    }
}
