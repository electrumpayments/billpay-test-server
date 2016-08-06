package com.electrum.billpaytestserver.ws;

import io.electrum.billpay.api.AccountLookupsResource;
import io.electrum.billpay.api.IAccountLookupsResource;

import com.electrum.billpaytestserver.handler.AccountLookResourceHandler;


public class AccountLookupResource extends AccountLookupsResource {
   IAccountLookupsResource handler = new AccountLookResourceHandler();

   @Override
   protected IAccountLookupsResource getResourceImplementation() {
      System.out.println("fuck");
      return handler;
   }

}
