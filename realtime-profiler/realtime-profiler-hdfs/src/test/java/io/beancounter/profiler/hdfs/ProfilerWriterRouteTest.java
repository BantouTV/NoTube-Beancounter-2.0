package io.beancounter.profiler.hdfs;

import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import io.beancounter.commons.model.UserProfile;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.testng.CamelTestSupport;
import org.codehaus.jackson.map.ObjectMapper;
import org.joda.time.DateTime;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.UUID;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class ProfilerWriterRouteTest extends CamelTestSupport {

    private Injector injector;
    private ProfileWriter profileWriter;
    private ObjectMapper mapper;

    @BeforeMethod
    public void setUp() throws Exception {
        mapper = new ObjectMapper();
        profileWriter = mock(ProfileWriter.class);
        injector = Guice.createInjector(new Module() {
            @Override
            public void configure(Binder binder) {
                binder.bind(ProfileWriter.class).toInstance(profileWriter);
                binder.bind(ProfilerWriterRoute.class).toInstance(new ProfilerWriterRoute() {
                    @Override
                    protected String fromKestrel() {
                        return "direct:start";
                    }

                    @Override
                    public String errorEndpoint() {
                        return "mock:error";
                    }
                });
            }
        });

        super.setUp();
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return injector.getInstance(ProfilerWriterRoute.class);
    }

    @Test
    public void errorIsHandledWhenMessageBodyIsNotAValidUserProfile() throws Exception {
        MockEndpoint error = getMockEndpoint("mock:error");
        error.expectedMessageCount(2);

        template.sendBody("direct:start", "{\"userId\":\"5609618e-7ff4-41bd-9972-0ed2bc5955f1\",\"profile\":{\"id\":\"abd4c9ba-f479-42cc-96e1-49729aa115f4\"}}");
        template.sendBody("direct:start", "{\"userId\":\"5609618f-7ff4-41bd-9972-0ed2bc5955f1\",\"data\":{\"name\":\"Bob\"}}");

        error.assertIsSatisfied();
    }

    @Test
    public void validUserProfileJsonShouldBeUnmarshalledCorrectly() throws Exception {
        UserProfile profile = new UserProfile("test-user");
        profile.setLastUpdated(DateTime.now());

        MockEndpoint error = getMockEndpoint("mock:error");
        error.expectedMessageCount(0);

        template.sendBody("direct:start", mapper.writeValueAsString(profile));

        error.assertIsSatisfied();
    }

    @Test
    public void validUserProfileShouldBeWrittenToStorage() throws Exception {
        UserProfile profile = new UserProfile("test-user");
        profile.setLastUpdated(DateTime.now());
        UUID applicationId = UUID.fromString("18b70337-c7f0-4c9b-a38f-1d6dfddc6b22");

        MockEndpoint error = getMockEndpoint("mock:error");
        error.expectedMessageCount(0);

        template.sendBody("direct:start", mapper.writeValueAsString(profile));

        error.assertIsSatisfied();
        verify(profileWriter).write(applicationId, profile);
    }

    @Test
    public void givenErrorOccursWhenWritingProfileThenHandleException() throws Exception {
        UserProfile profile = new UserProfile("test-user");
        profile.setLastUpdated(DateTime.now());
        UUID applicationId = UUID.fromString("18b70337-c7f0-4c9b-a38f-1d6dfddc6b22");

        doThrow(new ProfileWriterException("Uh-oh"))
                .when(profileWriter).write(applicationId, profile);

        MockEndpoint error = getMockEndpoint("mock:error");
        error.expectedMessageCount(1);

        template.sendBody("direct:start", mapper.writeValueAsString(profile));

        error.assertIsSatisfied();
    }
}
