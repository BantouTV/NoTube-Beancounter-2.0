package io.beancounter.platform;

import javax.ws.rs.FormParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public abstract class Service {

    public static final String ACTIVITY = "activity";
    public static final String ACTIVITY_ID = "activityId";
    public static final String ACTIVITY_ID_OBJ = "activityIdObj";
    public static final String APPLICATION_ACTION = "application.action";
    public static final String APPLICATION_OBJECT = "application.object";
    public static final String API_KEY = "apikey";
    public static final String IS_VISIBLE = "isVisible";
    public static final String ORDER = "order";
    public static final String PAGE_STRING = "page";
    public static final String PAGE_NUMBER = "pageNumber";
    public static final String PATH = "path";
    public static final String USER = "user";
    public static final String USERNAME = "username";
    public static final String USER_TOKEN = "token";
    public static final String VALUE = "value";
    public static final String VISIBILITY_OBJ = "vObj";

    private static class Param<T> {

        private Class<T> clazz;

        private String name;

        private T value;

        private Class<T> valueClazz;

        public Param(Class<T> clazz, String name, T value, Class<T> valueClazz) {
            this.clazz = clazz;
            this.name = name;
            this.value = value;
            this.valueClazz = valueClazz;
        }

        public boolean isNull() {
            return this.value == null;
        }

        public boolean hasValue(T value) {
            if (isNull()) {
                throw new IllegalStateException(
                        "this parameter is null-valued"
                );
            }
            if (this.value.equals(value)) {
                return true;
            }
            return false;
        }

        public Class<T> getClazz() {
            return clazz;
        }

        public String getName() {
            return name;
        }

        public T getValue() {
            return value;
        }

        public Class<T> getValueClazz() {
            return valueClazz;
        }
    }

    private static void areCompliant(Param... params) throws ServiceException {
        if (params.length == 0) {
            throw new ServiceException(
                    "Parameters are all null"
            );
        }
        for (Param p : params) {
            if (p.isNull()) {
                throw new ServiceException(
                        "Parameter [" + p.getName().substring(p.getName().indexOf('=') + 1, p.getName().lastIndexOf(')')) + "] is null"
                );
            }
            if (!p.getClazz().isAssignableFrom(p.getValueClazz())) {
                throw new ServiceException(
                        "Parameter [" + p.getName().substring(p.getName().indexOf('=') + 1, p.getName().lastIndexOf(')')) + "] " +
                                "doesn't match the type. Found " + p.getValueClazz() + " instead of " + p.getClazz()
                );
            } else {
                if (p.getValue() instanceof String && p.getValue().equals("")) {
                    throw new ServiceException(
                            "Parameter [" + p.getName().substring(p.getName().indexOf('=') + 1, p.getName().lastIndexOf(')')) + "] cannot be empty string"
                    );
                }
            }
        }
    }

    public static void check(Class clazz, String name, Object... values) throws ServiceException {
        Method[] methods = clazz.getMethods();
        Method m = null;
        for(Method candidate : methods) {
            if(candidate.getName().equals(name)) {
                m = candidate;
            }
        }
        if(m == null) {
            throw new ServiceException("Method [" + name + "] not found in [" + clazz.getName() + "]");
        }
        if(m.getParameterTypes().length != values.length) {
            throw new ServiceException("Some parameters are missing for method [" + name + "]. " +
                    "Parameters sent: [" + values.length + "/" + m.getParameterTypes().length + "]");
        }
        check(m, values);
    }

    private static void check(Method m, Object... values) throws
            ServiceException {
        Map<String, Class> types = getJerseyAnnotatedParameters(m);
        int i = 0;
        String names[] = types.keySet().toArray(new String[types.keySet().size()]);
        List<Param> params = new ArrayList<Param>();
        for (Object value : values) {
            if (value == null) {
                String paramName = names[i];
                throw new ServiceException(
                       "Parameter [" + paramName.substring(paramName.indexOf('=') + 1, paramName.lastIndexOf(')')) + "] is null"
                );
            }

            Param p = new Param(types.get(names[i]), names[i], value, value.getClass());
            params.add(p);
            i++;
        }
        areCompliant(params.toArray(new Param[params.size()]));
    }

    private static Map<String, Class> getJerseyAnnotatedParameters(Method m) {
        Annotation[][] annotations = m.getParameterAnnotations();
        Class<?> types[] = m.getParameterTypes();
        Map<String, Class> result = new LinkedHashMap<String, Class>();
        int i = 0;
        for (Annotation[] a : annotations) {
            for (Annotation aa : a) {
                if (
                        aa.annotationType().equals(FormParam.class) ||
                                aa.annotationType().equals(QueryParam.class) ||
                                aa.annotationType().equals(PathParam.class) ||
                                aa.annotationType().equals(Context.class)
                        ) {
                    result.put(aa.toString(), types[i]);
                }
            }
            i++;
        }
        return result;
    }


}
