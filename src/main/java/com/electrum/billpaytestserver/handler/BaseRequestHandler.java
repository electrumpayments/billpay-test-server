package com.electrum.billpaytestserver.handler;

import io.electrum.billpay.model.Account;
import io.electrum.billpay.model.AccountLookupRequest;
import io.electrum.billpay.model.PaymentRequest;
import io.electrum.billpay.model.PaymentResponse;
import io.electrum.billpay.model.RefundRequest;
import io.electrum.billpay.model.SlipData;
import io.electrum.vas.model.BasicAdvice;
import io.electrum.vas.model.BasicReversal;
import io.electrum.vas.model.Institution;
import io.electrum.vas.model.ThirdPartyIdentifier;
import io.electrum.vas.model.Transaction;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

/**
 * T is request type U is response type
 */
public abstract class BaseRequestHandler<T extends Transaction, U extends Transaction> {
   private static final Logger log = LoggerFactory.getLogger(BaseRequestHandler.class);

   protected void handleMessage(
         UUID id,
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
                  ErrorDetailFactory.getNoAccountFoundErrorDetail(((AccountLookupRequest) request).getAccountRef()));
            return;
         }
      } else if (request instanceof PaymentRequest) {
         account = MockBillPayBackend.getAccount(((PaymentRequest) request).getAccountRef());

         if (account == null) {
            asyncResponse
                  .resume(ErrorDetailFactory.getNoAccountFoundErrorDetail(((PaymentRequest) request).getAccountRef()));
            return;
         }
      } else if (request instanceof RefundRequest) {
         PaymentResponse paymentResponse =
               MockBillPayBackend.getPaymentResponse(((RefundRequest) request).getIssuerReference());

         if (paymentResponse == null) {
            asyncResponse.resume(
                  ErrorDetailFactory
                        .getNoPaymentRequestFoundErrorDetail(((RefundRequest) request).getIssuerReference()));
            return;
         }

         account = MockBillPayBackend.getAccount(paymentResponse.getAccount().getAccountRef());

         if (account == null) {
            asyncResponse.resume(
                  ErrorDetailFactory.getNoAccountFoundErrorDetail(paymentResponse.getAccount().getAccountRef()));
            return;
         }
      }

      asyncResponse.resume(Response.status(Response.Status.OK).entity(getResponse(request, account)).build());
   }

   protected void handleConfirm(
         UUID adviceId,
         UUID requestId,
         BasicAdvice advice,
         SecurityContext securityContext,
         AsyncResponse asyncResponse,
         Request request,
         HttpServletRequest httpServletRequest,
         HttpHeaders httpHeaders,
         UriInfo uriInfo) throws Exception {

      BasicReversal reversal = MockBillPayBackend.getRequestReversal(requestId);
      if (reversal != null) {
         asyncResponse.resume(ErrorDetailFactory.getPreviousAdviceReceivedErrorDetail(reversal));
         return;
      }

      BasicAdvice prevAdvice = MockBillPayBackend.getRequestConfirmation(requestId);
      if (prevAdvice != null) {
         asyncResponse.resume(ErrorDetailFactory.getPreviousAdviceReceivedErrorDetail(prevAdvice));
         return;
      }

      if (!validateAndPersist(advice, asyncResponse)) {
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
            asyncResponse.resume(ErrorDetailFactory.getMismatchingRequestAndAdviceErrorDetail(requestId));
            return;
         }
      }

      asyncResponse.resume(Response.status(Response.Status.ACCEPTED).build());
   }

   protected void handleReversal(
         UUID adviceId,
         UUID requestId,
         BasicReversal reversal,
         SecurityContext securityContext,
         AsyncResponse asyncResponse,
         Request request,
         HttpServletRequest httpServletRequest,
         HttpHeaders httpHeaders,
         UriInfo uriInfo) throws Exception {

      BasicReversal prevReversal = MockBillPayBackend.getRequestReversal(requestId);
      if (prevReversal != null) {
         asyncResponse.resume(ErrorDetailFactory.getPreviousAdviceReceivedErrorDetail(prevReversal));
         return;
      }

      BasicAdvice advice = MockBillPayBackend.getRequestConfirmation(requestId);
      if (advice != null) {
         asyncResponse.resume(ErrorDetailFactory.getPreviousAdviceReceivedErrorDetail(advice));
         return;
      }

      if (!validateAndPersist(reversal, asyncResponse)) {
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
            asyncResponse.resume(ErrorDetailFactory.getMismatchingRequestAndAdviceErrorDetail(requestId));
            return;
         }
      }

      asyncResponse.resume(Response.status(Response.Status.ACCEPTED).build());
   }

   protected boolean validateAndPersist(BasicAdvice advice, AsyncResponse asyncResponse) {
      ValidationResult validation = BillpayMessageValidator.validate(advice);

      if (!validation.isValid()) {
         log.info("Request format invalid");
         asyncResponse.resume(ErrorDetailFactory.getIllFormattedMessageErrorDetail(validation));
         return false;
      }

      try {
         log.debug(Utils.objectToPrettyPrintedJson(advice));
      } catch (JsonProcessingException e) {
         log.error("Could not print advice");
      }

      T origRequest = (T) MockBillPayBackend.getRequest(advice.getRequestId());
      if (origRequest == null) {
         asyncResponse.resume(ErrorDetailFactory.getNoPrecedingRequestFoundErrorDetail(advice.getRequestId()));
         return false;
      }

      boolean wasAdded = MockBillPayBackend.add(advice);

      if (!wasAdded) {
         asyncResponse.resume(ErrorDetailFactory.getNotUniqueUuidErrorDetail(advice.getId()));
         return false;
      }
      return true;
   }

   protected boolean validateAndPersist(T request, AsyncResponse asyncResponse) {
      ValidationResult validation = BillpayMessageValidator.validate(request);

      if (!validation.isValid()) {
         log.info("Request format invalid");
         asyncResponse.resume(ErrorDetailFactory.getIllFormattedMessageErrorDetail(validation));
         return false;
      }

      try {
         log.debug(Utils.objectToPrettyPrintedJson(request));
      } catch (JsonProcessingException e) {
         log.error("Could not print request");
      }

      boolean wasAdded = MockBillPayBackend.add(request);

      if (!wasAdded) {
         asyncResponse.resume(ErrorDetailFactory.getNotUniqueUuidErrorDetail(request.getId()));
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
      account.setBalance(bpAccount.getBalance());
      // account.setDueDate();
      return account;
   }

   protected SlipData getSlipData() {
      SlipData slipData = new SlipData();
      slipData.setIssuerReference(Utils.generateIssuerReferenceNumber());
      slipData.setPhoneNumber("PhoneNumber");
      List<String> lines = new ArrayList();
      lines.add("line 1");
      lines.add("line 2");
      lines.add("line 3");
      slipData.setMessageLines(lines);

      return slipData;
   }
}
