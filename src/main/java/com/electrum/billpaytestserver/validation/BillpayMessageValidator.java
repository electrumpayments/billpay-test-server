package com.electrum.billpaytestserver.validation;

import io.electrum.billpay.model.AccountLookupRequest;
import io.electrum.billpay.model.PaymentRequest;
import io.electrum.vas.model.BasicRequest;
import io.electrum.vas.model.Merchant;
import io.electrum.vas.model.MerchantName;
import io.electrum.vas.model.Sender;

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
         result.addViolation(getViolation("Message", "", "", null));
         return result;
      }

      validate(request, result);

      if (request instanceof AccountLookupRequest || request instanceof PaymentRequest) {
         validateValue(request, "message", "accountRef", result);
      }

      return result;
   }

   private static void validate(BasicRequest request, ValidationResult result) {

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

      if (strictMode || !isEmpty(sender.getMerchant())) {
         validateValue(sender, "sender", "merchant", result);
         validate(sender.getMerchant(), result);
      }

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

   private static boolean isNonMandatoryEmpty(Object o) {
      if (!strictMode) {
         return false;
      } else {
         return isEmpty(o);
      }
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
