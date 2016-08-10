package com.electrum.billpaytestserver.handler;

import io.electrum.billpay.api.IRefundsResource;
import io.electrum.billpay.model.PaymentResponse;
import io.electrum.billpay.model.RefundRequest;
import io.electrum.billpay.model.RefundResponse;
import io.electrum.billpay.model.RefundReversal;
import io.electrum.vas.model.BasicAdvice;
import io.electrum.vas.model.LedgerAmount;

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
public class RefundResourceHandler extends BaseRequestHandler<RefundRequest,RefundResponse> implements IRefundsResource {
   private static final Logger log = LoggerFactory.getLogger(RefundResourceHandler.class);

   @Override
   public void confirmRefund(
         UUID adviceId,
         UUID refundId,
         BasicAdvice basicAdvice,
         SecurityContext securityContext,
         AsyncResponse asyncResponse,
         Request request,
         HttpServletRequest httpServletRequest,
         HttpHeaders httpHeaders,
         UriInfo uriInfo) {
      log.info("Handling refund confirm");
      handleConfirm(
            adviceId,
            refundId,
            basicAdvice,
            securityContext,
            asyncResponse,
            request,
            httpServletRequest,
            httpHeaders,
            uriInfo);
   }

   @Override
   public void createRefund(
         UUID uuid,
         RefundRequest refundRequest,
         SecurityContext securityContext,
         AsyncResponse asyncResponse,
         Request request,
         HttpServletRequest httpServletRequest,
         HttpHeaders httpHeaders,
         UriInfo uriInfo) {
      log.info("Handling refund request");
      handleMessage(
            uuid,
            refundRequest,
            securityContext,
            asyncResponse,
            request,
            httpServletRequest,
            httpHeaders,
            uriInfo);
   }

   @Override
   public void reverseRefund(
         UUID adviceId,
         UUID refundId,
         RefundReversal refundReversal,
         SecurityContext securityContext,
         AsyncResponse asyncResponse,
         Request request,
         HttpServletRequest httpServletRequest,
         HttpHeaders httpHeaders,
         UriInfo uriInfo) {
      log.info("Handling refund reversal");
      handleReversal(
            adviceId,
            refundId,
            refundReversal,
            securityContext,
            asyncResponse,
            request,
            httpServletRequest,
            httpHeaders,
            uriInfo);

   }

   protected void doConfirm(RefundRequest request) {

      PaymentResponse origPaymentResponse = MockBillPayBackend.getPaymentResponse(request.getIssuerReference());

      BillPayAccount account = MockBillPayBackend.getAccount(origPaymentResponse.getAccount().getAccountRef());

      LedgerAmount ledgerAmount = account.getBalance();

      long amount = ledgerAmount.getAmount();

      amount += origPaymentResponse.getResponseAmount().getAmount();

      ledgerAmount.setAmount(amount);
   }

   protected void doReversal(RefundRequest request) {
      PaymentResponse origPaymentResponse = MockBillPayBackend.getPaymentResponse(request.getIssuerReference());

      BillPayAccount account = MockBillPayBackend.getAccount(origPaymentResponse.getAccount().getAccountRef());

      LedgerAmount ledgerAmount = account.getBalance();

      long amount = ledgerAmount.getAmount();

      amount -= origPaymentResponse.getResponseAmount().getAmount();

      ledgerAmount.setAmount(amount);
   }

   protected RefundResponse getResponse(RefundRequest request, BillPayAccount account) {
      log.info("Constructing response");
      RefundResponse response = new RefundResponse();

      response.setId(request.getId());
      response.setTime(new DateTime());
      response.setSender(request.getSender());
      // response.setLinkData();
      response.setProcessor(getProcessor());
      response.setReceiver(getReceiver());
      response.setAccount(getAccount(account));
      response.setCustomer(account.getCustomer());

      try {
         log.debug(Utils.objectToPrettyPrintedJson(response));
      } catch (JsonProcessingException e) {
         log.error("Could not print response");
      }

      return response;
   }

}
