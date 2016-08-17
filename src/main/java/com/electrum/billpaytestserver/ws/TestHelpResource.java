package com.electrum.billpaytestserver.ws;

import io.electrum.billpay.model.AccountLookupRequest;
import io.electrum.billpay.model.PaymentRequest;
import io.electrum.billpay.model.PaymentReversal;
import io.electrum.billpay.model.RefundRequest;
import io.electrum.billpay.model.RefundReversal;
import io.electrum.vas.model.BasicAdvice;
import io.electrum.vas.model.TenderAdvice;

import java.io.IOException;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.electrum.billpaytestserver.account.BillPayAccount;
import com.electrum.billpaytestserver.engine.MockBillPayBackend;

/**
 *
 */
@Path("/test/")
@Consumes({ "application/json" })
@Produces({ "application/json" })
public class TestHelpResource {
   private static final Logger log = LoggerFactory.getLogger(TestHelpResource.class);

   @Path("reset")
   @GET
   public void reset() throws IOException {
      log.info("Resetting server");
      try {
         MockBillPayBackend.reset();
      } catch (IOException e) {
         log.error("Could not reset server", e);
         throw e;
      }
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

   @Path("allPaymentRequests")
   @GET
   public PaymentRequest[] getAllPaymentRequests() {
      log.info("GET payment requests");
      return MockBillPayBackend.getPaymentRequests();
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
   public PaymentReversal[] getAllPaymentReversals() {
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
   public RefundReversal[] getAllRefundReversals() {
      log.info("GET refund requests");
      return MockBillPayBackend.getRefundReversals();
   }
}
