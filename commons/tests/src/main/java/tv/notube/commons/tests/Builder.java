package tv.notube.commons.tests;

import tv.notube.commons.tests.annotations.Random;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class Builder {

    private Set<Randomiser> randomisers = new HashSet<Randomiser>();

    public Set<Randomiser> getRandomisers() {
        return randomisers;
    }

    public void register(Randomiser randomiser) {
        randomisers.add(randomiser);
    }

    public Object build(Class aClass) throws BuilderException {
        Constructor constructor = getConstructor(aClass);
        Class<?> paramTypes[] = constructor.getParameterTypes();
        Object[] parameterInstances = instantiate(paramTypes);
        try {
            return constructor.newInstance(parameterInstances);
        } catch (InstantiationException e) {
            throw new BuilderException("", e);
        } catch (IllegalAccessException e) {
            throw new BuilderException("", e);
        } catch (InvocationTargetException e) {
            throw new BuilderException("", e);
        }
    }

    private Object[] instantiate(Class<?>[] types) {
        Collection<Object> instances = new ArrayList<Object>();
        for(Class<?> type : types) {
            Randomiser r = filterByType(type);
            instances.add(r.getRandom());
        }
        return instances.toArray(new Object[instances.size()]);
    }

    private Randomiser filterByType(Class<?> type) {
        for(Randomiser r : randomisers) {
            if(r.type().equals(type)) {
                return r;
            }
        }
        throw new RuntimeException("Cannot find a randomiser for [" + type + "]");
    }

    private Constructor getConstructor(Class type) {
        Constructor constructors[] = type.getDeclaredConstructors();
        for(Constructor constructor : constructors) {
            Annotation annotations[] = constructor.getDeclaredAnnotations();
            for(Annotation a : annotations) {
                if(a.annotationType().equals(Random.class)) {
                    return constructor;
                }
            }
        }
        throw new RuntimeException("Class [" + type + "] has no constructor annotated with @EntryPoint");
    }
}
