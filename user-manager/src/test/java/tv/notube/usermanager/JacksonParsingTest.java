package tv.notube.usermanager;

import junit.framework.Assert;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import tv.notube.commons.model.User;
import tv.notube.commons.model.auth.Auth;
import tv.notube.commons.model.auth.OAuthAuth;
import tv.notube.commons.model.auth.SimpleAuth;
import tv.notube.commons.tests.Tests;
import tv.notube.commons.tests.TestsBuilder;
import tv.notube.commons.tests.TestsException;

import java.io.IOException;
import java.util.HashMap;

/**
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
public class JacksonParsingTest {

    private Tests tests;

    @BeforeClass
    public void setUp() {
        tests = TestsBuilder.getInstance().build();
    }

    @Test
    public void testValidParsing() throws TestsException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        User actual = getUser();
        String json = mapper.writeValueAsString(actual);
        Assert.assertNotNull(json);
        User expected = mapper.readValue(json, User.class);
        Assert.assertNotNull(expected);
        Assert.assertEquals(actual, expected);
        expected.setServices(getServices());
        String jsonWithService = mapper.writeValueAsString(expected);
        Assert.assertNotNull(jsonWithService);
        User withServices = mapper.readValue(jsonWithService, User.class);
        Assert.assertEquals(expected, withServices);
    }

    private User getUser() throws TestsException {
        User user = tests.build(User.class).getObject();
        return user;
    }

    private HashMap<String, Auth> getServices() throws TestsException {
        Auth a1 = new SimpleAuth("session", "username");
        Auth a2 = new OAuthAuth("session", "secret");
        HashMap<String, Auth> services = new HashMap<String, Auth>();
        services.put("Twitter", a1);
        services.put("Facebook", a2);
        return services;
    }

}