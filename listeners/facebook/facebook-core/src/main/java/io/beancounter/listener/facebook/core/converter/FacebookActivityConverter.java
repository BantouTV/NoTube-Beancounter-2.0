package io.beancounter.listener.facebook.core.converter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.beancounter.commons.model.activity.*;
import io.beancounter.listener.facebook.core.converter.custom.Converter;
import io.beancounter.listener.facebook.core.converter.custom.ConverterException;
import io.beancounter.listener.facebook.core.converter.custom.UnconvertableException;

import java.lang.Object;
import java.util.HashMap;
import java.util.Map;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class FacebookActivityConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(FacebookActivityConverter.class);

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

    public Result convert(Object obj, Verb verb, String userId)
            throws FacebookActivityConverterException {
        Key key = new Key(obj.getClass(), verb);
        LOGGER.debug("got key {}", key);
        Converter converter = converters.get(key);
        if(converter == null) {
            final String errMsg = "object with type [" + obj.getClass() + "] and verb [" + verb + "] is not supported";
            LOGGER.error(errMsg);
            throw new FacebookActivityConverterException(errMsg);
        }
        io.beancounter.commons.model.activity.Object innerObj;
        try {
            // TODO (enabling opengraph stuff should be configurable)
            innerObj = converter.convert(obj, true);
        } catch (UnconvertableException e) {
            final String errMsg = "object with type [" + obj.getClass() + "] cannot be converted";
            LOGGER.warn(errMsg, e);
            throw new UnconvertableFacebookActivityException(errMsg, e);
        } catch (ConverterException e) {
            final String errMsg = "error while converting object with type [" + obj.getClass() + "]";
            LOGGER.error(errMsg);
            throw new FacebookActivityConverterException(errMsg, e);
        }
        Context convertedContext;
        try {
            convertedContext = converter.getContext(obj, userId);
        } catch (ConverterException e) {
            final String errMsg = "error while getting context for [" + obj.getClass() + "]";
            LOGGER.error(errMsg);
            throw new FacebookActivityConverterException(errMsg, e);
        }
        return new Result(innerObj, convertedContext);
    }

    public class Result {
        private io.beancounter.commons.model.activity.Object object;
        private Context context;

        public Result(io.beancounter.commons.model.activity.Object object, Context context) {
            this.object = object;
            this.context = context;
        }

        public io.beancounter.commons.model.activity.Object getObject() {
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

        @Override
        public String toString() {
            return "Key{" +
                    "clazz=" + clazz +
                    ", verb=" + verb +
                    '}';
        }
    }

}
