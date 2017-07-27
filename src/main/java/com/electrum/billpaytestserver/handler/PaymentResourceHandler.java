package com.electrum.billpaytestserver.handler;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.core.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.electrum.billpaytestserver.Utils;
import com.electrum.billpaytestserver.account.BillPayAccount;
import com.electrum.billpaytestserver.engine.ErrorDetailFactory;
import com.electrum.billpaytestserver.engine.MockBillPayBackend;
import com.electrum.billpaytestserver.validation.BillpayMessageValidator;
import com.electrum.billpaytestserver.validation.ValidationResult;
import com.fasterxml.jackson.core.JsonProcessingException;

import io.electrum.billpay.api.IPaymentsResource;
import io.electrum.billpay.model.*;
import io.electrum.vas.model.*;

/**
 *
 */
public class PaymentResourceHandler extends BaseRequestHandler implements IPaymentsResource {
   private static final Logger log = LoggerFactory.getLogger(PaymentResourceHandler.class);

   @Override
   public void confirmPayment(
         String adviceId,
         String paymentId,
         TenderAdvice tenderAdvice,
         SecurityContext securityContext,
         AsyncResponse asyncResponse,
         Request request,
         HttpServletRequest httpServletRequest,
         HttpHeaders httpHeaders,
         UriInfo uriInfo) {
      log.info("Handling payment confirm");
      try {
         handleConfirm(adviceId, paymentId, tenderAdvice, asyncResponse, ErrorDetail.RequestType.PAYMENT_CONFIRMATION);
      } catch (Exception e) {
         log.error("Error handling message", e);
         asyncResponse.resume(ErrorDetailFactory.getServerErrorErrorDetail(
               e,
               ErrorDetail.RequestType.PAYMENT_CONFIRMATION,
               tenderAdvice.getId(),
               tenderAdvice.getRequestId()));
      }
   }

   @Override
   public void createPayment(
         String uuid,
         PaymentRequest paymentRequest,
         SecurityContext securityContext,
         AsyncResponse asyncResponse,
         Request request,
         HttpServletRequest httpServletRequest,
         HttpHeaders httpHeaders,
         UriInfo uriInfo) {
      log.info("Handling payment request");
      try {
         handleMessage(paymentRequest, asyncResponse);
      } catch (Exception e) {
         log.error("Error handling message", e);
         asyncResponse.resume(
               ErrorDetailFactory.getServerErrorErrorDetail(
                     e,
                     ErrorDetail.RequestType.PAYMENT_REQUEST,
                     paymentRequest.getId(),
                     null));
      }
   }

   @Override
   public void createPayment(
         String id,
         TrafficFinePaymentRequest trafficFinePaymentRequest,
         SecurityContext securityContext,
         AsyncResponse asyncResponse,
         Request request,
         HttpServletRequest httpServletRequest,
         HttpHeaders httpHeaders,
         UriInfo uriInfo) {
      log.info("Handling traffic fine payment request");
      try {
         handleMessage(trafficFinePaymentRequest, asyncResponse);
      } catch (Exception e) {
         log.error("Error handling message", e);
         asyncResponse.resume(ErrorDetailFactory.getServerErrorErrorDetail(
               e,
               ErrorDetail.RequestType.TRAFFIC_FINE_PAYMENT_REQUEST,
               trafficFinePaymentRequest.getId(),
               null));
      }
   }

   @Override
   public void createPayment(
         String id,
         PolicyPaymentRequest policyPaymentRequest,
         SecurityContext securityContext,
         AsyncResponse asyncResponse,
         Request request,
         HttpServletRequest httpServletRequest,
         HttpHeaders httpHeaders,
         UriInfo uriInfo) {
      log.info("Handling policy payment request");
      try {
         handleMessage(policyPaymentRequest, asyncResponse);
      } catch (Exception e) {
         log.error("Error handling message", e);
         asyncResponse.resume(
               ErrorDetailFactory.getServerErrorErrorDetail(
                     e,
                     ErrorDetail.RequestType.POLICY_PAYMENT_REQUEST,
                     policyPaymentRequest.getId(),
                     null));
      }
   }

