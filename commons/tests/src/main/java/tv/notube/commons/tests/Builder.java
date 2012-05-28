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

    public RandomBean build(Class aClass) throws BuilderException {
        Constructor constructor = getConstructor(aClass);
        String paramNames[] = getParamNames(constructor);
        Class<?> paramTypes[] = constructor.getParameterTypes();
        Object[] parameterInstances = instantiate(paramTypes);
        Object bean;
        try {
            bean = constructor.newInstance(parameterInstances);
        } catch (InstantiationException e) {
            throw new BuilderException("", e);
        } catch (IllegalAccessException e) {
            throw new BuilderException("", e);
        } catch (InvocationTargetException e) {
            throw new BuilderException("", e);
        }
        RandomBean rb = new RandomBean(bean);
        int i = 0;
        for(String paramName : paramNames) {
            rb.putValue(paramName, parameterInstances[i]);
            i++;
        }
        return rb;
    }

    private String[] getParamNames(Constructor constructor) {
        Random ra = (Random) constructor.getAnnotation(Random.class);
        return ra.names();
    }

    private Object[] instantiate(Class<?>[] types) throws BuilderException {
        Collection<Object> instances = new ArrayList<Object>();
        for (Class<?> type : types) {
            Randomiser r = filterByType(type);
            if (r != null) {
                instances.add(r.getRandom());
            } else {
                instances.add(build(type).getObject());
            }
        }
        return instances.toArray(new Object[instances.size()]);
    }

    private Randomiser filterByType(Class<?> type) {
        for(Randomiser r : randomisers) {
            if(r.type().equals(type)) {
                return r;
            }
        }
        // ok, there is no a randomiser registered return null.
        return null;
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
        throw new RuntimeException("Class [" + type + "] has no constructor annotated with @EntryPoint nor an Randomiser<" + type + "> implementation has been registered");
    }
}
