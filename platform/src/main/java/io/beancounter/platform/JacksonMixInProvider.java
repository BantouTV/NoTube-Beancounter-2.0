package io.beancounter.platform;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import io.beancounter.commons.model.PrivateUser;
import io.beancounter.commons.model.User;
import io.beancounter.commons.model.auth.*;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

/**
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
@Provider
public class JacksonMixInProvider implements ContextResolver<ObjectMapper> {

    @Override
    public ObjectMapper getContext(Class<?> aClass) {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.getSerializationConfig().setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);
        mapper.getSerializationConfig().addMixInAnnotations(User.class, PrivateUser.class);
        mapper.getSerializationConfig().addMixInAnnotations(OAuthAuth.class, PrivateOAuth.class);
        mapper.getSerializationConfig().addMixInAnnotations(SimpleAuth.class, PrivateSimpleAuth.class);
        return mapper;
    }
}