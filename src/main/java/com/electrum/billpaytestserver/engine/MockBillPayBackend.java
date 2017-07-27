package com.electrum.billpaytestserver.engine;

import java.io.IOException;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.electrum.billpaytestserver.account.AccountLoader;
import com.electrum.billpaytestserver.account.BillPayAccount;

import io.electrum.billpay.model.*;
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

   private static HashMap<String, BillPayAccount> accounts = new HashMap<>();

   private static HashMap<String, AccountLookupRequest> accountLookups = new HashMap<>();
   private static HashMap<String, TrafficFineLookupRequest> trafficFineLookups = new HashMap<>();
   private static HashMap<String, PolicyLookupRequest> policyLookups = new HashMap<>();
   private static HashMap<String, PaymentRequest> paymentRequests = new HashMap<>();
   private static HashMap<String, TrafficFinePaymentRequest> trafficFinePaymentRequests = new HashMap<>();
   private static HashMap<String, PolicyPaymentRequest> policyPaymentRequests = new HashMap<>();
   private static HashMap<String, RefundRequest> refundRequests = new HashMap<>();

   private static HashMap<String, TenderAdvice> paymentConfirmations = new HashMap<>();
   private static HashMap<String, BasicAdvice> refundConfirmation = new HashMap<>();

   private static HashMap<String, BasicReversal> paymentReversals = new HashMap<>();
   private static HashMap<String, BasicReversal> refundReversals = new HashMap<>();

   private static List<Map<String, ? extends Transaction>> allRequests = new ArrayList<>();
   private static List<Map<String, ? extends BasicAdvice>> allConfirmations = new ArrayList<>();
   private static List<Map<String, ? extends BasicReversal>> allReversals = new ArrayList<>();
   private static List<Map<String, ?>> allMessages = new ArrayList<>();

   private static HashMap<String, PaymentResponse> paymentResponses = new HashMap<>();
   private static HashMap<String, TrafficFinePaymentResponse> trafficFinePaymentResponses = new HashMap<>();
   private static HashMap<String, PolicyPaymentResponse> policyPaymentResponses = new HashMap<>();

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
      if (request instanceof AccountLookupRequest)
         return add((AccountLookupRequest) request);
      if (request instanceof TrafficFineLookupRequest)
         return add((TrafficFineLookupRequest) request);
      if (request instanceof PolicyLookupRequest)
         return add((PolicyLookupRequest) request);
      if (request instanceof PaymentRequest)
         return add((PaymentRequest) request);
      if (request instanceof TrafficFinePaymentRequest)
         return add((TrafficFinePaymentRequest) request);
      if (request instanceof PolicyPaymentRequest)
         return add((PolicyPaymentRequest) request);
      if (request instanceof RefundRequest)
         return add((RefundRequest) request);

      return false;
   }

   public static boolean add(AccountLookupRequest accountLookupRequest) {
      if (existsMessage(accountLookupRequest.getId())) {
         return false;
      }

      accountLookups.put(accountLookupRequest.getId(), accountLookupRequest);
      return true;
   }

   public static boolean add(TrafficFineLookupRequest trafficFineLookupRequest) {
      if (existsMessage(trafficFineLookupRequest.getId())) {
         return false;
      }

      trafficFineLookups.put(trafficFineLookupRequest.getId(), trafficFineLookupRequest);
      return true;
   }

   public static boolean add(PolicyLookupRequest policyLookupRequest) {
      if (existsMessage(policyLookupRequest.getId())) {
         return false;
      }

      policyLookups.put(policyLookupRequest.getId(), policyLookupRequest);
      return true;
   }

   public static boolean add(PaymentRequest paymentRequest) {
      if (existsMessage(paymentRequest.getId())) {
         return false;
      }

      paymentRequests.put(paymentRequest.getId(), paymentRequest);
      return true;
   }

   public static boolean add(TrafficFinePaymentRequest trafficFinePaymentRequest) {
      if (existsMessage(trafficFinePaymentRequest.getId())) {
         return false;
      }

      trafficFinePaymentRequests.put(trafficFinePaymentRequest.getId(), trafficFinePaymentRequest);
      return true;
   }

   public static boolean add(PolicyPaymentRequest policyPaymentRequest) {
      if (existsMessage(policyPaymentRequest.getId())) {
         return false;
      }

      policyPaymentRequests.put(policyPaymentRequest.getId(), policyPaymentRequest);
      return true;
   }

   public static boolean add(PaymentResponse paymentResponse) {

      paymentResponses.put(paymentResponse.getSlipData().getIssuerReference(), paymentResponse);
      return true;
   }

   public static boolean add(TrafficFinePaymentResponse trafficFinePaymentResponse) {

      trafficFinePaymentResponses
            .put(trafficFinePaymentResponse.getSlipData().getIssuerReference(), trafficFinePaymentResponse);
      return true;
   }

   public static boolean add(PolicyPaymentResponse policyPaymentResponse) {

      policyPaymentResponses.put(policyPaymentResponse.getSlipData().getIssuerReference(), policyPaymentResponse);
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

   public static TrafficFineLookupRequest[] getTrafficFineLookups() {
      return trafficFineLookups.values().toArray(new TrafficFineLookupRequest[] {});
   }

   public static TrafficFinePaymentRequest[] getTrafficFinePaymentRequests() {
      return trafficFinePaymentRequests.values().toArray(new TrafficFinePaymentRequest[] {});
   }

   public static TrafficFinePaymentResponse[] getTrafficFinePaymentResponses() {
      return trafficFinePaymentResponses.values().toArray(new TrafficFinePaymentResponse[] {});
   }

   public static PolicyLookupRequest[] getPolicyLookups() {
      return policyLookups.values().toArray(new PolicyLookupRequest[] {});
   }

   public static PolicyPaymentRequest[] getPolicyPaymentRequests() {
      return policyPaymentRequests.values().toArray(new PolicyPaymentRequest[] {});
   }

   public static PolicyPaymentResponse[] getPolicyPaymentResponses() {
      return policyPaymentResponses.values().toArray(new PolicyPaymentResponse[] {});
   }

   static {
      allRequests.add(accountLookups);
      allRequests.add(trafficFineLookups);
      allRequests.add(policyLookups);
      allRequests.add(paymentRequests);
      allRequests.add(trafficFinePaymentRequests);
      allRequests.add(policyPaymentRequests);
      allRequests.add(refundRequests);

      allConfirmations.add(paymentConfirmations);
      allConfirmations.add(refundConfirmation);

      allReversals.add(paymentReversals);
      allReversals.add(refundReversals);

      allMessages.add(accountLookups);
      allMessages.add(trafficFineLookups);
      allMessages.add(policyLookups);
      allMessages.add(paymentRequests);
      allMessages.add(trafficFinePaymentRequests);
      allMessages.add(policyPaymentRequests);
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
      trafficFineLookups.clear();
      policyLookups.clear();
      paymentRequests.clear();
      trafficFinePaymentRequests.clear();
      policyPaymentRequests.clear();
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
