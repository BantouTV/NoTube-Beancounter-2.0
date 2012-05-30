package tv.noube.crawler;

import tv.notube.commons.model.activity.Verb;
import tv.notube.commons.tests.DefaultRandomiser;

import java.util.Random;

/**
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
public class VerbRandomiser extends DefaultRandomiser<Verb> {

    private Random random = new Random();

    public VerbRandomiser(String name) {
        super(name);
    }

    @Override
    public Class<Verb> type() {
        return Verb.class;
    }

    @Override
    public Verb getRandom() {
        Verb[] verbs = Verb.values();
        int index = random.nextInt(verbs.length);
        return verbs[index];
    }
}