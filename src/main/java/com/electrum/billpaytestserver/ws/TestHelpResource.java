package com.electrum.billpaytestserver.ws;

import io.electrum.billpay.model.AccountLookupRequest;
import io.electrum.billpay.model.PaymentRequest;
import io.electrum.billpay.model.RefundRequest;

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
}
