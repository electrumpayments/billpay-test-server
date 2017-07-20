package com.electrum.billpaytestserver.handler;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.electrum.billpaytestserver.Utils;
import com.electrum.billpaytestserver.account.BillPayAccount;
import com.electrum.billpaytestserver.engine.ErrorDetailFactory;
import com.electrum.billpaytestserver.engine.MockBillPayBackend;
import com.electrum.billpaytestserver.validation.BillpayMessageValidator;
import com.electrum.billpaytestserver.validation.ValidationResult;
import com.fasterxml.jackson.core.JsonProcessingException;

import io.electrum.billpay.model.Account;
import io.electrum.billpay.model.AccountLookupRequest;
import io.electrum.billpay.model.BillSlipData;
import io.electrum.billpay.model.ErrorDetail;
import io.electrum.billpay.model.PaymentRequest;
import io.electrum.billpay.model.PaymentResponse;
import io.electrum.billpay.model.RefundRequest;
import io.electrum.vas.model.BasicAdvice;
import io.electrum.vas.model.BasicAdviceResponse;
import io.electrum.vas.model.BasicReversal;
import io.electrum.vas.model.Institution;
import io.electrum.vas.model.SlipLine;
import io.electrum.vas.model.TenderAdvice;
import io.electrum.vas.model.ThirdPartyIdentifier;
import io.electrum.vas.model.Transaction;

/**
 * T is request type U is response type
 */
public abstract class BaseRequestHandler<T extends Transaction, U extends Transaction> {
   private static final Logger log = LoggerFactory.getLogger(BaseRequestHandler.class);

   protected void handleMessage(
         String id,
         T request,
         SecurityContext securityContext,
         AsyncResponse asyncResponse,
         Request jaxRequest,
         HttpServletRequest httpServletRequest,
         HttpHeaders httpHeaders,
         UriInfo uriInfo) throws Exception {

      if (!validateAndPersist(request, asyncResponse)) {
         return;
      }

      BillPayAccount account = null;

      if (request instanceof AccountLookupRequest) {
         account = MockBillPayBackend.getAccount(((AccountLookupRequest) request).getAccountRef());

         if (account == null) {
            asyncResponse.resume(
                  ErrorDetailFactory.getNoAccountFoundErrorDetail(
                        ((AccountLookupRequest) request).getAccountRef(),
                        ErrorDetail.RequestType.ACCOUNT_LOOKUP_REQUEST,
                        request.getId(),
                        null));
            return;
         }
      } else if (request instanceof PaymentRequest) {
         account = MockBillPayBackend.getAccount(((PaymentRequest) request).getAccountRef());

         if (account == null) {
            asyncResponse.resume(
                  ErrorDetailFactory.getNoAccountFoundErrorDetail(
                        ((PaymentRequest) request).getAccountRef(),
                        ErrorDetail.RequestType.PAYMENT_REQUEST,
                        request.getId(),
                        null));
            return;
         }
      } else if (request instanceof RefundRequest) {
         PaymentResponse paymentResponse =
               MockBillPayBackend.getPaymentResponse(((RefundRequest) request).getIssuerReference());

         if (paymentResponse == null) {
            asyncResponse.resume(
                  ErrorDetailFactory.getNoPaymentRequestFoundErrorDetail(
                        ((RefundRequest) request).getIssuerReference(),
                        ErrorDetail.RequestType.REFUND_REQUEST,
                        request.getId(),
                        null));
            return;
         }

         account = MockBillPayBackend.getAccount(paymentResponse.getAccount().getAccountRef());

         if (account == null) {
            asyncResponse.resume(
                  ErrorDetailFactory.getNoAccountFoundErrorDetail(
                        paymentResponse.getAccount().getAccountRef(),
                        ErrorDetail.RequestType.PAYMENT_REQUEST,
                        request.getId(),
                        null));
            return;
         }
      }

      asyncResponse.resume(Response.status(Response.Status.OK).entity(getResponse(request, account)).build());
   }