   @Override
   public void reversePayment(
         String adviceId,
         String paymentId,
         BasicReversal paymentReversal,
         SecurityContext securityContext,
         AsyncResponse asyncResponse,
         Request request,
         HttpServletRequest httpServletRequest,
         HttpHeaders httpHeaders,
         UriInfo uriInfo) {
      log.info("Handling payment reversal");
      try {
         handleReversal(
               adviceId,
               paymentId,
               paymentReversal,
               asyncResponse,
               ErrorDetail.RequestType.PAYMENT_REVERSAL,
               log);
      } catch (Exception e) {
         log.error("Error handling message", e);
         asyncResponse.resume(ErrorDetailFactory.getServerErrorErrorDetail(
               e,
               ErrorDetail.RequestType.PAYMENT_REVERSAL,
               paymentReversal.getId(),
               paymentReversal.getRequestId()));
      }
   }

   protected void doConfirm(Transaction request) {
      String reference = null;
      Amounts amounts = null;

      if (request instanceof PaymentRequest) {
         reference = ((PaymentRequest) request).getAccountRef();
         amounts = ((PaymentRequest) request).getAmounts();
      }
      if (request instanceof TrafficFinePaymentRequest) {
         reference = ((TrafficFinePaymentRequest) request).getNoticeNumber();
         amounts = ((TrafficFinePaymentRequest) request).getAmounts();
      }
      if (request instanceof PolicyPaymentRequest) {
         reference = ((PolicyPaymentRequest) request).getPolicyNumber();
         amounts = ((PolicyPaymentRequest) request).getAmounts();
      }

      BillPayAccount account = MockBillPayBackend.getAccount(reference);

      LedgerAmount ledgerAmount = account.getBalance();

      long amount = ledgerAmount.getAmount();

      amount -= amounts.getRequestAmount().getAmount();

      ledgerAmount.setAmount(amount);
   }

   protected void doReversal(Transaction request) {
   }

   protected PaymentResponse getResponse(PaymentRequest request, BillPayAccount account) {
      log.info("Constructing response");
      PaymentResponse response = new PaymentResponse();

      setBasicResponseFields(request, response);

      response.setAccount(getAccount(account));
      response.setCustomer(account.getCustomer());
      response.setSlipData(getSlipData());
      Amounts reqAmounts = request.getAmounts();
      LedgerAmount requestedAmount = reqAmounts.getRequestAmount();
      Amounts rspAmounts =
            new Amounts().requestAmount(requestedAmount)
                  .approvedAmount(requestedAmount)
                  .balanceAmount(account.getBalance());
      response.setAmounts(rspAmounts);

      logRequestOrResponse(response, log);

      MockBillPayBackend.add(response);

      return response;
   }

   protected TrafficFinePaymentResponse getResponse(TrafficFinePaymentRequest request, BillPayAccount account) {
      log.info("Constructing response");
      TrafficFinePaymentResponse response = new TrafficFinePaymentResponse();

      setBasicResponseFields(request, response);

      response.setTrafficFine(getTrafficFine(account));
      response.setCustomer(account.getCustomer());
      response.setSlipData(getSlipData());
      Amounts reqAmounts = request.getAmounts();
      LedgerAmount requestedAmount = reqAmounts.getRequestAmount();
      Amounts rspAmounts =
            new Amounts().requestAmount(requestedAmount)
                  .approvedAmount(requestedAmount)
                  .balanceAmount(account.getBalance());
      response.setAmounts(rspAmounts);

      logRequestOrResponse(response, log);

      MockBillPayBackend.add(response);

      return response;
   }

