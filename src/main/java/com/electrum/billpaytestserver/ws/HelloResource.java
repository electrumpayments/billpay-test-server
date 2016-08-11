
package com.electrum.billpaytestserver.ws;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/hello")
public class HelloResource {
   @GET
   @Produces(MediaType.TEXT_PLAIN)
   public String sayHello() {
      return "Test server is up and running.";
   }

}
