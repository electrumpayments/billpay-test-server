package com.electrum.billpaytestserver.engine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.electrum.billpaytestserver.account.AccountLoader;
import com.electrum.billpaytestserver.account.BillPayAccount;

import io.electrum.billpay.model.AccountLookupRequest;
import io.electrum.billpay.model.PaymentRequest;
import io.electrum.billpay.model.PaymentResponse;
import io.electrum.billpay.model.RefundRequest;
import io.electrum.vas.model.BasicAdvice;
import io.electrum.vas.model.BasicReversal;
import io.electrum.vas.model.TenderAdvice;
import io.electrum.vas.model.Transaction;

/**
 *
 */
public class MockBillPayBackend {
   private static final Logger log = LoggerFactory.getLogger(MockBillPayBackend.class);

   private static Date lastResetTime;

   private static HashMap<String, BillPayAccount> accounts = new HashMap<String, BillPayAccount>();

   private static HashMap<String, AccountLookupRequest> accountLookups = new HashMap<String, AccountLookupRequest>();
   private static HashMap<String, PaymentRequest> paymentRequests = new HashMap<String, PaymentRequest>();
   private static HashMap<String, RefundRequest> refundRequests = new HashMap<String, RefundRequest>();

   private static HashMap<String, TenderAdvice> paymentConfirmations = new HashMap<String, TenderAdvice>();
   private static HashMap<String, BasicAdvice> refundConfirmation = new HashMap<String, BasicAdvice>();

   private static HashMap<String, BasicReversal> paymentReversals = new HashMap<String, BasicReversal>();
   private static HashMap<String, BasicReversal> refundReversals = new HashMap<String, BasicReversal>();

   private static List<Map<String, ? extends Transaction>> allRequests =
         new ArrayList<Map<String, ? extends Transaction>>();
   private static List<Map<String, ? extends BasicAdvice>> allConfirmations =
         new ArrayList<Map<String, ? extends BasicAdvice>>();
   private static List<Map<String, ? extends BasicReversal>> allReversals =
         new ArrayList<Map<String, ? extends BasicReversal>>();
   private static List<Map<String, ?>> allMessages = new ArrayList<Map<String, ? extends Object>>();

   private static HashMap<String, PaymentResponse> paymentResponses = new HashMap<String, PaymentResponse>();

   public static void init() throws IOException {

      AccountLoader accountLoader = new AccountLoader(accounts);
      accountLoader.loadAccounts();

   }

   public static BillPayAccount getAccount(String accountRef) {
      return accounts.get(accountRef);
   }

   public static void add(BillPayAccount account) {
      if (account != null && account.getAccountRef() != null) {
         accounts.put(account.getAccountRef(), account);
      }
   }

   public static boolean add(Transaction request) {
      if (request instanceof AccountLookupRequest) {
         return add((AccountLookupRequest) request);
      } else if (request instanceof PaymentRequest) {
         return add((PaymentRequest) request);
      } else if (request instanceof RefundRequest) {
         return add((RefundRequest) request);
      }
      return false;
   }

   public static boolean add(AccountLookupRequest accountLookupRequest) {
      if (existsMessage(accountLookupRequest.getId())) {
         return false;
      }

      accountLookups.put(accountLookupRequest.getId(), accountLookupRequest);
      return true;
   }

   public static boolean add(PaymentRequest paymentRequest) {
      if (existsMessage(paymentRequest.getId())) {
         return false;
      }

      paymentRequests.put(paymentRequest.getId(), paymentRequest);
      return true;
   }

   public static boolean add(PaymentResponse paymentResponse) {

      paymentResponses.put(paymentResponse.getSlipData().getIssuerReference(), paymentResponse);
      return true;
   }

   public static boolean add(RefundRequest refundRequest) {
      if (existsMessage(refundRequest.getId())) {
         return false;
      }

      refundRequests.put(refundRequest.getId(), refundRequest);
      return true;
   }

   public static boolean add(TenderAdvice tenderAdvice) {
      if (existsMessage(tenderAdvice.getId())) {
         return false;
      }

      paymentConfirmations.put(tenderAdvice.getId(), tenderAdvice);
      return true;
   }

   public static boolean add(BasicAdvice advice, boolean isPaymentMessage) {
      if (existsMessage(advice.getId())) {
         return false;
      }

      if (advice instanceof TenderAdvice) {
         return add((TenderAdvice) advice);
      } else if (advice instanceof BasicReversal) {
         if (isPaymentMessage) {
            return addPaymentReversal((BasicReversal) advice);
         } else {
            return addRefundReversal((BasicReversal) advice);
         }
      }

      refundConfirmation.put(advice.getId(), advice);
      return true;
   }

