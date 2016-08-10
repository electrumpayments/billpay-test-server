package com.electrum.billpaytestserver.handler;

import io.electrum.billpay.api.IPaymentsResource;
import io.electrum.billpay.model.PaymentRequest;
import io.electrum.billpay.model.PaymentResponse;
import io.electrum.billpay.model.PaymentReversal;
import io.electrum.vas.model.LedgerAmount;
import io.electrum.vas.model.TenderAdvice;

import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.electrum.billpaytestserver.Utils;
import com.electrum.billpaytestserver.account.BillPayAccount;
import com.electrum.billpaytestserver.engine.MockBillPayBackend;
import com.fasterxml.jackson.core.JsonProcessingException;

/**
 *
 */
public class PaymentResourceHandler
      extends BaseDualRequestHandler<PaymentRequest, PaymentResponse, TenderAdvice, PaymentReversal>
      implements IPaymentsResource {
   private static final Logger log = LoggerFactory.getLogger(PaymentResourceHandler.class);

   @Override
   public void confirmPayment(
         UUID adviceId,
         UUID paymentId,
         TenderAdvice tenderAdvice,
         SecurityContext securityContext,
         AsyncResponse asyncResponse,
         Request request,
         HttpServletRequest httpServletRequest,
         HttpHeaders httpHeaders,
         UriInfo uriInfo) {
      log.info("Handling payment confirm");
      handleConfirm(
            adviceId,
            paymentId,
            tenderAdvice,
            securityContext,
            asyncResponse,
            request,
            httpServletRequest,
            httpHeaders,
            uriInfo);
   }

   @Override
   public void createPayment(
         UUID uuid,
         PaymentRequest paymentRequest,
         SecurityContext securityContext,
         AsyncResponse asyncResponse,
         Request request,
         HttpServletRequest httpServletRequest,
         HttpHeaders httpHeaders,
         UriInfo uriInfo) {
      log.info("Handling payment request");
      handleMessage(
            uuid,
            paymentRequest,
            securityContext,
            asyncResponse,
            request,
            httpServletRequest,
            httpHeaders,
            uriInfo);
   }

   @Override
   public void reversePayment(
         UUID adviceId,
         UUID paymentId,
         PaymentReversal paymentReversal,
         SecurityContext securityContext,
         AsyncResponse asyncResponse,
         Request request,
         HttpServletRequest httpServletRequest,
         HttpHeaders httpHeaders,
         UriInfo uriInfo) {
      log.info("Handling payment reversal");
      handleReversal(
            adviceId,
            paymentId,
            paymentReversal,
            securityContext,
            asyncResponse,
            request,
            httpServletRequest,
            httpHeaders,
            uriInfo);
   }

   protected void doConfirm(PaymentRequest request) {
      BillPayAccount account = MockBillPayBackend.getAccount(request.getAccountRef());

      LedgerAmount ledgerAmount = account.getBalance();

      long amount = ledgerAmount.getAmount();

      amount -= request.getRequestAmount().getAmount();

      ledgerAmount.setAmount(amount);
   }

   protected void doReversal(PaymentRequest request) {
      BillPayAccount account = MockBillPayBackend.getAccount(request.getAccountRef());

      LedgerAmount ledgerAmount = account.getBalance();

      long amount = ledgerAmount.getAmount();

      amount += request.getRequestAmount().getAmount();

      ledgerAmount.setAmount(amount);
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
      response.setResponseAmount(request.getRequestAmount());

      try {
         log.debug(Utils.objectToPrettyPrintedJson(response));
      } catch (JsonProcessingException e) {
         log.error("Could not print response");
      }

      return response;
   }

}
