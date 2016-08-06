
package com.electrum.billpaytestserver.ws;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;


@Path("/hello")
public class HelloResource {
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String sayHello() {
        return "Hello World PLAIN text";
    }
    
    @GET
    @Produces(MediaType.TEXT_XML)
    public String sayXMLHello() {
        return "<?xml version=\"1.0\"?><hello> Hello World XML, YAY!!!</hello>";
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public String sayHtmlHello() {
        return "<html><title>Hello World HTML</title><body><h1>Hello World HTML</body></h1></html>";
    }
    
    @POST
    @Consumes({"text/xml", "text/plain", MediaType.TEXT_HTML})
    @Produces(MediaType.TEXT_PLAIN)
    public String sayPostHello() {
        return "Hello World Post!";
    }
}

