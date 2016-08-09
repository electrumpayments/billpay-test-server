package com.electrum.billpaytestserver.validation;

import io.electrum.billpay.model.AccountLookupRequest;
import io.electrum.billpay.model.PaymentRequest;
import io.electrum.billpay.model.PaymentReversal;
import io.electrum.billpay.model.RefundReversal;
import io.electrum.vas.model.BasicAdvice;
import io.electrum.vas.model.BasicRequest;
import io.electrum.vas.model.BasicReversal;
import io.electrum.vas.model.Institution;
import io.electrum.vas.model.Merchant;
import io.electrum.vas.model.MerchantName;
import io.electrum.vas.model.Sender;
import io.electrum.vas.model.TenderAdvice;

import java.util.HashSet;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

/**
 *
 */
public class BillpayMessageValidator {

   private static boolean strictMode = true;
   private static Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

   public static ValidationResult validate(BasicRequest request) {
      ValidationResult result = new ValidationResult();
      if (isEmpty(request)) {
         result.addViolation(getViolation("message", "", "", null));
         return result;
      }

      validate(request, result);

      if (request instanceof AccountLookupRequest || request instanceof PaymentRequest) {
         validateValue(request, "message", "accountRef", result);
      }

      return result;
   }

   public static ValidationResult validate(TenderAdvice advice) {
      ValidationResult result = new ValidationResult();
      if (isEmpty(advice)) {
         result.addViolation(getViolation("message", "", "", null));
         return result;
      }

      validateValue(advice, "message", "tenders", result);

      validate(advice, result);

      return result;
   }

   public static ValidationResult validate(BasicAdvice advice) {
      ValidationResult result = new ValidationResult();
      if (isEmpty(advice)) {
         result.addViolation(getViolation("message", "", "", null));
         return result;
      }

      validate(advice, result);

      return result;
   }

   public static ValidationResult validate(PaymentReversal reversal) {
      ValidationResult result = new ValidationResult();
      if (isEmpty(reversal)) {
         result.addViolation(getViolation("Message", "", "", null));
         return result;
      }

      validateValue(reversal, "message", "paymentRequest", result);
      validateValue(reversal.getPaymentRequest(), "paymentRequest", "accountRef", result);
      validate(reversal.getPaymentRequest(), result);

      validate(reversal, result);

      return result;
   }

   public static ValidationResult validate(RefundReversal reversal) {
      ValidationResult result = new ValidationResult();
      if (isEmpty(reversal)) {
         result.addViolation(getViolation("message", "", "", null));
         return result;
      }

      validateValue(reversal, "message", "refundRequest", result);
      validateValue(reversal.getRefundRequest(), "refundRequest", "issuerReference", result);
      validateValue(reversal.getRefundRequest(), "refundRequest", "refundReason", result);
      validate(reversal.getRefundRequest(), result);

      validate(reversal, result);

      return result;
   }

   private static void validate(BasicReversal reversal, ValidationResult result) {
      if (isEmpty(reversal)) {
         return;
      }

      validateValue(reversal, "reversalReason", "id", result);
      validate((BasicAdvice) reversal, result);

   }

   private static void validate(BasicAdvice advice, ValidationResult result) {
      if (isEmpty(advice)) {
         return;
      }

      validateValue(advice, "message", "id", result);
      validateValue(advice, "message", "requestId", result);
      validateValue(advice, "message", "time", result);
      validateValue(advice, "message", "linkData", result);

   }

   private static void validate(BasicRequest request, ValidationResult result) {
      if (isEmpty(request)) {
         return;
      }

      validateValue(request, "message", "id", result);
      validateValue(request, "message", "time", result);
      validateValue(request, "message", "sender", result);
      validate(request.getSender(), result);

   }

   private static void validate(Sender sender, ValidationResult result) {
      if (isEmpty(sender)) {
         return;
      }

      validateValue(sender, "sender", "terminalId", result);
      validateValue(sender, "sender", "referenceNumber", result);
      validateValue(sender, "sender", "institution", result);
      validate(sender.getInstitution(), result);

      if (strictMode || !isEmpty(sender.getMerchant())) {
         validateValue(sender, "sender", "merchant", result);
         validate(sender.getMerchant(), result);
      }

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

      if (strictMode || !isEmpty(merchant.getMerchantName())) {
         validateValue(merchant, "merchant", "merchantName", result);
         validate(merchant.getMerchantName(), result);
      }
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