   public static boolean addPaymentReversal(BasicReversal paymentReversal) {
      if (existsMessage(paymentReversal.getId())) {
         return false;
      }

      paymentReversals.put(paymentReversal.getId(), paymentReversal);
      return true;
   }

   public static boolean addRefundReversal(BasicReversal refundReversal) {
      if (existsMessage(refundReversal.getId())) {
         return false;
      }

      refundReversals.put(refundReversal.getId(), refundReversal);
      return true;
   }

   public static boolean existsMessage(String uuid) {
      for (Map<String, ? extends Object> map : allMessages) {
         if (map.containsKey(uuid)) {
            return true;
         }
      }
      return false;
   }

   public static boolean removeMessage(String uuid) {
      for (Map<String, ? extends Object> map : allMessages) {
         if (map.containsKey(uuid)) {
            map.remove(uuid);
         }
      }
      return false;
   }

   public static Transaction getRequest(String uuid) {
      for (Map<String, ? extends Transaction> map : allRequests) {
         if (map.containsKey(uuid)) {
            return map.get(uuid);
         }
      }
      return null;
   }

   public static BasicAdvice getRequestConfirmation(String requestId) {
      for (Map<String, ? extends BasicAdvice> map : allConfirmations) {
         for (Map.Entry<String, ? extends BasicAdvice> entry : map.entrySet()) {

            if (entry.getValue().getRequestId().equals(requestId)) {
               return entry.getValue();
            }
         }
      }

      return null;
   }

   public static BasicReversal getRequestReversal(String requestId) {
      for (Map<String, ? extends BasicReversal> map : allReversals) {
         for (Map.Entry<String, ? extends BasicReversal> entry : map.entrySet()) {

            if (entry.getValue().getRequestId().equals(requestId)) {
               return entry.getValue();
            }
         }
      }

      return null;
   }

   public static PaymentResponse getPaymentResponse(String issuerRefNum) {
      return paymentResponses.get(issuerRefNum);
   }

   public static BillPayAccount[] getAccounts() {
      return accounts.values().toArray(new BillPayAccount[] {});
   }

   public static AccountLookupRequest[] getAccountLookupRequests() {
      return accountLookups.values().toArray(new AccountLookupRequest[] {});
   }

   public static PaymentRequest[] getPaymentRequests() {
      return paymentRequests.values().toArray(new PaymentRequest[] {});
   }

   public static RefundRequest[] getRefundRequests() {
      return refundRequests.values().toArray(new RefundRequest[] {});
   }

   public static TenderAdvice[] getPaymentConfirmations() {
      return paymentConfirmations.values().toArray(new TenderAdvice[] {});
   }

   public static BasicAdvice[] getRefundConfirmations() {
      return refundConfirmation.values().toArray(new BasicAdvice[] {});
   }

   public static BasicReversal[] getPaymentReversals() {
      return paymentReversals.values().toArray(new BasicReversal[] {});
   }

   public static BasicReversal[] getRefundReversals() {
      return refundReversals.values().toArray(new BasicReversal[] {});
   }

   static {
      allRequests.add(accountLookups);
      allRequests.add(paymentRequests);
      allRequests.add(refundRequests);

      allConfirmations.add(paymentConfirmations);
      allConfirmations.add(refundConfirmation);

      allReversals.add(paymentReversals);
      allReversals.add(refundReversals);

      allMessages.add(accountLookups);
      allMessages.add(paymentRequests);
      allMessages.add(refundRequests);

      allMessages.add(paymentConfirmations);
      allMessages.add(refundConfirmation);

      allMessages.add(paymentReversals);
      allMessages.add(refundReversals);

   }

   public static Date getLastResetTime() {
      return lastResetTime;
   }

   public static void reset() throws IOException {
      lastResetTime = new Date(System.currentTimeMillis());
      log.info("Clearing all messages");
      accountLookups.clear();
      paymentRequests.clear();
      refundRequests.clear();

      paymentConfirmations.clear();
      refundConfirmation.clear();

      paymentReversals.clear();
      refundReversals.clear();

      allRequests.clear();
      allConfirmations.clear();
      allReversals.clear();
      allMessages.clear();

      log.info("Clearing and reloading accounts");
      accounts.clear();
      AccountLoader accountLoader = new AccountLoader(accounts);
      accountLoader.loadAccounts();
   }
}
