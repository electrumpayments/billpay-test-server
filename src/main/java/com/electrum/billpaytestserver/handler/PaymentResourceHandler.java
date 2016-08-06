package com.electrum.billpaytestserver.handler;

import io.electrum.billpay.api.IPaymentsResource;
import io.electrum.billpay.model.PaymentRequest;
import io.electrum.billpay.model.PaymentResponse;
import io.electrum.billpay.model.PaymentReversal;
import io.electrum.vas.model.TenderAdvice;

import java.util.UUID;

import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.electrum.billpaytestserver.Utils;
import com.electrum.billpaytestserver.account.BillPayAccount;
import com.fasterxml.jackson.core.JsonProcessingException;

/**
 *
 */
public class PaymentResourceHandler
      extends BaseDualRequestHandler<PaymentRequest, PaymentResponse, TenderAdvice, PaymentReversal>
      implements IPaymentsResource {
   private static final Logger log = LoggerFactory.getLogger(AccountLookResourceHandler.class);

   @Override
   public Response confirmPayment(
         UUID uuid,
         UUID uuid1,
         TenderAdvice tenderAdvice,
         SecurityContext securityContext,
         AsyncResponse asyncResponse,
         HttpHeaders httpHeaders,
         UriInfo uriInfo) {
      return null;
   }

   @Override
   public Response createPayment(
         UUID uuid,
         PaymentRequest paymentRequest,
         SecurityContext securityContext,
         AsyncResponse asyncResponse,
         HttpHeaders httpHeaders,
         UriInfo uriInfo) {
      log.info("Handling payment request");
      return handleMessage(uuid.toString(), paymentRequest, securityContext, asyncResponse, httpHeaders, uriInfo);
   }

   @Override
   public Response reversePayment(
         UUID uuid,
         UUID uuid1,
         PaymentReversal paymentReversal,
         SecurityContext securityContext,
         AsyncResponse asyncResponse,
         HttpHeaders httpHeaders,
         UriInfo uriInfo) {
      return null;
   }

   protected PaymentResponse getResponse(PaymentRequest request, BillPayAccount account) {
      log.info("Constructing response");
      PaymentResponse response = new PaymentResponse();

      response.setId(request.getId());
      response.setTime(new DateTime());
      response.setSender(request.getSender());
      // response.setLinkData();
      response.setProcessor(getProcessor());
      response.setReceiver(getReceiver());
      response.setAccount(getAccount(account));
      response.setCustomer(account.getCustomer());
      response.setSlipData(getSlipData());

      // add response amount

      try {
         log.debug(Utils.objectToPrettyPrintedJson(response));
      } catch (JsonProcessingException e) {
         log.error("Could not print response");
      }

      return response;
   }

}
