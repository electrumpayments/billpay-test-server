package com.electrum.billpaytestserver.handler;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.core.*;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.electrum.billpaytestserver.Utils;
import com.electrum.billpaytestserver.account.BillPayAccount;
import com.electrum.billpaytestserver.engine.ErrorDetailFactory;
import com.electrum.billpaytestserver.engine.MockBillPayBackend;
import com.electrum.billpaytestserver.validation.BillpayMessageValidator;
import com.electrum.billpaytestserver.validation.ValidationResult;
import com.fasterxml.jackson.core.JsonProcessingException;

import io.electrum.billpay.api.IRefundsResource;
import io.electrum.billpay.model.ErrorDetail;
import io.electrum.billpay.model.PaymentResponse;
import io.electrum.billpay.model.RefundRequest;
import io.electrum.billpay.model.RefundResponse;
import io.electrum.vas.model.*;

/**
 *
 */
public class RefundResourceHandler extends BaseRequestHandler implements IRefundsResource {
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
         handleConfirm(adviceId, refundId, basicAdvice, asyncResponse, ErrorDetail.RequestType.REFUND_CONFIRMATION);
      } catch (Exception e) {
         log.error("Error handling message", e);
         asyncResponse.resume(ErrorDetailFactory.getServerErrorErrorDetail(
               e,
               ErrorDetail.RequestType.REFUND_CONFIRMATION,
               basicAdvice.getId(),
               basicAdvice.getRequestId()));
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
         handleMessage(refundRequest, asyncResponse, ErrorDetail.RequestType.REFUND_REQUEST);
      } catch (Exception e) {
         log.error("Error handling message", e);
         asyncResponse.resume(
               ErrorDetailFactory.getServerErrorErrorDetail(
                     e,
                     ErrorDetail.RequestType.REFUND_REQUEST,
                     refundRequest.getId(),
                     null));
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
         handleReversal(adviceId, refundId, refundReversal, asyncResponse, ErrorDetail.RequestType.REFUND_REVERSAL);
      } catch (Exception e) {
         log.error("Error handling message", e);
         asyncResponse.resume(ErrorDetailFactory.getServerErrorErrorDetail(
               e,
               ErrorDetail.RequestType.REFUND_REVERSAL,
               refundReversal.getId(),
               refundReversal.getRequestId()));
      }

   }

   protected void handleConfirm(
         String adviceId,
         String requestId,
         BasicAdvice advice,
         AsyncResponse asyncResponse,
         ErrorDetail.RequestType requestType) throws Exception {

      BasicReversal reversal = MockBillPayBackend.getRequestReversal(requestId);
      if (reversal != null) {
         asyncResponse.resume(
               ErrorDetailFactory.getPreviousAdviceReceivedErrorDetail(
                     reversal,
                     requestType,
                     advice.getId(),
                     advice.getRequestId()));
         return;
      }

      BasicAdvice prevAdvice = MockBillPayBackend.getRequestConfirmation(requestId);
      if (prevAdvice != null) {
         asyncResponse.resume(
               ErrorDetailFactory.getPreviousAdviceReceivedErrorDetail(
                     prevAdvice,
                     requestType,
                     advice.getId(),
                     advice.getRequestId()));
         return;
      }

      if (!validateAndPersist(advice, asyncResponse)) {
         return;
      }

      RefundRequest origRequest = (RefundRequest) MockBillPayBackend.getRequest(requestId);
      if (origRequest != null) {
         try {
            doConfirm(origRequest);
         } catch (ClassCastException e) {
            log.error("Request type and advice type incompatible");
            log.info("Removing advice message of ID {}", adviceId);
            MockBillPayBackend.removeMessage(adviceId);
            asyncResponse.resume(
                  ErrorDetailFactory.getMismatchingRequestAndAdviceErrorDetail(
                        requestId,
                        requestType,
                        advice.getId(),
                        advice.getRequestId()));
            return;
         }
      }

      BasicAdviceResponse adviceResponse = new BasicAdviceResponse();
      adviceResponse.setId(advice.getId());
      adviceResponse.setRequestId(advice.getRequestId());
      adviceResponse.setThirdPartyIdentifiers(advice.getThirdPartyIdentifiers());
      adviceResponse.setTime(advice.getTime());
      asyncResponse.resume(Response.status(Response.Status.ACCEPTED).entity(adviceResponse).build());
   }

   protected void handleMessage(RefundRequest request, AsyncResponse asyncResponse, ErrorDetail.RequestType requestType)
         throws Exception {

      if (!validateAndPersist(request, asyncResponse)) {
         return;
      }

      BillPayAccount account = null;

      PaymentResponse paymentResponse = MockBillPayBackend.getPaymentResponse((request).getIssuerReference());

      if (paymentResponse == null) {
         asyncResponse.resume(
               ErrorDetailFactory.getNoPaymentRequestFoundErrorDetail(
                     (request).getIssuerReference(),
                     requestType,
                     request.getId(),
                     null));
         return;
      }

      account = MockBillPayBackend.getAccount(paymentResponse.getAccount().getAccountRef());

      if (account == null) {
         asyncResponse.resume(
               ErrorDetailFactory.getNoAccountFoundErrorDetail(
                     paymentResponse.getAccount().getAccountRef(),
                     requestType,
                     request.getId(),
                     null));
         return;
      }

      asyncResponse.resume(Response.status(Response.Status.OK).entity(getResponse(request, account)).build());
   }

   protected void handleReversal(
         String adviceId,
         String requestId,
         BasicReversal reversal,
         AsyncResponse asyncResponse,
         ErrorDetail.RequestType requestType) throws Exception {

      BasicReversal prevReversal = MockBillPayBackend.getRequestReversal(requestId);
      if (prevReversal != null) {
         asyncResponse.resume(
               ErrorDetailFactory.getPreviousAdviceReceivedErrorDetail(
                     prevReversal,
                     requestType,
                     reversal.getId(),
                     reversal.getRequestId()));
         return;
      }

      BasicAdvice advice = MockBillPayBackend.getRequestConfirmation(requestId);
      if (advice != null) {
         asyncResponse.resume(
               ErrorDetailFactory.getPreviousAdviceReceivedErrorDetail(
                     advice,
                     requestType,
                     reversal.getId(),
                     reversal.getRequestId()));
         return;
      }

      if (!validateAndPersist(reversal, asyncResponse)) {
         return;
      }

      RefundRequest origRequest = (RefundRequest) MockBillPayBackend.getRequest(requestId);
      if (origRequest != null) {
         try {
            doReversal(origRequest);
         } catch (ClassCastException e) {
            log.error("Request type and reversal type incompatible");
            log.info("Removing advice message of ID {}", adviceId);
            MockBillPayBackend.removeMessage(adviceId);
            asyncResponse.resume(
                  ErrorDetailFactory.getMismatchingRequestAndAdviceErrorDetail(
                        requestId,
                        requestType,
                        reversal.getId(),
                        reversal.getRequestId()));
            return;
         }
      }

      BasicAdviceResponse adviceResponse = new BasicAdviceResponse();
      adviceResponse.setId(reversal.getId());
      adviceResponse.setRequestId(reversal.getRequestId());
      adviceResponse.setThirdPartyIdentifiers(reversal.getThirdPartyIdentifiers());
      adviceResponse.setTime(reversal.getTime());
      asyncResponse.resume(Response.status(Response.Status.ACCEPTED).entity(adviceResponse).build());
   }

   protected boolean validateAndPersist(BasicAdvice advice, AsyncResponse asyncResponse) {
      ValidationResult validation = BillpayMessageValidator.validate(advice);

      ErrorDetail.RequestType requestType;
      if (advice instanceof BasicReversal) {
         requestType = ErrorDetail.RequestType.REFUND_REVERSAL;
      } else {
         requestType = ErrorDetail.RequestType.REFUND_CONFIRMATION;
      }

      if (!validation.isValid()) {
         log.info("Request format invalid");
         asyncResponse.resume(
               ErrorDetailFactory.getIllFormattedMessageErrorDetail(
                     validation,
                     requestType,
                     advice.getId(),
                     advice.getRequestId()));
         return false;
      }

      try {
         log.debug(Utils.objectToPrettyPrintedJson(advice));
      } catch (JsonProcessingException e) {
         log.error("Could not print advice");
      }

      RefundRequest origRequest = (RefundRequest) MockBillPayBackend.getRequest(advice.getRequestId());
      if (origRequest == null) {
         asyncResponse.resume(
               ErrorDetailFactory.getNoPrecedingRequestFoundErrorDetail(
                     advice.getRequestId(),
                     requestType,
                     advice.getId(),
                     advice.getRequestId()));
         return false;
      }

      boolean wasAdded = MockBillPayBackend.add(advice, false);

      if (!wasAdded) {
         asyncResponse.resume(
               ErrorDetailFactory
                     .getNotUniqueUuidErrorDetail(advice.getId(), requestType, advice.getId(), advice.getRequestId()));
         return false;
      }
      return true;
   }

   protected boolean validateAndPersist(RefundRequest request, AsyncResponse asyncResponse) {
      ValidationResult validation = BillpayMessageValidator.validate(request);

      ErrorDetail.RequestType requestType = ErrorDetail.RequestType.REFUND_REQUEST;

      if (!validation.isValid()) {
         log.info("Request format invalid");
         asyncResponse.resume(
               ErrorDetailFactory.getIllFormattedMessageErrorDetail(validation, requestType, request.getId(), null));
         return false;
      }

      logRequestOrResponse(request, log);

      boolean wasAdded = MockBillPayBackend.add(request);

      if (!wasAdded) {
         asyncResponse.resume(
               ErrorDetailFactory.getNotUniqueUuidErrorDetail(request.getId(), requestType, request.getId(), null));
         return false;
      }
      return true;
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