   protected void handleConfirm(
         String adviceId,
         String requestId,
         BasicAdvice advice,
         SecurityContext securityContext,
         AsyncResponse asyncResponse,
         Request request,
         HttpServletRequest httpServletRequest,
         HttpHeaders httpHeaders,
         UriInfo uriInfo,
         boolean isPaymentMessage) throws Exception {

      BasicReversal reversal = MockBillPayBackend.getRequestReversal(requestId);
      if (reversal != null) {
         asyncResponse
               .resume(
                     ErrorDetailFactory
                           .getPreviousAdviceReceivedErrorDetail(
                                 reversal,
                                 isPaymentMessage ? ErrorDetail.RequestType.PAYMENT_CONFIRMATION
                                       : ErrorDetail.RequestType.REFUND_CONFIRMATION,
                                 advice.getId(),
                                 advice.getRequestId()));
         return;
      }

      BasicAdvice prevAdvice = MockBillPayBackend.getRequestConfirmation(requestId);
      if (prevAdvice != null) {
         asyncResponse
               .resume(
                     ErrorDetailFactory
                           .getPreviousAdviceReceivedErrorDetail(
                                 prevAdvice,
                                 isPaymentMessage ? ErrorDetail.RequestType.PAYMENT_CONFIRMATION
                                       : ErrorDetail.RequestType.REFUND_CONFIRMATION,
                                 advice.getId(),
                                 advice.getRequestId()));
         return;
      }

      if (!validateAndPersist(advice, asyncResponse, isPaymentMessage)) {
         return;
      }

      T origRequest = (T) MockBillPayBackend.getRequest(requestId);
      if (origRequest != null) {
         try {
            doConfirm(origRequest);
         } catch (ClassCastException e) {
            log.error("Request type and advice type incompatible");
            log.info("Removing advice message of ID {}", adviceId);
            MockBillPayBackend.removeMessage(adviceId);
            asyncResponse
                  .resume(
                        ErrorDetailFactory.getMismatchingRequestAndAdviceErrorDetail(
                              requestId,
                              isPaymentMessage ? ErrorDetail.RequestType.PAYMENT_CONFIRMATION
                                    : ErrorDetail.RequestType.REFUND_CONFIRMATION,
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

   protected void handleReversal(
         String adviceId,
         String requestId,
         BasicReversal reversal,
         SecurityContext securityContext,
         AsyncResponse asyncResponse,
         Request request,
         HttpServletRequest httpServletRequest,
         HttpHeaders httpHeaders,
         UriInfo uriInfo,
         boolean isPaymentMessage) throws Exception {

      BasicReversal prevReversal = MockBillPayBackend.getRequestReversal(requestId);
      if (prevReversal != null) {
         asyncResponse
               .resume(
                     ErrorDetailFactory
                           .getPreviousAdviceReceivedErrorDetail(
                                 prevReversal,
                                 isPaymentMessage ? ErrorDetail.RequestType.PAYMENT_REVERSAL
                                       : ErrorDetail.RequestType.REFUND_REVERSAL,
                                 reversal.getId(),
                                 reversal.getRequestId()));
         return;
      }

      BasicAdvice advice = MockBillPayBackend.getRequestConfirmation(requestId);
      if (advice != null) {
         asyncResponse
               .resume(
                     ErrorDetailFactory
                           .getPreviousAdviceReceivedErrorDetail(
                                 advice,
                                 isPaymentMessage ? ErrorDetail.RequestType.PAYMENT_REVERSAL
                                       : ErrorDetail.RequestType.REFUND_REVERSAL,
                                 reversal.getId(),
                                 reversal.getRequestId()));
         return;
      }

      if (!validateAndPersist(reversal, asyncResponse, isPaymentMessage)) {
         return;
      }

      T origRequest = (T) MockBillPayBackend.getRequest(requestId);
      if (origRequest != null) {
         try {
            doReversal(origRequest);
         } catch (ClassCastException e) {
            log.error("Request type and reversal type incompatible");
            log.info("Removing advice message of ID {}", adviceId);
            MockBillPayBackend.removeMessage(adviceId);
            asyncResponse
                  .resume(
                        ErrorDetailFactory.getMismatchingRequestAndAdviceErrorDetail(
                              requestId,
                              isPaymentMessage ? ErrorDetail.RequestType.PAYMENT_REVERSAL
                                    : ErrorDetail.RequestType.REFUND_REVERSAL,
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

   protected boolean validateAndPersist(BasicAdvice advice, AsyncResponse asyncResponse, boolean isPaymentMessage) {
      ValidationResult validation = BillpayMessageValidator.validate(advice);

      ErrorDetail.RequestType requestType = ErrorDetail.RequestType.PAYMENT_REVERSAL;
      if (advice instanceof BasicReversal) {
         requestType = isPaymentMessage ? requestType : ErrorDetail.RequestType.REFUND_REVERSAL;
      } else if (advice instanceof TenderAdvice) {
         requestType =
               isPaymentMessage ? ErrorDetail.RequestType.PAYMENT_CONFIRMATION : ErrorDetail.RequestType.REFUND_REVERSAL;
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

      T origRequest = (T) MockBillPayBackend.getRequest(advice.getRequestId());
      if (origRequest == null) {
         asyncResponse.resume(
               ErrorDetailFactory.getNoPrecedingRequestFoundErrorDetail(
                     advice.getRequestId(),
                     requestType,
                     advice.getId(),
                     advice.getRequestId()));
         return false;
      }

      boolean wasAdded = MockBillPayBackend.add(advice, isPaymentMessage);

      if (!wasAdded) {
         asyncResponse.resume(
               ErrorDetailFactory
                     .getNotUniqueUuidErrorDetail(advice.getId(), requestType, advice.getId(), advice.getRequestId()));
         return false;
      }
      return true;
   }

   protected boolean validateAndPersist(T request, AsyncResponse asyncResponse) {
      ValidationResult validation = BillpayMessageValidator.validate(request);

      ErrorDetail.RequestType requestType = ErrorDetail.RequestType.ACCOUNT_LOOKUP_REQUEST;

      if (request instanceof PaymentRequest) {
         requestType = ErrorDetail.RequestType.PAYMENT_REQUEST;
      } else if (request instanceof RefundRequest) {
         requestType = ErrorDetail.RequestType.REFUND_REQUEST;
      }

      if (!validation.isValid()) {
         log.info("Request format invalid");
         asyncResponse.resume(
               ErrorDetailFactory.getIllFormattedMessageErrorDetail(validation, requestType, request.getId(), null));
         return false;
      }

      try {
         log.debug(Utils.objectToPrettyPrintedJson(request));
      } catch (JsonProcessingException e) {
         log.error("Could not print request");
      }

      boolean wasAdded = MockBillPayBackend.add(request);

      if (!wasAdded) {
         asyncResponse.resume(
               ErrorDetailFactory.getNotUniqueUuidErrorDetail(request.getId(), requestType, request.getId(), null));
         return false;
      }
      return true;
   }

   protected abstract U getResponse(T request, BillPayAccount account);

   protected abstract void doReversal(T origRequest);

   protected abstract void doConfirm(T origRequest);

   protected Institution getClient() {
      Institution institution = new Institution();
      institution.setId("ClientId");
      institution.setName("Client Name");

      return institution;
   }

   protected Institution getSettlementEntity() {
      Institution institution = new Institution();
      institution.setId("SettlementEntityId");
      institution.setName("Settlement Entity");

      return institution;
   }

   protected Institution getReceiver() {
      Institution institution = new Institution();
      institution.setId("ReceiverId");
      institution.setName("Receiver Name");

      return institution;
   }

   protected List<ThirdPartyIdentifier> getThirdPartyIdentifiers(
         List<ThirdPartyIdentifier> thirdPartyIdentifiersFromRequest) {
      List<ThirdPartyIdentifier> thirdPartyIdentifiers = new ArrayList<>();

      for (ThirdPartyIdentifier thirdPartyIdentifier : thirdPartyIdentifiersFromRequest) {
         thirdPartyIdentifiers.add(thirdPartyIdentifier);
      }

      ThirdPartyIdentifier settlement = new ThirdPartyIdentifier();
      settlement.setInstitutionId("234652");
      settlement.setTransactionIdentifier("settlementEntityRef");

      ThirdPartyIdentifier receiver = new ThirdPartyIdentifier();
      receiver.setInstitutionId("803485");
      receiver.setTransactionIdentifier("receiverRef");

      thirdPartyIdentifiers.add(settlement);
      thirdPartyIdentifiers.add(receiver);

      return thirdPartyIdentifiers;
   }

   protected Account getAccount(BillPayAccount bpAccount) {
      Account account = new Account();

      account.setAccountRef(bpAccount.getAccountRef());
      // account.setDueDate();
      return account;
   }

   protected BillSlipData getSlipData() {
      BillSlipData slipData = new BillSlipData();
      slipData.setIssuerReference(Utils.generateIssuerReferenceNumber());
      slipData.setPhoneNumber("PhoneNumber");
      List<SlipLine> lines = new ArrayList<SlipLine>();
      lines.add(new SlipLine().fontHeightScaleFactor(2).text("Double height"));
      lines.add(new SlipLine().fontWidthScaleFactor(2).text("Double width"));
      lines.add(new SlipLine().line(true));
      lines.add(new SlipLine().text("Some text goes here"));
      lines.add(new SlipLine().cut(true));
      slipData.setMessageLines(lines);

      return slipData;
   }
}
