package io.electrum.billpaytestserver.ws;

import io.electrum.billpay.api.AccountLookupsResource;
import io.electrum.billpay.api.IAccountLookupsResource;
import io.electrum.billpaytestserver.handler.AccountLookResourceHandler;

import javax.ws.rs.Path;

@Path("/accountLookups/{requestId}")
public class AccountLookupResource extends AccountLookupsResource {
   IAccountLookupsResource handler = new AccountLookResourceHandler();

   @Override
   protected IAccountLookupsResource getResourceImplementation() {
      return handler;
   }

}
