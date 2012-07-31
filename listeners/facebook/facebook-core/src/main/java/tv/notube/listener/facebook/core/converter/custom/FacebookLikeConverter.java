package tv.notube.listener.facebook.core.converter.custom;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.codehaus.jackson.map.ObjectMapper;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tv.notube.commons.model.activity.Context;
import tv.notube.commons.model.activity.facebook.Like;
import tv.notube.listener.facebook.core.model.FacebookData;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * This {@link Converter} is responsible of converting <i>Facebook</i> responses
 * into {@link Like}s.
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
public class FacebookLikeConverter implements Converter<FacebookData, Like> {

    private static final Logger LOGGER = LoggerFactory.getLogger(FacebookLikeConverter.class);

    private static final String SERVICE = "facebook";

    private ObjectMapper mapper;

    @Override
    public Like convert(FacebookData facebookData, boolean isOpenGraph) throws ConverterException {
        if(!isOpenGraph) {
            return convert(facebookData);
        }
        mapper = new ObjectMapper();
        Like like = convert(facebookData);
        Map<String, String> fields = new HashMap<String, String>();
        fields.put("description", "description");
        fromOpenGraph(facebookData.getId(), fields, like);
        return like;
    }

    @Override
    public Context getContext(FacebookData facebookData, String userId) throws ConverterException {
        Context context = new Context();
        context.setDate(new DateTime(facebookData.getCreatedTime()));
        context.setUsername(userId);
        context.setService(SERVICE);
        return context;
    }

    private void fromOpenGraph(String id, Map<String, String> fields, Like like) throws ConverterException {
        Map<String, String> jsonAsMap = makeHttpCall(id);
        for(String fieldOnJson : fields.keySet()) {
            String jsonValue = jsonAsMap.get(fieldOnJson);
            if(jsonValue != null) {
                set(like, fields.get(fieldOnJson), jsonValue);
            }
        }
    }

    private Map<String, String> makeHttpCall(String id) throws ConverterException {
        String urlStr = "http://graph.facebook.com/" + id;
        GetMethod getMethod = new GetMethod(urlStr);
        HttpClient client = new HttpClient();
        try {
            client.executeMethod(getMethod);
        } catch (IOException ioe) {
            try {
                getMethod.releaseConnection();
            } catch (Exception e) {
                final String errMsg = "error while closing the connection [" + urlStr + "]";
                LOGGER.error(errMsg);
                throw new ConverterException(errMsg, e);
            }
            final String errMsg = "error while calling facebook api [" + urlStr + "]";
            LOGGER.error(errMsg);
            throw new ConverterException(errMsg, ioe);
        }
        String responseBody;
        try {
            responseBody = new String(getMethod.getResponseBody());
        } catch (IOException e) {
            final String errMsg = "error while getting response body from facebook api [" + urlStr + "]";
            LOGGER.error(errMsg, e);
            throw new ConverterException(errMsg, e);
        } finally {
            try {
                getMethod.releaseConnection();
            } catch (Exception e) {
                final String errMsg = "error while closing the connection [" + urlStr + "]";
                LOGGER.error(errMsg, e);
                throw new ConverterException(errMsg, e);
            }
        }
        Object map;
        try {
            map = mapper.readValue(responseBody, Object.class);
        } catch (IOException e) {
            final String errMsg = "error while parsing the response [" + responseBody + "]" +
                    "from facebook api [" + urlStr + "]";
            LOGGER.error(errMsg, e);
            throw new ConverterException(errMsg, e);
        }
        try {
            return (Map<String, String>) map;
        } catch (ClassCastException e) {
            final String errMsg = "error while parsing the response [" + responseBody + "]" +
                    "from facebook api [" + urlStr + "]";
            LOGGER.error(errMsg, e);
            throw new UnconvertableException(errMsg, e);
        }
    }

    private void set(Like like, String fieldName, String jsonValue) throws ConverterException {
        Method setter;
        fieldName = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1, fieldName.length());
        try {
            setter = like.getClass().getMethod("set" + fieldName, String.class);
        } catch (NoSuchMethodException e) {
            final String errMsg = "cannot find on class Like method [set"+ fieldName + "]";
            LOGGER.error(errMsg, e);
            throw new ConverterException(errMsg, e);
        }
        try {
            setter.invoke(like, jsonValue);
        } catch (IllegalAccessException e) {
            final String errMsg = "error while invoking [set"+ fieldName + "]";
            LOGGER.error(errMsg, e);
            throw new ConverterException(errMsg, e);
        } catch (InvocationTargetException e) {
            final String errMsg = "error while invoking [set"+ fieldName + "]";
            LOGGER.error(errMsg, e);
            throw new ConverterException(errMsg, e);
        }
    }

    private Like convert(FacebookData facebookData) throws ConverterException {
        Like like = new Like();
        like.setName(facebookData.getName());
        for(String category : getCategories(facebookData.getCategory())) {
            like.addCategory(category);
        }
        String candidateUrl = "http://www.facebook.com/" + facebookData.getId();
        URL url;
        try {
            url = new URL(candidateUrl);
        } catch (MalformedURLException e) {
            final String errMsg = "[" + candidateUrl + "] is ill-formed";
            LOGGER.error(errMsg, e);
            throw new ConverterException(errMsg, e);
        }
        like.setUrl(url);
        return like;
    }

    private String[] getCategories(String category) {
        return category.trim().split("/");
    }
}
