package io.beancounter.commons.tests.randomisers;

import io.beancounter.commons.tests.Randomiser;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class StringRandomiser implements Randomiser<String> {

    private String name;

    private int ngrams;

    private int length;

    private List<Character> chars = new ArrayList<Character>();

    private Random random = new Random();

    public StringRandomiser(String name, int ngrams, int length, boolean alphaOnly) {
        this.name = name;
        this.ngrams = ngrams;
        this.length = length;
        if(alphaOnly) {
            char[] charsPrimitive = "abcdefghijklmnopqrstuvwxyz".toCharArray();
            for(char c : charsPrimitive) {
                chars.add(c);
            }
        } else {
            for(int i=0; i <= 16384; i++) {
                if(Character.isDefined(i)) {
                    char[] cs = Character.toChars(i);
                    for(char c : cs) {
                        chars.add(c);
                    }
                }
            }
        }
    }

    public StringRandomiser(String name, int ngrams, int length) {
        this(name, ngrams, length, true);
    }

    public Class<String> type() {
        return String.class;
    }

    public String name() {
        return name;
    }

    public String getRandom() {
        // maximum two words
        int wordsNumber = random.nextInt(ngrams) + 1;
        String[] words = new String[wordsNumber];
        for(int i = 0; i < wordsNumber; i++) {
            // maximum 15 chars
            int length = random.nextInt(this.length) + 1;
            words[i] = getRandomString(length);
        }
        String result = "";
        for(String word : words) {
            result += word + ' ';
        }
        return result.substring(0, result.length() - 1);
    }

    private String getRandomString(int length) {
        String result = "";
        for(int i = 0; i < length; i++) {
            int index = random.nextInt(chars.size());
            result += chars.get(index);
        }
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StringRandomiser that = (StringRandomiser) o;

        if (name != null ? !name.equals(that.name) : that.name != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }
}
