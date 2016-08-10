package com.electrum.billpaytestserver.handler;

import io.electrum.billpay.model.Account;
import io.electrum.billpay.model.AccountLookupRequest;
import io.electrum.billpay.model.PaymentRequest;
import io.electrum.billpay.model.SlipData;
import io.electrum.vas.model.BasicAdvice;
import io.electrum.vas.model.BasicRequest;
import io.electrum.vas.model.BasicResponse;
import io.electrum.vas.model.BasicReversal;
import io.electrum.vas.model.Institution;

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
public abstract class BaseRequestHandler<T extends BasicRequest, U extends BasicResponse> {
   private static final Logger log = LoggerFactory.getLogger(BaseRequestHandler.class);

   protected void handleMessage(
         UUID id,
         T request,
         SecurityContext securityContext,
         AsyncResponse asyncResponse,
         Request jaxRequest,
         HttpServletRequest httpServletRequest,
         HttpHeaders httpHeaders,
         UriInfo uriInfo) {

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
         UriInfo uriInfo) {

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

      if (origRequest == null) {
         asyncResponse.resume(ErrorDetailFactory.getNoPrecedingRequestFoundErrorDetail(requestId));
         return;
      }

      doConfirm(origRequest);

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
         UriInfo uriInfo) {

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

      if (origRequest == null) {
         asyncResponse.resume(ErrorDetailFactory.getNoPrecedingRequestFoundErrorDetail(requestId));
         return;
      }

      doReversal(origRequest);

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

      boolean wasAdded = MockBillPayBackend.add(advice);

      if (!wasAdded) {
         asyncResponse.resume(ErrorDetailFactory.getNotUniqueUuidErrorDetail());
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
         asyncResponse.resume(ErrorDetailFactory.getNotUniqueUuidErrorDetail());
         return false;
      }
      return true;
   }

   protected abstract U getResponse(T request, BillPayAccount account);

   protected abstract void doReversal(T origRequest);

   protected abstract void doConfirm(T origRequest);

   protected Institution getProcessor() {
      Institution institution = new Institution();
      institution.setId("ProcessorId");
      institution.setName("ProcessorName");

      return institution;
   }

   protected Institution getReceiver() {
      Institution institution = new Institution();
      institution.setId("ReceiverId");
      institution.setName("ReceiverName");

      return institution;
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
