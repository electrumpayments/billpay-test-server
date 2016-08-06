package com.electrum.billpaytestserver.handler;

import io.electrum.billpay.api.IPaymentsResource;
import io.electrum.billpay.model.PaymentRequest;
import io.electrum.billpay.model.PaymentReversal;
import io.electrum.vas.model.TenderAdvice;

import java.util.UUID;

import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

/**
 *
 */
public class PaymentResourceHandler implements IPaymentsResource {
   @Override public Response confirmPayment(
         UUID uuid,
         UUID uuid1,
         TenderAdvice tenderAdvice,
         SecurityContext securityContext,
         AsyncResponse asyncResponse,
         HttpHeaders httpHeaders,
         UriInfo uriInfo) {
      return null;
   }

   @Override public Response createPayment(
         UUID uuid,
         PaymentRequest paymentRequest,
         SecurityContext securityContext,
         AsyncResponse asyncResponse,
         HttpHeaders httpHeaders,
         UriInfo uriInfo) {
      return null;
   }

   @Override public Response reversePayment(
         UUID uuid,
         UUID uuid1,
         PaymentReversal paymentReversal,
         SecurityContext securityContext,
         AsyncResponse asyncResponse,
         HttpHeaders httpHeaders,
         UriInfo uriInfo) {
      return null;
   }
}
