package com.electrum.billpaytestserver.validation;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class ValidationResult {
   private boolean isValid = true;
   private ArrayList<BillpayMessageViolation> violations = new ArrayList<>();

   public ValidationResult() {
   }

   public boolean isValid() {
      return isValid;
   }

   public void setValid(boolean valid) {
      isValid = valid;
   }

   public List<BillpayMessageViolation> getViolations() {
      return violations;
   }

   public void addViolation(BillpayMessageViolation reason) {
      this.setValid(false);
      violations.add(reason);

   }
}
