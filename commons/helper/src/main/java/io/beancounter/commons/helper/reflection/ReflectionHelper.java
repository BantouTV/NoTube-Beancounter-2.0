package io.beancounter.commons.helper.reflection;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

/**
 * This class contains some methods that makes easier to
 * do basic <i>Java Reflection</i> stuff.
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class ReflectionHelper {

    public static class Access {
        private String field;
        private String method;

        public Access(String field, String method) {
            this.field = field;
            this.method = method;
        }

        public String getField() {
            return field;
        }

        public String getMethod() {
            return method;
        }
    }

    /**
     *
     * @param clazz
     * @param onlyPrimitives if true, excludes getters with return types non primitives
     * @return
     * @throws ReflectionHelperException
     */
    public static Access[] getGetters(
            Class<? extends Object> clazz,
            boolean onlyPrimitives,
            Class<? extends Object>... exclusions
    ) throws ReflectionHelperException {
        Method[] methods = clazz.getMethods();
        Set<Access> result = new HashSet<Access>();
        for (Method method : methods) {
            // exclude non primitives?
            if(
                    !method.getReturnType().isPrimitive() &&
                            onlyPrimitives &&
                            !isExcluded(method.getReturnType(), exclusions)
                    ) {
                continue;
            }
            // a getter should have no parameters
            if (method.getParameterTypes().length == 0) {
                String methodName = method.getName();
                // and the name should start with get or is
                if(methodName.startsWith("get")) {
                    Access a = new Access(
                            method.getName().replace("get", "").toLowerCase(),
                            method.getName()
                    );
                    result.add(a);
                    continue;
                }
                if(methodName.startsWith("is")) {
                    Access a = new Access(
                            method.getName().replace("is", "").toLowerCase(),
                            method.getName()
                    );
                    result.add(a);
                }
            }
        }
        return result.toArray(new Access[result.size()]);
    }

    private static boolean isExcluded(Class<?> returnType, Class<? extends Object>[] exclusions) {
        for(Class<? extends Object> exclusion : exclusions) {
            if(exclusion.equals(returnType)) {
                return true;
            }
        }
        return false;
    }

    public static <T> T invokeAndCast(String methodName, Object obj, Class<T> aClass)
            throws ReflectionHelperException {
        Method method;
        try {
            method = obj.getClass().getDeclaredMethod(methodName);
        } catch (NoSuchMethodException e) {
            try {
                method = obj.getClass().getMethod(methodName);
            } catch (NoSuchMethodException e1) {
                final String errMgs = "Error while getting method [" + methodName + "] from [" + obj.getClass().getName() + "]";
                throw new ReflectionHelperException(errMgs, e);
            }
        }
        Object returnedValue;
        try {
            returnedValue = method.invoke(obj);
        } catch (IllegalAccessException e) {
            final String errMgs = "Error while calling method [" + methodName + "] from [" + obj.getClass().getName() + "]";
            throw new ReflectionHelperException(errMgs, e);
        } catch (InvocationTargetException e) {
            final String errMgs = "Error while calling method [" + methodName + "] from [" + obj.getClass().getName() + "]";
            throw new ReflectionHelperException(errMgs, e);
        }
        return aClass.cast(returnedValue);
    }
}
