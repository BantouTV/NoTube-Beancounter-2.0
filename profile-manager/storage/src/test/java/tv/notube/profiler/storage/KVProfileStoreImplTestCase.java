package tv.notube.profiler.storage;

import org.joda.time.DateTime;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import tv.notube.commons.configuration.Configurations;
import tv.notube.commons.configuration.ConfigurationsException;
import tv.notube.commons.configuration.storage.StorageConfiguration;
import tv.notube.commons.model.Interest;
import tv.notube.commons.model.Type;
import tv.notube.commons.model.UserProfile;
import tv.notube.commons.model.activity.Activity;
import tv.notube.commons.model.activity.Context;
import tv.notube.commons.model.activity.Verb;
import tv.notube.commons.storage.kvs.KVStore;
import tv.notube.commons.storage.kvs.configuration.ConfigurationManager;
import tv.notube.commons.storage.kvs.mybatis.MyBatisKVStore;
import tv.notube.commons.storage.model.fields.serialization.SerializationManager;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

/**
 * Reference test case for {@link KVProfileStoreImpl}.
 * 
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class KVProfileStoreImplTestCase {

    private ProfileStore profileStore;

    private static final String STORAGE_CONF = "storage-configuration.xml";

    @BeforeTest
    public void setUp() throws ConfigurationsException {
        StorageConfiguration storageConfiguration = Configurations.getConfiguration(
                STORAGE_CONF,
                StorageConfiguration.class
        );
        Properties properties = storageConfiguration.getKvsProperties();
        KVStore kVStore = new MyBatisKVStore(properties, new SerializationManager());
        profileStore = new KVProfileStoreImpl(kVStore);
    }

    @AfterTest
    public void tearDown() {
        profileStore = null;
    }

    @Test
    public void testCRUD() throws URISyntaxException, MalformedURLException, ProfileStoreException {
        final String username = "user-for-test";
        UserProfile expected = new UserProfile();
        expected.setUsername(username);
        expected.setVisibility(UserProfile.Visibility.PUBLIC);
        expected.setReference(new URI("http://notube.tv/profile/dpalmisano"));
        expected.setId(UUID.randomUUID());

        Activity a1 = new Activity();
        a1.setVerb(Verb.SHARE);

        tv.notube.commons.model.activity.Object o1 =
                new  tv.notube.commons.model.activity.Object();

        o1.setName("Why the Semantic Web is going to emerge");
        o1.setUrl(new URL("http://planetrdf.org/article1.html"));
        a1.setObject(o1);

        Context c1 = new Context();
        c1.setDate(new DateTime());
        c1.setService(new URL("http://facebook.com"));
        a1.setContext(c1);

        Interest i1 = new Interest();
        i1.setWeight(0.5f);
        i1.setResource(new URI("http://dbpedia.org/resource/Semantic_Web"));
        i1.setVisible(true);
        i1.setId(UUID.randomUUID());
        i1.addActivity(a1);

        Set<Interest> interests = new HashSet<Interest>();
        interests.add(i1);
        Type type = new Type(interests, 0.67);

        expected.addType(type);

        profileStore.storeUserProfile(expected);

        UserProfile actual = profileStore.getUserProfile(username);
        Assert.assertNotNull(actual);
        Assert.assertEquals(expected, actual);
        Assert.assertEquals(actual.getTypes().size(), 1);

        profileStore.deleteUserProfile(username);
        actual = profileStore.getUserProfile(username);
        Assert.assertNull(actual);

    }


}
