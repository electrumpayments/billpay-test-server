package com.electrum.billpaytestserver.validation;

/**
 *
 */
public class BillpayMessageViolation {
   private String messageProperty;
   private String field;
   private String error;
   private Object invalidValue;

   public BillpayMessageViolation(String messageProperty, String field, String error, Object invalidValue) {
      this.messageProperty = messageProperty;
      this.field = field;
      this.error = error;
      this.invalidValue = invalidValue;
   }

   public String getMessageProperty() {
      return messageProperty;
   }

   public void setMessageProperty(String messageProperty) {
      this.messageProperty = messageProperty;
   }

   public String getField() {
      return field;
   }

   public void setField(String field) {
      this.field = field;
   }

   public String getError() {
      return error;
   }

   public void setError(String error) {
      this.error = error;
   }

   public Object getInvalidValue() {
      return invalidValue;
   }

   public void setInvalidValue(String invalidValue) {
      this.invalidValue = invalidValue;
   }
}
