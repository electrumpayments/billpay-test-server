package com.electrum.billpaytestserver.ws;

import io.electrum.billpay.api.IPaymentsResource;
import io.electrum.billpay.api.PaymentsResource;

import javax.ws.rs.Path;

import com.electrum.billpaytestserver.handler.PaymentResourceHandler;

@Path("/billpay/v4/payments/")
public class PaymentResource extends PaymentsResource {

   IPaymentsResource handler = new PaymentResourceHandler();

   @Override
   protected IPaymentsResource getResourceImplementation() {
      return handler;
   }
}
