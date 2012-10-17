package io.beancounter.analyser.examples;

import io.beancounter.analyser.analysis.Analysis;
import io.beancounter.analyser.analysis.AnalysisException;
import io.beancounter.commons.model.AnalysisResult;
import io.beancounter.commons.model.Interest;
import io.beancounter.commons.model.UserProfile;
import org.joda.time.DateTime;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class TrendingInterestsAnalysisTestCase {

    private TrendingInterestsAnalysis tia = new TrendingInterestsAnalysis();

    @Test
    public void testRun() throws URISyntaxException, AnalysisException {
        UserProfile userProfile = getProfile();
        AnalysisResult result = tia.run(userProfile, null); // first time it starts
        System.out.print(result);
        userProfile = getOtherProfile();
        result = tia.run(userProfile, result); // second time
        System.out.print(result);
    }

    private UserProfile getOtherProfile() throws URISyntaxException {
        UserProfile up = new UserProfile(UUID.randomUUID());
        up.setLastUpdated(DateTime.now());
        Set<Interest> interests = new HashSet<Interest>();
        Interest i1 = new Interest("interest-4", new URI("http://interest4.com"));
        i1.setWeight(0.6);
        interests.add(i1);
        Interest i2 = new Interest("interest-5", new URI("http://interest5.com"));
        i2.setWeight(0.3);
        interests.add(i2);
        Interest i3 = new Interest("interest-6", new URI("http://interest6.com"));
        i3.setWeight(0.1);
        interests.add(i3);
        up.setInterests(interests);
        return up;
    }

    private UserProfile getProfile() throws URISyntaxException {
        UserProfile up = new UserProfile(UUID.randomUUID());
        up.setLastUpdated(DateTime.now());
        Set<Interest> interests = new HashSet<Interest>();
        interests.add(new Interest("interest-1", new URI("http://interest1.com")));
        interests.add(new Interest("interest-2", new URI("http://interest2.com")));
        interests.add(new Interest("interest-3", new URI("http://interest3.com")));
        up.setInterests(interests);
        return up;
    }

}
