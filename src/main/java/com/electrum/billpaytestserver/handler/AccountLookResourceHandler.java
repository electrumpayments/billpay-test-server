package com.electrum.billpaytestserver.handler;

import io.electrum.billpay.api.IAccountLookupsResource;
import io.electrum.billpay.model.Account;
import io.electrum.billpay.model.AccountLookupRequest;
import io.electrum.billpay.model.AccountLookupResponse;
import io.electrum.billpay.model.SlipData;
import io.electrum.vas.model.Institution;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.electrum.billpaytestserver.account.BillPayAccount;
import com.electrum.billpaytestserver.engine.ErrorDetailFactory;
import com.electrum.billpaytestserver.engine.MockBillPayBackend;
import com.electrum.billpaytestserver.engine.ValidationResult;
import com.electrum.billpaytestserver.engine.Validator;

/**
 *
 */
public class AccountLookResourceHandler implements IAccountLookupsResource {
   private static final Logger log = LoggerFactory.getLogger(AccountLookResourceHandler.class);

   @Override
   public Response requestAccountInfo(
         String s,
         AccountLookupRequest accountLookupRequest,
         SecurityContext securityContext,
         AsyncResponse asyncResponse,
         HttpHeaders httpHeaders,
         UriInfo uriInfo) {

      log.info("Handling account lookup request");

      ValidationResult validation = Validator.validate(accountLookupRequest);

      if (!validation.isValid()) {
         return ErrorDetailFactory.getIllFormattedMessageErrorDetail(validation);
      }

      boolean wasAdded = MockBillPayBackend.add(accountLookupRequest);

      if (!wasAdded) {
         return ErrorDetailFactory.getNotUniqueUuidErrorDetail();
      }

      BillPayAccount account = MockBillPayBackend.getAccount(accountLookupRequest.getAccountRef());

      if (account == null) {
         return ErrorDetailFactory.getNoAccountFoundErrorDetail(accountLookupRequest.getAccountRef());
      }

      return getResponse(accountLookupRequest, account);
   }

   private Response getResponse(AccountLookupRequest request, BillPayAccount account) {
      AccountLookupResponse response = new AccountLookupResponse();

      response.setId(request.getId());
      response.setTime(new DateTime());
      response.setSender(request.getSender());
      // response.setLinkData();
      response.setProcessor(getProcessor());
      response.setReceiver(getReceiver());
      response.setAccount(getAccount(account));
      response.setCustomer(account.getCustomer());
      response.setSlipData(getSlipData());

      return Response.status(Response.Status.OK).entity(response).build();
   }

   private Institution getProcessor() {
      Institution institution = new Institution();
      institution.setId("ProcessorId");
      institution.setName("ProcessorName");

      return institution;
   }

   private Institution getReceiver() {
      Institution institution = new Institution();
      institution.setId("ReceiverId");
      institution.setName("ReceiverName");

      return institution;
   }

   private Account getAccount(BillPayAccount bpAccount) {
      Account account = new Account();

      account.setAccountRef(bpAccount.getAccountRef());
      account.setBalance(bpAccount.getBalance());
      // account.setDueDate();
      return account;
   }

   private SlipData getSlipData() {
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
