package com.electrum.billpaytestserver.engine;

import io.electrum.billpay.model.AccountLookupRequest;
import io.electrum.billpay.model.PaymentRequest;
import io.electrum.billpay.model.PaymentReversal;
import io.electrum.billpay.model.RefundRequest;
import io.electrum.billpay.model.RefundReversal;
import io.electrum.vas.model.BasicAdvice;
import io.electrum.vas.model.BasicRequest;
import io.electrum.vas.model.BasicReversal;
import io.electrum.vas.model.TenderAdvice;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.electrum.billpaytestserver.account.AccountLoader;
import com.electrum.billpaytestserver.account.BillPayAccount;

/**
 *
 */
public class MockBillPayBackend {

   private static HashMap<String, BillPayAccount> accounts = new HashMap();

   private static HashMap<UUID, AccountLookupRequest> accountLookups = new HashMap();
   private static HashMap<UUID, PaymentRequest> paymentRequests = new HashMap();
   private static HashMap<UUID, RefundRequest> refundRequests = new HashMap();

   private static HashMap<UUID, TenderAdvice> paymentConfirmations = new HashMap();
   private static HashMap<UUID, BasicAdvice> refundConfirmation = new HashMap();

   private static HashMap<UUID, PaymentReversal> paymentReversals = new HashMap();
   private static HashMap<UUID, RefundReversal> refundReversals = new HashMap();

   private static List<Map<UUID, ? extends BasicRequest>> allRequests =
         new ArrayList<Map<UUID, ? extends BasicRequest>>();
   private static List<Map<UUID, ? extends BasicAdvice>> allConfirmations =
         new ArrayList<Map<UUID, ? extends BasicAdvice>>();
   private static List<Map<UUID, ? extends BasicReversal>> allReversals =
         new ArrayList<Map<UUID, ? extends BasicReversal>>();
   private static List<Map<UUID, ?>> allMessages = new ArrayList<Map<UUID, ? extends Object>>();

   public static void init() throws IOException {

      AccountLoader accountLoader = new AccountLoader(accounts);
      accountLoader.loadAccounts();

   }

   public static BillPayAccount getAccount(String accountRef) {
      return accounts.get(accountRef);
   }

   public static boolean add(BasicRequest request) {
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

   public static boolean add(BasicAdvice advice) {
      if (existsMessage(advice.getId())) {
         return false;
      }

      refundConfirmation.put(advice.getId(), advice);
      return true;
   }

   public static boolean add(PaymentReversal paymentReversal) {
      if (existsMessage(paymentReversal.getId())) {
         return false;
      }

      paymentReversals.put(paymentReversal.getId(), paymentReversal);
      return true;
   }

   public static boolean add(RefundReversal refundReversal) {
      if (existsMessage(refundReversal.getId())) {
         return false;
      }

      refundReversals.put(refundReversal.getId(), refundReversal);
      return true;
   }

   private static boolean existsMessage(UUID uuid) {
      for (Map<UUID, ? extends Object> map : allMessages) {
         if (map.containsKey(uuid)) {
            return true;
         }
      }
      return false;
   }

   private static BasicRequest getRequest(UUID uuid) {
      for (Map<UUID, ? extends BasicRequest> map : allRequests) {
         if (map.containsKey(uuid)) {
            return map.get(uuid);
         }
      }
      return null;
   }

   public static BasicRequest getOrigRequest(UUID id) {
      return getRequest(id);
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
}
