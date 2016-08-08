package com.electrum.billpaytestserver.handler;

import io.electrum.billpay.model.Account;
import io.electrum.billpay.model.AccountLookupRequest;
import io.electrum.billpay.model.PaymentRequest;
import io.electrum.billpay.model.SlipData;
import io.electrum.vas.model.BasicRequest;
import io.electrum.vas.model.BasicResponse;
import io.electrum.vas.model.Institution;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.electrum.billpaytestserver.Utils;
import com.electrum.billpaytestserver.account.BillPayAccount;
import com.electrum.billpaytestserver.engine.ErrorDetailFactory;
import com.electrum.billpaytestserver.engine.MockBillPayBackend;
import com.electrum.billpaytestserver.validation.ValidationResult;
import com.electrum.billpaytestserver.validation.BillpayMessageValidator;
import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * T is request type U is response type
 */
public abstract class BaseRequestHandler<T extends BasicRequest, U extends BasicResponse> {
   private static final Logger log = LoggerFactory.getLogger(BaseRequestHandler.class);

   protected Response handleMessage(
         String id,
         T request,
         SecurityContext securityContext,
         AsyncResponse asyncResponse,
         HttpHeaders httpHeaders,
         UriInfo uriInfo) {

      ValidationResult validation = BillpayMessageValidator.validate(request);

      if (!validation.isValid()) {
         log.info("Request format invalid");
         return ErrorDetailFactory.getIllFormattedMessageErrorDetail(validation);
      }

      try {
         log.debug(Utils.objectToPrettyPrintedJson(request));
      } catch (JsonProcessingException e) {
         log.error("Could not print request");
      }

      boolean wasAdded = MockBillPayBackend.add(request);

      if (!wasAdded) {
         return ErrorDetailFactory.getNotUniqueUuidErrorDetail();
      }

      BillPayAccount account = null;
      
      if (request instanceof AccountLookupRequest) {
          account = MockBillPayBackend.getAccount(((AccountLookupRequest) request).getAccountRef());

         if (account == null) {
            return ErrorDetailFactory.getNoAccountFoundErrorDetail(((AccountLookupRequest) request).getAccountRef());
         }
      } else if (request instanceof PaymentRequest) {
          account = MockBillPayBackend.getAccount(((PaymentRequest) request).getAccountRef());

         if (account == null) {
            return ErrorDetailFactory.getNoAccountFoundErrorDetail(((PaymentRequest) request).getAccountRef());
         }
      }

      return Response.status(Response.Status.OK).entity(getResponse(request, account)).build();
   }
 
   protected abstract U getResponse(T request, BillPayAccount account);

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
      slipData.setIssuerReference("IssuerReference");
      slipData.setPhoneNumber("PhoneNumber");
      List<String> lines = new ArrayList();
      lines.add("line 1");
      lines.add("line 2");
      lines.add("line 3");
      slipData.setMessageLines(lines);

      return slipData;
   }
}
