package com.electrum.billpaytestserver.validation;

import io.electrum.billpay.model.AccountLookupRequest;
import io.electrum.billpay.model.PaymentRequest;
import io.electrum.billpay.model.PaymentReversal;
import io.electrum.billpay.model.RefundReversal;
import io.electrum.vas.model.BasicAdvice;
import io.electrum.vas.model.Institution;
import io.electrum.vas.model.LedgerAmount;
import io.electrum.vas.model.Merchant;
import io.electrum.vas.model.MerchantName;
import io.electrum.vas.model.Originator;
import io.electrum.vas.model.Tender;
import io.electrum.vas.model.TenderAdvice;
import io.electrum.vas.model.ThirdPartyIdentifier;
import io.electrum.vas.model.Transaction;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

public class BillpayMessageValidator {

   private static Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

   public static ValidationResult validate(Transaction request) {
      ValidationResult result = new ValidationResult();
      if (isEmpty(request)) {
         result.addViolation(getViolation("message", "", "", null));
         return result;
      }

      validate(request, result);

      if (request instanceof AccountLookupRequest) {
         validateValue(request, "message", "accountRef", result);
      }

      if (request instanceof PaymentRequest) {
         validateValue(request, "message", "accountRef", result);
         validateValue(request, "message", "requestAmount", result);
         validate(((PaymentRequest) request).getRequestAmount(), result);
      }

      return result;
   }

   public static ValidationResult validate(BasicAdvice advice) {
      ValidationResult result = new ValidationResult();
      if (isEmpty(advice)) {
         result.addViolation(getViolation("message", "", "", null));
         return result;
      }

      if (advice instanceof TenderAdvice) {
         validate((TenderAdvice) advice, result);
      } else if (advice instanceof PaymentReversal) {
         validate((PaymentReversal) advice, result);
      } else if (advice instanceof RefundReversal) {
         validate((RefundReversal) advice, result);
      }

      validate(advice, result);

      return result;
   }

   private static void validate(TenderAdvice advice, ValidationResult result) {
      validateValue(advice, "message", "tenders", result);
      for (Tender tender : advice.getTenders()) {
         validate(tender, result);
      }

   }

   public static void validate(PaymentReversal reversal, ValidationResult result) {
      validateValue(reversal, "message", "reversalReason", result);
      validateValue(reversal, "message", "paymentRequest", result);
      validateValue(reversal.getPaymentRequest(), "paymentRequest", "accountRef", result);
      validate(reversal.getPaymentRequest(), result);
   }

   public static void validate(RefundReversal reversal, ValidationResult result) {
      validateValue(reversal, "message", "reversalReason", result);
      validateValue(reversal, "message", "refundRequest", result);
      validateValue(reversal.getRefundRequest(), "refundRequest", "issuerReference", result);
      validateValue(reversal.getRefundRequest(), "refundRequest", "refundReason", result);
      validate(reversal.getRefundRequest(), result);
   }

   private static void validate(BasicAdvice advice, ValidationResult result) {
      if (isEmpty(advice)) {
         return;
      }

      validateValue(advice, "message", "id", result);
      validateValue(advice, "message", "requestId", result);
      validateValue(advice, "message", "time", result);
      validateValue(advice, "message", "thirdPartyIdentifiers", result);
      validate(advice.getThirdPartyIdentifiers(), result);

   }

   private static void validate(Transaction request, ValidationResult result) {
      if (isEmpty(request)) {
         return;
      }

      validateValue(request, "message", "id", result);
      validateValue(request, "message", "time", result);
      validateValue(request, "message", "originator", result);
      validate(request.getOriginator(), result);
      validateValue(request, "message", "settlementEntity", result);
      validate(request.getSettlementEntity(), result);
      validateValue(request, "message", "receiver", result);
      validate(request.getReceiver(), result);
      validateValue(request, "message", "client", result);
      validate(request.getClient(), result);
      validateValue(request, "message", "thirdPartyIdentifiers", result);
      validate(request.getThirdPartyIdentifiers(), result);

   }

