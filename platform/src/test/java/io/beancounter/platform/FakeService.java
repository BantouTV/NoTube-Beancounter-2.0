package io.beancounter.platform;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import java.util.List;

@Path("test/fake")
public class FakeService {

    @GET
    @Path("/{id}")
    public void getWithOnePathParam(@PathParam("id") String id) {}

    @GET
    @Path("/list")
    public void getWithQueryParamList(@PathParam("list") List<String> list) {}
}
