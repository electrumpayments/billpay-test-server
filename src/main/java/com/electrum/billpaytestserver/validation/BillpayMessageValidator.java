package com.electrum.billpaytestserver.validation;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import io.electrum.billpay.model.AccountLookupRequest;
import io.electrum.billpay.model.PaymentRequest;
import io.electrum.vas.model.*;

public class BillpayMessageValidator {

   private static Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

   public static ValidationResult validate(Transaction request) {
      ValidationResult result = new ValidationResult();
      if (isEmpty(request)) {
         result.addViolation(getViolation("message", "", "", null));
         return result;
      }

      validate(request, result);

      if (request instanceof AccountLookupRequest || request instanceof PaymentRequest) {
         validateValue(request, "message", "accountRef", result);
      }

      if (request instanceof PaymentRequest) {
         validateValue(request, "message", "amounts", result);
         validate(((PaymentRequest) request).getAmounts(), result);
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
      } else if (advice instanceof BasicReversal) {
         validate((BasicReversal) advice, result);
      }

      validate(advice, result);

      return result;
   }

   private static void validate(Amounts amounts, ValidationResult result) {
      validateValue(amounts, "amounts", "requestAmount", result);
   }

   private static void validate(TenderAdvice advice, ValidationResult result) {
      validateValue(advice, "message", "tenders", result);
   }

   public static void validate(BasicReversal reversal, ValidationResult result) {
      validateValue(reversal, "message", "reversalReason", result);
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
