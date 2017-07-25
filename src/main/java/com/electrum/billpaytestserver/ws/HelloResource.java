
package com.electrum.billpaytestserver.ws;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import com.electrum.billpaytestserver.engine.MockBillPayBackend;

@Path("/billpay/v4")
public class HelloResource {
   @GET
   @Produces(MediaType.TEXT_PLAIN)
   public String sayHello() {
      return "Test server is up and running. Server was last reset at: "
            + (MockBillPayBackend.getLastResetTime() == null ? "Never" : MockBillPayBackend.getLastResetTime())
            + ". Contact support@electrum.co.za for assistance.";
   }

}
