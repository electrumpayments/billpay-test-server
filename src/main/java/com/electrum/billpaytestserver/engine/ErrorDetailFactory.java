package com.electrum.billpaytestserver.engine;

import io.electrum.billpay.model.ErrorDetail;

import java.util.UUID;

import javax.ws.rs.core.Response;

import com.electrum.billpaytestserver.validation.ValidationResult;

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
      errorDetail.setErrorMessage("See error detail for format errors.");
      errorDetail.setDetailMessage(
            (result == null ? "Mandatory fields missing - check server logs." : result.getViolations()));
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

   public static Response getNoPrecedingRequestFoundErrorDetail(UUID id) {
      ErrorDetail errorDetail = new ErrorDetail();
      errorDetail.setErrorType(ErrorDetail.ErrorType.UNABLE_TO_LOCATE_RECORD);
      errorDetail.setErrorMessage(
            "No preceding request (ID: " + id.toString()
                  + ") found for advice. Use GET /test/allPaymentRequests or /test/allRefundRequests to see all requests");
      return Response.status(Response.Status.BAD_REQUEST).entity(errorDetail).build();
   }

}
