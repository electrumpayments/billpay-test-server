package io.electrum.billpaytestserver.ws;

import io.electrum.billpay.api.IRefundsResource;
import io.electrum.billpay.api.RefundsResource;
import io.electrum.billpaytestserver.handler.RefundResourceHandler;

import javax.ws.rs.Path;

@Path("/refunds/{refundId}")
public class RefundResource extends RefundsResource {

   IRefundsResource handler = new RefundResourceHandler();

   @Override
   protected IRefundsResource getResourceImplementation() {
      return handler;
   }
}
