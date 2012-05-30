package tv.notube.commons.tests;

import tv.notube.commons.tests.annotations.Random;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class Tests {

    private Set<Randomiser> randomisers = new HashSet<Randomiser>();

    private Class currentType;

    public void register(Randomiser randomiser) {
        randomisers.add(randomiser);
    }

    public void assertCompliance(RandomBean rb) {
        Object obj = rb.getObject();
        assert obj != null;
        Map<String, Object> values = rb.getValues();
        for(String k : values.keySet()) {
            Object expected = values.get(k);
            Object actual = invokeGetter(obj, k);
            assert expected.equals(actual);
        }
    }

    private Object invokeGetter(Object obj, String name) {
        String methodName = "get" + name.toUpperCase().substring(0, 1) + name.substring(1);
        Method m;
        try {
            m = obj.getClass().getDeclaredMethod(methodName);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Method [" + methodName + "] does not exist", e);
        }
        try {
            return m.invoke(obj);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Error while accessing to [" + methodName + "]", e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException("Error while invoking to [" + methodName + "]", e);
        }
    }

    public <T> Collection<RandomBean<T>> build(Class<? extends T> aClass, int size)
            throws TestsException {
        Collection<RandomBean<T>> result = new ArrayList<RandomBean<T>>();
        for(int i=0; i < size; i++) {
            result.add(build(aClass));
        }
        return result;
    }

    public <T> RandomBean<T> build(Class<? extends T> aClass) throws TestsException {
        currentType = aClass;
        Constructor constructor = getConstructor(aClass);
        String paramNames[] = getParamNames(constructor);
        Class<?> paramTypes[] = constructor.getParameterTypes();
        Object[] parameterInstances = instantiate(paramTypes);
        Object bean;
        try {
            bean = constructor.newInstance(parameterInstances);
        } catch (InstantiationException e) {
            throw new TestsException("", e);
        } catch (IllegalAccessException e) {
            throw new TestsException("", e);
        } catch (InvocationTargetException e) {
            throw new TestsException("", e);
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

    private Object[] instantiate(Class<?>[] types) throws TestsException {
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
        // ok, there is no a randomiser registered, return null.
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
