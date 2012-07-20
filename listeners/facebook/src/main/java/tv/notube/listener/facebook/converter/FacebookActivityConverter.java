package tv.notube.listener.facebook.converter;

import tv.notube.commons.model.activity.*;

import java.lang.Object;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class FacebookActivityConverter {

    public void registerConverter(Class<?> clazz, Verb verb, Converter converter)
        throws FacebookActivityConverterException {}

    public tv.notube.commons.model.activity.Object convert(Object obj)
            throws FacebookActivityConverterException {
        throw new UnsupportedOperationException();
    }

}
