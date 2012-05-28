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
public class Tests {

    private Set<Randomiser> randomisers = new HashSet<Randomiser>();

    private Class currentType;

    public Set<Randomiser> getRandomisers() {
        return randomisers;
    }

    public void register(Randomiser randomiser) {
        randomisers.add(randomiser);
    }

    public RandomBean build(Class aClass) throws BuilderException {
        currentType = aClass;
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
                // okay, this is a recursive call.
                // if the object is the same we should stop at some point
                // otherwise we can easily get into stackoverflow
                if(type.equals(currentType)) {
                    // simply instantiate it without recurring
                    instances.add(instantiateSingle(type));
                    continue;
                }
                instances.add(build(type).getObject());
            }
        }
        return instances.toArray(new Object[instances.size()]);
    }

    private Object instantiateSingle(Class<?> type) {
        Randomiser r = filterByType(type);
        if (r != null) {
            return r.getRandom();
        }
        throw new RuntimeException("an Randomiser<" + type + "> implementation has been registered");
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
        throw new RuntimeException("Class [" + type + "] has no constructor annotated with @Random nor an Randomiser<" + type + "> implementation has been registered");
    }
}
