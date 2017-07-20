package com.electrum.billpaytestserver.handler;

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
import com.electrum.billpaytestserver.engine.ErrorDetailFactory;
import com.electrum.billpaytestserver.engine.MockBillPayBackend;
import com.fasterxml.jackson.core.JsonProcessingException;

import io.electrum.billpay.api.IRefundsResource;
import io.electrum.billpay.model.ErrorDetail;
import io.electrum.billpay.model.PaymentResponse;
import io.electrum.billpay.model.RefundRequest;
import io.electrum.billpay.model.RefundResponse;
import io.electrum.vas.model.Amounts;
import io.electrum.vas.model.BasicAdvice;
import io.electrum.vas.model.BasicReversal;
import io.electrum.vas.model.LedgerAmount;

/**
 *
 */
public class RefundResourceHandler extends BaseRequestHandler<RefundRequest, RefundResponse>
      implements IRefundsResource {
   private static final Logger log = LoggerFactory.getLogger(RefundResourceHandler.class);

   @Override
   public void confirmRefund(
         String adviceId,
         String refundId,
         BasicAdvice basicAdvice,
         SecurityContext securityContext,
         AsyncResponse asyncResponse,
         Request request,
         HttpServletRequest httpServletRequest,
         HttpHeaders httpHeaders,
         UriInfo uriInfo) {
      log.info("Handling refund confirm");
      try {
         handleConfirm(
               adviceId,
               refundId,
               basicAdvice,
               securityContext,
               asyncResponse,
               request,
               httpServletRequest,
               httpHeaders,
               uriInfo,
               false);
      } catch (Exception e) {
         log.error("Error handling message", e);
         asyncResponse.resume(ErrorDetailFactory.getServerErrorErrorDetail(e, ErrorDetail.RequestType.REFUND_CONFIRMATION, basicAdvice.getId(), basicAdvice.getRequestId()));
      }
   }

   @Override
   public void createRefund(
         String uuid,
         RefundRequest refundRequest,
         SecurityContext securityContext,
         AsyncResponse asyncResponse,
         Request request,
         HttpServletRequest httpServletRequest,
         HttpHeaders httpHeaders,
         UriInfo uriInfo) {
      log.info("Handling refund request");
      try {
         handleMessage(
               uuid,
               refundRequest,
               securityContext,
               asyncResponse,
               request,
               httpServletRequest,
               httpHeaders,
               uriInfo);
      } catch (Exception e) {
         log.error("Error handling message", e);
         asyncResponse.resume(ErrorDetailFactory.getServerErrorErrorDetail(e, ErrorDetail.RequestType.REFUND_REQUEST, refundRequest.getId(), null));
      }
   }

   @Override
   public void reverseRefund(
         String adviceId,
         String refundId,
         BasicReversal refundReversal,
         SecurityContext securityContext,
         AsyncResponse asyncResponse,
         Request request,
         HttpServletRequest httpServletRequest,
         HttpHeaders httpHeaders,
         UriInfo uriInfo) {
      log.info("Handling refund reversal");
      try {
         handleReversal(
               adviceId,
               refundId,
               refundReversal,
               securityContext,
               asyncResponse,
               request,
               httpServletRequest,
               httpHeaders,
               uriInfo,
               false);
      } catch (Exception e) {
         log.error("Error handling message", e);
         asyncResponse.resume(ErrorDetailFactory.getServerErrorErrorDetail(e, ErrorDetail.RequestType.REFUND_REVERSAL, refundReversal.getId(), refundReversal.getRequestId()));
      }

   }

   protected void doConfirm(RefundRequest request) {

      PaymentResponse origPaymentResponse = MockBillPayBackend.getPaymentResponse(request.getIssuerReference());

      BillPayAccount account = MockBillPayBackend.getAccount(origPaymentResponse.getAccount().getAccountRef());

      LedgerAmount ledgerAmount = account.getBalance();

      long amount = ledgerAmount.getAmount();

      amount += origPaymentResponse.getAmounts().getApprovedAmount().getAmount();

      ledgerAmount.setAmount(amount);
   }

   protected void doReversal(RefundRequest request) {
      PaymentResponse origPaymentResponse = MockBillPayBackend.getPaymentResponse(request.getIssuerReference());

      BillPayAccount account = MockBillPayBackend.getAccount(origPaymentResponse.getAccount().getAccountRef());

      LedgerAmount ledgerAmount = account.getBalance();

      long amount = ledgerAmount.getAmount();

      amount -= origPaymentResponse.getAmounts().getApprovedAmount().getAmount();

      ledgerAmount.setAmount(amount);
   }

   protected RefundResponse getResponse(RefundRequest request, BillPayAccount account) {
      log.info("Constructing response");
      RefundResponse response = new RefundResponse();

      response.setId(request.getId());
      response.setTime(new DateTime());
      response.setOriginator(request.getOriginator());
      response.setClient(getClient());
      response.setSettlementEntity(getSettlementEntity());
      response.setReceiver(getReceiver());
      response.setAccount(getAccount(account));
      response.setAmounts(new Amounts().balanceAmount(account.getBalance()));
      response.setCustomer(account.getCustomer());
      response.setThirdPartyIdentifiers(getThirdPartyIdentifiers(request.getThirdPartyIdentifiers()));

      try {
         log.debug(Utils.objectToPrettyPrintedJson(response));
      } catch (JsonProcessingException e) {
         log.error("Could not print response");
      }

      return response;
   }

}
