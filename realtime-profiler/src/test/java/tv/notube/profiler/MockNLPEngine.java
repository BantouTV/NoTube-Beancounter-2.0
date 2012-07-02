package tv.notube.profiler;

import tv.notube.commons.nlp.NLPEngine;
import tv.notube.commons.nlp.NLPEngineException;

import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.Random;

/**
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
public class MockNLPEngine implements NLPEngine {

    private Collection<URI> uris;

    private Random random = new Random();

    public MockNLPEngine() {
        uris = new HashSet<URI>();
    }

    @Override
    public Collection<URI> enrich(String text) throws NLPEngineException {
        try {
            for(int i = 0; i<random.nextInt(5);i++)
                uris.add(new URI(getRandomInterest()));
        }catch (Exception e ) {
            throw new NLPEngineException("Something went wrong in the MockEngine!",e);
        }
        HashSet<URI> temp = new HashSet<URI>();
        temp.addAll(uris);
        uris.removeAll(uris);
        System.out.println(temp);
        return temp;
    }

    @Override
    public Collection<URI> enrich(URL url) throws NLPEngineException {
        try {
            if(url.equals(new URL("http://www.bbc.co.uk/news/uk-18494541")))
                uris.add(new URI("BBC"));
        } catch (Exception e) {
            throw new NLPEngineException("Something went wrong in the MockEngine!",e);
        }
        return uris;
    }

    private String getRandomInterest() {
        String interest;
        int i = random.nextInt(15)+1;
        switch (i) {
            case 1 : interest = "BBC";
                break;
            case 2 : interest = "Doctor_Who";
                break;
            case 3 : interest = "London";
                break;
            case 4 : interest = "Sport";
                break;
            case 5 : interest = "Tennis";
                break;
            case 6 : interest = "Euro2012";
                break;
            case 7 : interest = "Semantic_Web";
                break;
            case 8 : interest = "Football";
                break;
            case 9 : interest = "Music";
                break;
            case 10 : interest = "Guitar";
                break;
            case 11 : interest = "Cars";
                break;
            case 12 : interest = "Swimming";
                break;
            case 13 : interest = "Java";
                break;
            case 14 : interest = "Apple";
                break;
            case 15 : interest = "Muse";
                break;
            default: interest = "BBC";
        }
        return interest;
    }
}