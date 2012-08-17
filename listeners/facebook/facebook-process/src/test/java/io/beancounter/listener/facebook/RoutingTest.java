package io.beancounter.listener.facebook;

import org.apache.camel.EndpointInject;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.testng.CamelTestSupport;

/**
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
public class RoutingTest extends CamelTestSupport {

    @EndpointInject(uri = "mock:getEndpoint")
    private MockEndpoint getEndpoint;

    @EndpointInject(uri = "mock:posttEndpoint")
    private MockEndpoint postEndpoint;



}