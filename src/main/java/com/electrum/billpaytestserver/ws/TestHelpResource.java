package com.electrum.billpaytestserver.ws;

import java.io.IOException;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.electrum.billpaytestserver.account.BillPayAccount;
import com.electrum.billpaytestserver.engine.ErrorDetailFactory;
import com.electrum.billpaytestserver.engine.MockBillPayBackend;

import io.electrum.billpay.model.*;
import io.electrum.vas.model.BasicAdvice;
import io.electrum.vas.model.BasicReversal;
import io.electrum.vas.model.LedgerAmount;
import io.electrum.vas.model.TenderAdvice;

/**
 *
 */
@Path("/billpay/v4/test/")
@Consumes({ "application/json" })
@Produces({ "application/json" })
public class TestHelpResource {
   private static final Logger log = LoggerFactory.getLogger(TestHelpResource.class);

   @Path("reset")
   @GET
   public Response reset() throws IOException {
      log.info("Resetting server");
      try {
         MockBillPayBackend.reset();
      } catch (IOException e) {
         log.error("Could not reset server", e);
         return ErrorDetailFactory
               .getServerErrorErrorDetail(e, ErrorDetail.RequestType.ACCOUNT_LOOKUP_REQUEST, "none", null);
      }
      return Response.ok().build();
   }

   @Path("allAccounts")
   @GET
   public BillPayAccount[] getAllQuestions() {
      log.info("GET accounts");
      return MockBillPayBackend.getAccounts();
   }

   @Path("allAccountLookups")
   @GET
   public AccountLookupRequest[] getAllLookupRequests() {
      log.info("GET account lookups");
      return MockBillPayBackend.getAccountLookupRequests();
   }

   @Path("allTrafficFineLookups")
   @GET
   public TrafficFineLookupRequest[] getAllTrafficFineLookupRequests() {
      log.info("GET traffic fine lookups");
      return MockBillPayBackend.getTrafficFineLookups();
   }

   @Path("allPolicyLookups")
   @GET
   public PolicyLookupRequest[] getAllPolicyLookupRequests() {
      log.info("GET policy lookups");
      return MockBillPayBackend.getPolicyLookups();
   }

   @Path("allPaymentRequests")
   @GET
   public PaymentRequest[] getAllPaymentRequests() {
      log.info("GET payment requests");
      return MockBillPayBackend.getPaymentRequests();
   }

   @Path("allTrafficFinePaymentRequests")
   @GET
   public TrafficFinePaymentRequest[] getAllTrafficFinePaymentRequests() {
      log.info("GET traffic fine payment requests");
      return MockBillPayBackend.getTrafficFinePaymentRequests();
   }

   @Path("allPolicyPaymentRequests")
   @GET
   public PolicyPaymentRequest[] getAllPolicyPaymentRequests() {
      log.info("GET policy payment requests");
      return MockBillPayBackend.getPolicyPaymentRequests();
   }

   @Path("allRefundRequests")
   @GET
   public RefundRequest[] getAllRefundRequests() {
      log.info("GET refund requests");
      return MockBillPayBackend.getRefundRequests();
   }

   @Path("allPaymentConfirmations")
   @GET
   public TenderAdvice[] getAllPaymentConfirmations() {
      log.info("GET refund requests");
      return MockBillPayBackend.getPaymentConfirmations();
   }

   @Path("allPaymentReversals")
   @GET
   public BasicReversal[] getAllPaymentReversals() {
      log.info("GET refund requests");
      return MockBillPayBackend.getPaymentReversals();
   }

   @Path("allRefundConfirmations")
   @GET
   public BasicAdvice[] getAllRefundConfirmations() {
      log.info("GET refund requests");
      return MockBillPayBackend.getRefundConfirmations();
   }

   @Path("allRefundReversals")
   @GET
   public BasicReversal[] getAllRefundReversals() {
      log.info("GET refund requests");
      return MockBillPayBackend.getRefundReversals();
   }

   @Path("addAccount")
   @POST
   public Response getAllQuestions(BillPayAccount account) {
      log.info("Adding account");

      if (account == null) {
         return ErrorDetailFactory.getAccountAddErrorErrorDetail("Account can not be null", account);
      }

      if (account.getAccountRef() == null || account.getAccountRef().isEmpty()) {
         return ErrorDetailFactory.getAccountAddErrorErrorDetail("AccountRef must be populated", account);
      }

      if (MockBillPayBackend.getAccount(account.getAccountRef()) != null) {
         return ErrorDetailFactory.getAccountAddErrorErrorDetail(
               "Account with this AccountRef already exists",
               MockBillPayBackend.getAccount(account.getAccountRef()));
      }

      if (account.getBalance() == null) {
         LedgerAmount amount = new LedgerAmount();
         amount.setAmount(10000l);
         amount.setCurrency("$AM");
         account.setBalance(amount);
      }

      if (account.getCustomer() == null) {
         account.setCustomer("Terry", "Pratchett", "34 Ankh Morpork Drive", "1234567890987", "0213456578");
      }

      MockBillPayBackend.add(account);

      return Response.accepted().entity(account).build();
   }
}
