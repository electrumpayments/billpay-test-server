package com.electrum.billpaytestserver.handler;

import io.electrum.billpay.api.IRefundsResource;
import io.electrum.billpay.model.RefundRequest;
import io.electrum.billpay.model.RefundReversal;
import io.electrum.vas.model.BasicAdvice;

import java.util.UUID;

import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

/**
 *
 */
public class RefundResourceHandler implements IRefundsResource {
   @Override public Response confirmRefund(
         UUID uuid,
         UUID uuid1,
         BasicAdvice basicAdvice,
         SecurityContext securityContext,
         AsyncResponse asyncResponse,
         HttpHeaders httpHeaders,
         UriInfo uriInfo) {
      return null;
   }

   @Override public Response createRefund(
         UUID uuid,
         RefundRequest refundRequest,
         SecurityContext securityContext,
         AsyncResponse asyncResponse,
         HttpHeaders httpHeaders,
         UriInfo uriInfo) {
      return null;
   }

   @Override public Response reverseRefund(
         UUID uuid,
         UUID uuid1,
         RefundReversal refundReversal,
         SecurityContext securityContext,
         AsyncResponse asyncResponse,
         HttpHeaders httpHeaders,
         UriInfo uriInfo) {
      return null;
   }
}
