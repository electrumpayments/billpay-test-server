package com.electrum.billpaytestserver.ws;

import io.electrum.billpay.api.AccountLookupsResource;
import io.electrum.billpay.api.IAccountLookupsResource;

import javax.ws.rs.Path;

import com.electrum.billpaytestserver.handler.AccountLookResourceHandler;

@Path("/accountLookups/{requestId}")
public class AccountLookupResource extends AccountLookupsResource {
   IAccountLookupsResource handler = new AccountLookResourceHandler();

   @Override
   protected IAccountLookupsResource getResourceImplementation() {
      return handler;
   }

}
