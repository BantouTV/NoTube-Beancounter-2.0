package io.beancounter.commons.tagdef.handler;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.ObjectCodec;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;
import io.beancounter.commons.tagdef.Def;
import io.beancounter.commons.tagdef.TagDefResponse;

import java.io.IOException;
import java.net.URL;
import java.util.Iterator;

/**
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
public class TagDefResponseDeserializer extends JsonDeserializer<TagDefResponse> {

    @Override
    public TagDefResponse deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        ObjectCodec oc = jsonParser.getCodec();
        JsonNode node = oc.readTree(jsonParser);
        TagDefResponse response = new TagDefResponse(TagDefResponse.Status.OK);
        response.setAmount(node.get("num_defs").getValueAsInt());
        Iterator<JsonNode> i = node.get("defs").getElements();
        while(i.hasNext()) {
            JsonNode n = i.next();
            Def def = new Def(
                    n.get("def").get("text").getTextValue(),
                    new URL(n.get("def").get("uri").getTextValue())
            );
            response.addDef(def);
        }
        return response;
    }
}