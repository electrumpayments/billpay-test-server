package com.electrum.billpaytestserver.engine;

import io.electrum.billpay.model.ErrorDetail;

import javax.ws.rs.core.Response;

/**
 *
 */
public class ErrorDetailFactory {

   public static Response getNotUniqueUuidErrorDetail() {
      ErrorDetail errorDetail = new ErrorDetail();
      errorDetail.setErrorType(ErrorDetail.ErrorType.DUPLICATE_RECORD);
      errorDetail.setErrorMessage("Message ID (UUID) is not unique.");
      return Response.status(Response.Status.BAD_REQUEST).entity(errorDetail).build();
   }

   public static Response getIllFormattedMessageErrorDetail(ValidationResult result) {
      ErrorDetail errorDetail = new ErrorDetail();
      errorDetail.setErrorType(ErrorDetail.ErrorType.FORMAT_ERROR);
      errorDetail.setErrorMessage(
            (result == null ? "Mandatory fields missing - check server logs." : result.getInvalidityReason()));
      return Response.status(Response.Status.BAD_REQUEST).entity(errorDetail).build();
   }

   public static Response getNoAccountFoundErrorDetail(String accountRef) {
      ErrorDetail errorDetail = new ErrorDetail();
      errorDetail.setErrorType(ErrorDetail.ErrorType.UNKNOWN_CUSTOMER_ACCOUNT);
      errorDetail.setErrorMessage(
            "No customer account found for given accountRef (" + accountRef
                  + "). Use GET /test/allAccounts to see available test accounts");
      return Response.status(Response.Status.BAD_REQUEST).entity(errorDetail).build();
   }
}