   private static void validate(Originator originator, ValidationResult result) {
      if (isEmpty(originator)) {
         return;
      }

      validateValue(originator, "originator", "terminalId", result);
      validateValue(originator, "originator", "institution", result);
      validate(originator.getInstitution(), result);
      validateValue(originator, "originator", "merchant", result);
      validate(originator.getMerchant(), result);

   }

   private static void validate(List<ThirdPartyIdentifier> thirdPartyIdentifiers, ValidationResult result) {
      if (isEmpty(thirdPartyIdentifiers)) {
         return;
      }

      for (ThirdPartyIdentifier thirdPartyIdentifier : thirdPartyIdentifiers) {
         validate(thirdPartyIdentifier, result);
      }
   }

   private static void validate(ThirdPartyIdentifier thirdPartyIdentifier, ValidationResult result) {
      if (isEmpty(thirdPartyIdentifier)) {
         return;
      }

      validateValue(thirdPartyIdentifier, "thirdPartyIdentifier", "institutionId", result);
      validateValue(thirdPartyIdentifier, "thirdPartyIdentifier", "transactionIdentifier", result);

   }

   private static void validate(Tender tender, ValidationResult result) {
      if (isEmpty(tender)) {
         return;
      }

      validateValue(tender, "tender", "accountType", result);
      validateValue(tender, "tender", "amount", result);
      validate(tender.getAmount(), result);
      validateValue(tender, "tender", "cardNumber", result);
      validateValue(tender, "tender", "reference", result);
      validateValue(tender, "tender", "tenderType", result);

   }

   private static void validate(LedgerAmount amount, ValidationResult result) {
      if (isEmpty(amount)) {
         return;
      }

      validateValue(amount, "ledgerAmount", "amount", result);
      validateValue(amount, "ledgerAmount", "currency", result);
      validateValue(amount, "ledgerAmount", "ledgerIndicator", result);

   }

   private static void validate(Institution institution, ValidationResult result) {
      if (isEmpty(institution)) {
         return;
      }

      validateValue(institution, "institution", "id", result);
      validateValue(institution, "institution", "name", result);

   }

   private static void validate(Merchant merchant, ValidationResult result) {
      if (isEmpty(merchant)) {
         return;
      }

      validateValue(merchant, "merchant", "merchantType", result);
      validateValue(merchant, "merchant", "merchantId", result);
      validateValue(merchant, "merchant", "merchantName", result);
      validate(merchant.getMerchantName(), result);
   }

   private static void validate(MerchantName merchantName, ValidationResult result) {
      if (isEmpty(merchantName)) {
         return;
      }

      validateValue(merchantName, "merchantName", "name", result);
      validateValue(merchantName, "merchantName", "city", result);
      validateValue(merchantName, "merchantName", "region", result);
      validateValue(merchantName, "merchantName", "country", result);
   }

   private static boolean isEmpty(Object o) {

      if (o == null) {
         return true;
      }

      if (o instanceof String) {
         return ((String) o).isEmpty();
      }

      if (o instanceof List) {
         return ((List) o).isEmpty();
      }

      return false;
   }

   private static <T> Set<ConstraintViolation<T>> validateValue(
         T tInstance,
         String parent,
         String propertyName,
         ValidationResult validationResult) {
      if (tInstance == null) {
         return new HashSet<ConstraintViolation<T>>();
      }

      Set<ConstraintViolation<T>> violations = validator.validateProperty(tInstance, propertyName);

      for (ConstraintViolation<T> constraintViolation : violations) {
         validationResult.addViolation(
               getViolation(
                     parent,
                     propertyName,
                     constraintViolation.getMessage(),
                     constraintViolation.getInvalidValue()));
      }

      return violations;
   }

   private static BillpayMessageViolation getViolation(
         String property,
         String field,
         String format,
         Object invalidValue) {
      return new BillpayMessageViolation(property, field, format, invalidValue);
   }

}
