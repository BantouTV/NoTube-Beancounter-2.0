package io.beancounter.commons.model.randomisers;

import java.util.Random;

import io.beancounter.commons.model.activity.Verb;
import io.beancounter.commons.tests.Randomiser;

public class VerbRandomizer implements Randomiser<Verb> {
    private Random random = new Random();
    private String name;

    public VerbRandomizer(String name) {
        this.name = name;
    }

    @Override
    public Class<Verb> type() {
        return Verb.class;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public Verb getRandom() {
        return Verb.values()[random.nextInt(Verb.values().length)];
    }
}
