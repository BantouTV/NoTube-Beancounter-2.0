package tv.notube.listener.facebook.converter;

import tv.notube.commons.model.activity.facebook.Like;
import tv.notube.listener.facebook.model.FacebookData;

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

    @Override
    public Like convert(FacebookData facebookData, boolean isOpenGraph) throws ConverterException {
        if(!isOpenGraph) {
            return convert(facebookData);
        }
        Like like = convert(facebookData);
        Map<String, String> fields = new HashMap<String, String>();
        fields.put("description", "description");
        fromOpenGraph(facebookData.getId(), fields, like);
        return like;
    }

    private void fromOpenGraph(String id, Map<String, String> fields, Like like) {
        Map<String, String> jsonAsMap = makeHttpCall(id);
        for(String fieldOnJson : fields.keySet()) {
            String jsonValue = jsonAsMap.get(fieldOnJson);
            if(jsonValue != null) {
                set(like, fields.get(fieldOnJson), jsonValue);
            }
        }
    }

    private Map<String, String> makeHttpCall(String id) {
        throw new UnsupportedOperationException("NIY");
    }

    private void set(Like like, String fieldName, String jsonValue) {
        Method setter;
        fieldName = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1, fieldName.length());
        try {
            setter = like.getClass().getMethod("set" + fieldName, String.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("cannot find on class Like method [set"+ fieldName + "]", e);
        }
        try {
            setter.invoke(like, jsonValue);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("error while invoking [set"+ fieldName + "]", e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException("error while invoking [set"+ fieldName + "]", e);
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
            throw new ConverterException("[" + candidateUrl + "] is ill-formed", e);
        }
        like.setUrl(url);

        return like;
    }

    private String[] getCategories(String category) {
        return category.trim().split("/");
    }
}
