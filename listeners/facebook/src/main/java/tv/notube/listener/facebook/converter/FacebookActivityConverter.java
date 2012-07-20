package tv.notube.listener.facebook.converter;

import tv.notube.commons.model.activity.*;
import tv.notube.listener.facebook.converter.custom.Converter;
import tv.notube.listener.facebook.converter.custom.ConverterException;

import java.lang.Object;
import java.util.HashMap;
import java.util.Map;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class FacebookActivityConverter {

    private Map<Key, Converter> converters = new HashMap<Key, Converter>();

    public void registerConverter(Class<?> clazz, Verb verb, Converter converter)
        throws FacebookActivityConverterException {
        Key key = new Key(clazz, verb);
        if(converters.containsKey(key)) {
            converters.remove(key);
            converters.put(key, converter);
        } else {
            converters.put(key, converter);
        }
    }

    public Result convert(Object obj, Verb verb)
            throws FacebookActivityConverterException {
        Key key = new Key(obj.getClass(), verb);
        Converter converter = converters.get(key);
        if(converter == null) {
            throw new FacebookActivityConverterException("object with type [" + obj.getClass() + "] and verb [" + verb + "] is not supported");
        }
        // TODO (opengraph stuff should be configurable)
        try {
            return new Result(converter.convert(obj, true), converter.getContext(obj));
        } catch (ConverterException e) {
            throw new FacebookActivityConverterException("error while converting object with type [" + obj.getClass() + "]", e);
        }
    }

    public class Result {
        private tv.notube.commons.model.activity.Object object;
        private Context context;

        public Result(tv.notube.commons.model.activity.Object object, Context context) {
            this.object = object;
            this.context = context;
        }

        public tv.notube.commons.model.activity.Object getObject() {
            return object;
        }

        public Context getContext() {
            return context;
        }
    }

    private class Key {
        private Class<?> clazz;
        private Verb verb;

        private Key(Class<?> clazz, Verb verb) {
            this.clazz = clazz;
            this.verb = verb;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Key key = (Key) o;

            if (clazz != null ? !clazz.equals(key.clazz) : key.clazz != null)
                return false;
            if (verb != key.verb) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = clazz != null ? clazz.hashCode() : 0;
            result = 31 * result + (verb != null ? verb.hashCode() : 0);
            return result;
        }
    }

}
