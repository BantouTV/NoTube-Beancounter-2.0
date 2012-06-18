package tv.notube.commons.tagdef.handler.adapter;

import com.google.gson.*;
import tv.notube.commons.tagdef.Def;
import tv.notube.commons.tagdef.TagDefResponse;

import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class TagDefResponseAdapter implements
        JsonDeserializer<TagDefResponse> {

    public TagDefResponse deserialize(
            JsonElement jsonElement,
            Type type,
            JsonDeserializationContext jsonDeserializationContext
    ) throws JsonParseException {
        TagDefResponse tagDefResponse = new TagDefResponse(TagDefResponse.Status.OK);
        JsonObject responseObj = jsonElement.getAsJsonObject();
        tagDefResponse.setAmount(responseObj.get("num_defs").getAsInt());
        JsonArray defsObj = responseObj.get("defs").getAsJsonArray();
        for(JsonElement defElement : defsObj) {
            Def def = new Def();
            JsonObject defObj = defElement.getAsJsonObject().get("def").getAsJsonObject();
            def.setText(defObj.get("text").getAsString());
            try {
                def.setUrl(new URL(defObj.get("uri").getAsString()));
            } catch (MalformedURLException e) {
                // just skip;
                continue;
            }
            tagDefResponse.addDef(def);
        }
        return tagDefResponse;
    }
}