   protected PolicyPaymentResponse getResponse(PolicyPaymentRequest request, BillPayAccount account) {
      log.info("Constructing response");
      PolicyPaymentResponse response = new PolicyPaymentResponse();

      setBasicResponseFields(request, response);

      response.setPolicy(getPolicy(account));
      response.setCustomer(account.getCustomer());
      response.setSlipData(getSlipData());
      Amounts reqAmounts = request.getAmounts();
      LedgerAmount requestedAmount = reqAmounts.getRequestAmount();
      Amounts rspAmounts =
            new Amounts().requestAmount(requestedAmount)
                  .approvedAmount(requestedAmount)
                  .balanceAmount(account.getBalance());
      response.setAmounts(rspAmounts);

      logRequestOrResponse(response, log);

      MockBillPayBackend.add(response);

      return response;
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

      if (!validateAndPersist(advice, asyncResponse, requestType)) {
         return;
      }

      Transaction origRequest = MockBillPayBackend.getRequest(requestId);
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

   protected void handleMessage(PaymentRequest request, AsyncResponse asyncResponse) throws Exception {

      if (!validateAndPersist(request, asyncResponse)) {
         return;
      }

      BillPayAccount account;

      account = MockBillPayBackend.getAccount((request).getAccountRef());

      if (account == null) {
         asyncResponse.resume(
               ErrorDetailFactory.getNoAccountFoundErrorDetail(
                     (request).getAccountRef(),
                     ErrorDetail.RequestType.PAYMENT_REQUEST,
                     request.getId(),
                     null));
         return;
      }

      asyncResponse.resume(Response.status(Response.Status.OK).entity(getResponse(request, account)).build());
   }

   protected void handleMessage(TrafficFinePaymentRequest request, AsyncResponse asyncResponse) throws Exception {

      if (!validateAndPersist(request, asyncResponse)) {
         return;
      }

      BillPayAccount account;

      account = MockBillPayBackend.getAccount((request).getNoticeNumber());

      if (account == null) {
         asyncResponse.resume(
               ErrorDetailFactory.getNoAccountFoundErrorDetail(
                     (request).getNoticeNumber(),
                     ErrorDetail.RequestType.TRAFFIC_FINE_PAYMENT_REQUEST,
                     request.getId(),
                     null));
         return;
      }

      asyncResponse.resume(Response.status(Response.Status.OK).entity(getResponse(request, account)).build());
   }

   protected void handleMessage(PolicyPaymentRequest request, AsyncResponse asyncResponse) throws Exception {

      if (!validateAndPersist(request, asyncResponse)) {
         return;
      }

      BillPayAccount account;

      account = MockBillPayBackend.getAccount((request).getPolicyNumber());

      if (account == null) {
         asyncResponse.resume(
               ErrorDetailFactory.getNoAccountFoundErrorDetail(
                     (request).getPolicyNumber(),
                     ErrorDetail.RequestType.POLICY_PAYMENT_REQUEST,
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
         ErrorDetail.RequestType requestType,
         Logger log) throws Exception {

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

      if (!validateAndPersist(reversal, asyncResponse, requestType)) {
         return;
      }

      Transaction origRequest = MockBillPayBackend.getRequest(requestId);
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

   protected boolean validateAndPersist(PaymentRequest request, AsyncResponse asyncResponse) {
      ValidationResult validation = BillpayMessageValidator.validate(request);

      ErrorDetail.RequestType requestType = ErrorDetail.RequestType.PAYMENT_REQUEST;

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

   protected boolean validateAndPersist(TrafficFinePaymentRequest request, AsyncResponse asyncResponse) {
      ValidationResult validation = BillpayMessageValidator.validate(request);

      ErrorDetail.RequestType requestType = ErrorDetail.RequestType.TRAFFIC_FINE_PAYMENT_REQUEST;

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

   protected boolean validateAndPersist(PolicyPaymentRequest request, AsyncResponse asyncResponse) {
      ValidationResult validation = BillpayMessageValidator.validate(request);

      ErrorDetail.RequestType requestType = ErrorDetail.RequestType.POLICY_PAYMENT_REQUEST;

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

   protected boolean validateAndPersist(
         BasicAdvice advice,
         AsyncResponse asyncResponse,
         ErrorDetail.RequestType requestType) {
      ValidationResult validation = BillpayMessageValidator.validate(advice);

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

      Transaction origRequest = MockBillPayBackend.getRequest(advice.getRequestId());
      if (origRequest == null) {
         asyncResponse.resume(
               ErrorDetailFactory.getNoPrecedingRequestFoundErrorDetail(
                     advice.getRequestId(),
                     requestType,
                     advice.getId(),
                     advice.getRequestId()));
         return false;
      }

      boolean wasAdded = MockBillPayBackend.add(advice, true);

      if (!wasAdded) {
         asyncResponse.resume(
               ErrorDetailFactory
                     .getNotUniqueUuidErrorDetail(advice.getId(), requestType, advice.getId(), advice.getRequestId()));
         return false;
      }
      return true;
   }
}
