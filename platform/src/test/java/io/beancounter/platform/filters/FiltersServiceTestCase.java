package io.beancounter.platform.filters;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.codehaus.jackson.map.ObjectMapper;
import org.testng.Assert;
import org.testng.annotations.*;
import io.beancounter.filter.model.Filter;
import io.beancounter.platform.APIResponse;
import io.beancounter.platform.AbstractJerseyTestCase;

import java.io.IOException;
import java.util.UUID;

/**
 * Reference test case for {@link io.beancounter.platform.FilterService}
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class FiltersServiceTestCase extends AbstractJerseyTestCase {

    private static String APIKEY;

    protected FiltersServiceTestCase() {
        super(9995);
    }

    private void deregisterTestApplication() throws IOException {
        HttpClient client = new HttpClient();
        String baseQuery = "application/" + APIKEY;
        DeleteMethod deleteMethod = new DeleteMethod(base_uri + baseQuery);
        client.executeMethod(deleteMethod);
    }

    private UUID registerTestApplication() throws IOException {
        String baseQuery = "application/register";
        final String name = "fake_application_name";
        final String description = "This is a test registration!";
        final String email = "fake_mail@test.com";
        final String oauth = "http://fakeUrlOAUTH";
        PostMethod postMethod = new PostMethod(base_uri + baseQuery);
        HttpClient client = new HttpClient();
        postMethod.addParameter("name", name);
        postMethod.addParameter("description", description);
        postMethod.addParameter("email", email);
        postMethod.addParameter("oauthCallback", oauth);
        client.executeMethod(postMethod);
        String responseBody = new String(postMethod.getResponseBody());
        APIResponse actual = fromJson(responseBody, APIResponse.class);
        return UUID.fromString(actual.getObject());
    }

    @BeforeMethod
    public void registerApp() throws Exception {
        APIKEY = registerTestApplication().toString();
    }

    @AfterMethod
    public void deregisterApp() throws IOException {
            deregisterTestApplication();
    }

    @Test
    public void testRegisterFilter() throws IOException {
        final String baseQuery = "filters/register/%s?apikey=%s";
        final String name = "social-event-filter";
        final String description = "this filter filters all the activities matching a given event";
        final String queue = "fake-queue";
        final String pattern = "{\n" +
                "    \"userId\": {\n" +
                "        \"uuid\": null\n" +
                "    },\n" +
                "    \"verb\": {\n" +
                "        \"verb\": \"ANY\"\n" +
                "    },\n" +
                "    \"object\": {\n" +
                "        \"type\": \"io.beancounter.filter.model.pattern.rai.TVEventPattern\",\n" +
                "        \"typePattern\": {\n" +
                "            \"string\": \"io.beancounter.filter.model.pattern.rai.TVEventPattern\"\n" +
                "        },\n" +
                "        \"url\": {\n" +
                "            \"url\": null\n" +
                "        },\n" +
                "        \"uuidPattern\": {\n" +
                "            \"uuid\": \"2590fd3d-97ea-49bb-b7ec-04da7553bb0a\"\n" +
                "        }\n" +
                "    },\n" +
                "    \"context\": {\n" +
                "        \"date\": {\n" +
                "            \"date\": 1342530815683,\n" +
                "            \"bool\": \"BEFORE\"\n" +
                "        },\n" +
                "        \"service\": {\n" +
                "            \"string\": \"\"\n" +
                "        },\n" +
                "        \"mood\": {\n" +
                "            \"string\": \"\"\n" +
                "        },\n" +
                "        \"username\": {\n" +
                "            \"string\": \"\"\n" +
                "        }\n" +
                "    }\n" +
                "}";
        final String query = String.format(
                baseQuery,
                name,
                APIKEY
        );
        PostMethod postMethod = new PostMethod(base_uri + query);
        HttpClient client = new HttpClient();
        postMethod.addParameter("pattern", pattern);
        postMethod.addParameter("description", description);
        postMethod.addParameter("queue", queue);
        int result = client.executeMethod(postMethod);
        String responseBody = new String(postMethod.getResponseBody());
        logger.info("result code: " + result);
        logger.info("response body: " + responseBody);
        APIResponse actual = fromJson(responseBody, APIResponse.class);
        APIResponse expected = new APIResponse(
                null,
                "filter [social-event-filter] successfully registered",
                "OK"
        );
        Assert.assertEquals(actual.getMessage(), expected.getMessage());
        Assert.assertEquals(actual.getStatus(), expected.getStatus());
    }

    @Test(dependsOnMethods = ("testRegisterFilter"))
    public void getFilter() throws IOException {
        final String baseQuery = "filters/%s?apikey=%s";
        final String name = "social-event-filter";
        final String query = String.format(
                baseQuery,
                name,
                APIKEY
        );
        GetMethod getMethod = new GetMethod(base_uri + query);
        HttpClient client = new HttpClient();
        int result = client.executeMethod(getMethod);
        String responseBody = new String(getMethod.getResponseBody());
        logger.info("result code: " + result);
        Assert.assertEquals(result, 200);
        logger.info("response body: " + responseBody);
    }

    @Test(dependsOnMethods = ("testRegisterFilter"))
    public void getFilterNames() throws IOException {
        final String baseQuery = "filters/list/all?apikey=%s";
        final String query = String.format(
                baseQuery,
                APIKEY
        );
        GetMethod getMethod = new GetMethod(base_uri + query);
        HttpClient client = new HttpClient();
        int result = client.executeMethod(getMethod);
        String responseBody = new String(getMethod.getResponseBody());
        logger.info("result code: " + result);
        Assert.assertEquals(result, 200);
        logger.info("response body: " + responseBody);
    }

    @Test
    public void testSerialization() throws IOException {
        final String str = "{\n" +
                "    \"name\": \"test-filter\",\n" +
                "    \"description\": \"test-description\",\n" +
                "    \"definedAt\": 1342546262929,\n" +
                "    \"active\": false,\n" +
                "    \"activityPattern\": {\n" +
                "        \"userId\": {\n" +
                "            \"uuid\": null\n" +
                "        },\n" +
                "        \"verb\": {\n" +
                "            \"verb\": \"ANY\"\n" +
                "        },\n" +
                "        \"object\": {\n" +
                "            \"type\": \"io.beancounter.filter.model.pattern.rai.TVEventPattern\",\n" +
                "            \"typePattern\": {\n" +
                "                \"string\": \"io.beancounter.filter.model.pattern.rai.TVEventPattern\"\n" +
                "            },\n" +
                "            \"url\": {\n" +
                "                \"url\": null\n" +
                "            },\n" +
                "            \"uuidPattern\": {\n" +
                "                \"uuid\": \"2590fd3d-97ea-49bb-b7ec-04da7553bb0a\"\n" +
                "            }\n" +
                "        },\n" +
                "        \"context\": {\n" +
                "            \"date\": {\n" +
                "                \"date\": 1342530815683,\n" +
                "                \"bool\": \"BEFORE\"\n" +
                "            },\n" +
                "            \"service\": {\n" +
                "                \"string\": \"\"\n" +
                "            },\n" +
                "            \"mood\": {\n" +
                "                \"string\": \"\"\n" +
                "            },\n" +
                "            \"username\": {\n" +
                "                \"string\": \"\"\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "}";
        ObjectMapper mapper = new ObjectMapper();
        Filter f;
        f = mapper.readValue(str, Filter.class);
        Assert.assertNotNull(f);
    }

}