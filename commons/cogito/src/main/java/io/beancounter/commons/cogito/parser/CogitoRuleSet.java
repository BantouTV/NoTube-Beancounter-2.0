package io.beancounter.commons.cogito.parser;

import org.apache.commons.digester3.*;
import io.beancounter.commons.cogito.model.*;

/**
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
public class CogitoRuleSet implements RuleSet {

    @Override
    public void addRuleInstances(Digester digester) {

        digester.addObjectCreate("RESPONSE", Response.class);

        digester.addObjectCreate("RESPONSE/SYNTHESIS/ENTITIES", Entities.class);
        digester.addSetProperties("RESPONSE/SYNTHESIS/ENTITIES", "TYPE", "type");

        digester.addObjectCreate("RESPONSE/SYNTHESIS/RELEVANTS", Relevants.class);
        digester.addSetProperties("RESPONSE/SYNTHESIS/RELEVANTS", "TYPE", "type");

        digester.addObjectCreate("RESPONSE/SYNTHESIS/ENTITIES/ENTITY", Entity.class);
        digester.addSetProperties("RESPONSE/SYNTHESIS/ENTITIES/ENTITY", "NAME", "name");
        digester.addSetProperties("RESPONSE/SYNTHESIS/ENTITIES/ENTITY/PROPS/PROP", "NAME", "syncon");
        digester.addSetProperties("RESPONSE/SYNTHESIS/ENTITIES/ENTITY/PROPS/PROP", "VALUE", "syncon");
        digester.addSetNext("RESPONSE/SYNTHESIS/ENTITIES/ENTITY", "addCogitoEntity");

        digester.addSetNext("RESPONSE/SYNTHESIS/ENTITIES", "addEntities");

        digester.addObjectCreate("RESPONSE/SYNTHESIS/RELEVANTS/RELEVANT", Relevant.class);
        digester.addSetProperties("RESPONSE/SYNTHESIS/RELEVANTS/RELEVANT", "NAME", "name");
        digester.addSetProperties("RESPONSE/SYNTHESIS/RELEVANTS/RELEVANT", "SCORE", "score");
        digester.addSetNext("RESPONSE/SYNTHESIS/RELEVANTS/RELEVANT", "addCogitoRelevant");

        digester.addSetNext("RESPONSE/SYNTHESIS/RELEVANTS", "addRelevants");
    }

    @Override
    public String getNamespaceURI() {
        // cogito does not define any namespace.
        return null;
    }

}