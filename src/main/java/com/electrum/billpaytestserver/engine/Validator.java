package com.electrum.billpaytestserver.engine;

import io.electrum.billpay.model.AccountLookupRequest;
import io.electrum.billpay.model.PaymentRequest;
import io.electrum.vas.model.BasicRequest;
import io.electrum.vas.model.Merchant;
import io.electrum.vas.model.MerchantName;
import io.electrum.vas.model.Sender;

/**
 *
 */
public class Validator {

   private static boolean strictMode = true;

   public static ValidationResult validate(BasicRequest request) {
      ValidationResult result = new ValidationResult();
      if (isEmpty(request)) {
         result.addInvalidityReason("Request is null");
         return result;
      }

      validate(request, result);

      if (request instanceof AccountLookupRequest) {
         if (isEmpty(((AccountLookupRequest) request).getAccountRef())) {
            result.addInvalidityReason("Message AccountRef must have a value");
         }
      } else if (request instanceof PaymentRequest) {
         if (isEmpty(((PaymentRequest) request).getAccountRef())) {
            result.addInvalidityReason("Message AccountRef must have a value");
         }
      }
      return result;
   }

   private static void validate(BasicRequest request, ValidationResult result) {
      if (isEmpty(request.getId())) {
         result.addInvalidityReason("Message ID is null");
      }

      if (isEmpty(request.getTime())) {
         result.addInvalidityReason("Message Time is null");
      }
      validate(request.getSender(), result);

   }

   private static void validate(Sender sender, ValidationResult result) {
      if (isEmpty(sender)) {
         result.addInvalidityReason("Message Sender is null");
      }

      if (isEmpty(sender.getTerminalId())) {
         result.addInvalidityReason("Message Sender TerminalId must have a value");
      }

      if (isNonMandatoryEmpty(sender.getReferenceNumber())) {
         result.addInvalidityReason("Message Sender ReferenceNumber must have a value");
      }

      if (strictMode || !isEmpty(sender.getMerchant())) {
         validate(sender.getMerchant(), result);
      }

   }

   private static void validate(Merchant merchant, ValidationResult result) {
      if (isEmpty(merchant)) {
         result.addInvalidityReason("Message Merchant is null");
      }

      if (isEmpty(merchant.getMerchantType())) {
         result.addInvalidityReason("Message Merchant MerchantType must have a value");
      }

      if (isEmpty(merchant.getMerchantId())) {
         result.addInvalidityReason("Message Merchant MerchantId must have a value");
      }

      if (strictMode || !isEmpty(merchant.getMerchantId())) {
         validate(merchant.getMerchantName(), result);
      }
   }

   private static void validate(MerchantName merchantName, ValidationResult result) {
      if (isEmpty(merchantName)) {
         result.addInvalidityReason("Message Merchant MerchantName must have a value");
      }

      if (isEmpty(merchantName.getName())) {
         result.addInvalidityReason("Message Merchant MerchantName Name must have a value");
      }

      if (isEmpty(merchantName.getCity())) {
         result.addInvalidityReason("Message Merchant MerchantName City must have a value");
      }

      if (isEmpty(merchantName.getRegion())) {
         result.addInvalidityReason("Message Merchant MerchantName Region must have a value");
      }

      if (isEmpty(merchantName.getCountry())) {
         result.addInvalidityReason("Message Merchant MerchantName Country must have a value");
      }
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

}
