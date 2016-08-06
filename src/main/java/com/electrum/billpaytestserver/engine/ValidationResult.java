package com.electrum.billpaytestserver.engine;

/**
 *
 */
public class ValidationResult {
   private boolean isValid = true;
   private String invalidityReason = "";

   public ValidationResult() {
   }

   public ValidationResult(boolean isValid, String reason) {
      this.isValid = isValid;
      this.invalidityReason = reason;
   }

   public boolean isValid() {
      return isValid;
   }

   public void setValid(boolean valid) {
      isValid = valid;
   }

   public String getInvalidityReason() {
      return invalidityReason;
   }

   public void setInvalidityReason(String invalidityReason) {
      this.setValid(false);
      this.invalidityReason = invalidityReason;
   }

   public void addInvalidityReason(String reason) {
      this.setValid(false);
      this.invalidityReason += "/n" + reason;
   }
}
