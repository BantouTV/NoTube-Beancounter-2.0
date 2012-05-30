package tv.notube.extension.profilingline.musicbrainz;

import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import tv.notube.extension.profilingline.lupedia.DefaultLupediaImpl;
import tv.notube.extension.profilingline.lupedia.Lupedia;
import tv.notube.extension.profilingline.lupedia.LupediaException;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

/**
 * Reference test for {@link tv.notube.extension.profilingline.lupedia.DefaultLupediaImpl} class.
 *
 * @author Davide Palmisano (dpalmisano@gmail.com)
 */
public class MusicBrainzLookupTestCase {

    private MusicBrainzLookup musicbrainz;

    @BeforeTest
    public void setUp() {
        musicbrainz = new MusicBrainzLookup();
    }

    @Test(enabled = false)
    public void testGetResources() throws URISyntaxException, MusicBrainzLooupException {
        URI uri = musicbrainz.resolve("244afcb7-fa9a-49b1-9aa6-0149512d1c52");
        Assert.assertNotNull(uri);
        Assert.assertEquals(uri, new URI("http://dbpedia.org/resource/Thrice"));
    }

}
