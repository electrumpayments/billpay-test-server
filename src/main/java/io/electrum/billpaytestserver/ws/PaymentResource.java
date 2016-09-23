package io.electrum.billpaytestserver.ws;

import io.electrum.billpay.api.IPaymentsResource;
import io.electrum.billpay.api.PaymentsResource;
import io.electrum.billpaytestserver.handler.PaymentResourceHandler;

import javax.ws.rs.Path;

@Path("/payments/{paymentId}")
public class PaymentResource extends PaymentsResource {

   IPaymentsResource handler = new PaymentResourceHandler();

   @Override
   protected IPaymentsResource getResourceImplementation() {
      return handler;
   }
}
