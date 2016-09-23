package com.electrum.billpaytestserver.ws;

import io.electrum.billpay.api.IRefundsResource;
import io.electrum.billpay.api.RefundsResource;

import javax.ws.rs.Path;

import com.electrum.billpaytestserver.handler.RefundResourceHandler;

@Path("/refunds/{refundId}")
public class RefundResource extends RefundsResource {

   IRefundsResource handler = new RefundResourceHandler();

   @Override
   protected IRefundsResource getResourceImplementation() {
      return handler;
   }
}
