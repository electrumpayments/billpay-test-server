package com.electrum.billpaytestserver.ws;

import io.electrum.billpay.api.IPaymentsResource;
import io.electrum.billpay.api.PaymentsResource;

import com.electrum.billpaytestserver.handler.PaymentResourceHandler;

/**
 *
 */
public class PaymentResource extends PaymentsResource {

   IPaymentsResource handler = new PaymentResourceHandler();
   @Override protected IPaymentsResource getResourceImplementation() {
      return handler;
   }
}
