package com.electrum.billpaytestserver.ws;

import io.electrum.billpay.api.IRefundsResource;
import io.electrum.billpay.api.RefundsResource;

import com.electrum.billpaytestserver.handler.RefundResourceHandler;

/**
 *
 */
public class RefundResource extends RefundsResource {

   IRefundsResource handler = new RefundResourceHandler();

   @Override
   protected IRefundsResource getResourceImplementation() {
      return handler;
   }
}
