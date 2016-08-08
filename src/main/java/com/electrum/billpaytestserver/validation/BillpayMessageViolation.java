package com.electrum.billpaytestserver.validation;

/**
 *
 */
public class BillpayMessageViolation {
   private String messageProperty;
   private String field;
   private String format;
   private Object invalidValue;

   public BillpayMessageViolation(String messageProperty, String field, String format, Object invalidValue) {
      this.messageProperty = messageProperty;
      this.field = field;
      this.format = format;
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

   public String getFormat() {
      return format;
   }

   public void setFormat(String format) {
      this.format = format;
   }

   public Object getInvalidValue() {
      return invalidValue;
   }

   public void setInvalidValue(String invalidValue) {
      this.invalidValue = invalidValue;
   }
}
