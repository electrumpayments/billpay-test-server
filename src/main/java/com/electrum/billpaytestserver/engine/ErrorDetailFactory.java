package com.electrum.billpaytestserver.engine;

import io.electrum.vas.model.BasicAdvice;
import io.electrum.vas.model.ErrorDetail;

import java.util.UUID;

import javax.ws.rs.core.Response;

import com.electrum.billpaytestserver.account.BillPayAccount;
import com.electrum.billpaytestserver.validation.ValidationResult;

/**
 *
 */
public class ErrorDetailFactory {

   public static Response getServerErrorErrorDetail(Exception exception) {
      ErrorDetail errorDetail = new ErrorDetail();
      errorDetail.setErrorType(ErrorDetail.ErrorType.GENERAL_ERROR);
      errorDetail.setErrorMessage("Server error");
      errorDetail.setDetailMessage(exception.getMessage());
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorDetail).build();
   }

   public static Response getNotUniqueUuidErrorDetail(UUID id) {
      ErrorDetail errorDetail = new ErrorDetail();
      errorDetail.setErrorType(ErrorDetail.ErrorType.DUPLICATE_RECORD);
      errorDetail.setErrorMessage("Message ID (UUID) is not unique.");
      errorDetail.setDetailMessage(id);
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

   public static Response getMismatchingRequestAndAdviceErrorDetail(UUID id) {
      ErrorDetail errorDetail = new ErrorDetail();
      errorDetail.setErrorType(ErrorDetail.ErrorType.FUNCTION_NOT_SUPPORTED);
      errorDetail.setErrorMessage(
            "Request (ID: " + id.toString()
                  + ") found for advice is incompatible. Use GET /test/allPaymentRequests or /test/allRefundRequests to see all requests");
      return Response.status(Response.Status.BAD_REQUEST).entity(errorDetail).build();
   }

   public static Response getNoPaymentRequestFoundErrorDetail(String issuerRefNum) {
      ErrorDetail errorDetail = new ErrorDetail();
      errorDetail.setErrorType(ErrorDetail.ErrorType.UNABLE_TO_LOCATE_RECORD);
      errorDetail.setErrorMessage(
            "No preceding request (issuerReference: " + issuerRefNum
                  + ") found for advice. Use GET /test/allPaymentRequests or /test/allRefundRequests to see all requests");
      return Response.status(Response.Status.BAD_REQUEST).entity(errorDetail).build();
   }

   public static Response getPreviousAdviceReceivedErrorDetail(BasicAdvice advice) {
      ErrorDetail errorDetail = new ErrorDetail();
      errorDetail.setErrorType(ErrorDetail.ErrorType.ACCOUNT_ALREADY_SETTLED);
      if (advice != null) {
         errorDetail.setErrorMessage(
               "Preceding advice  (ID: " + advice.getId().toString()
                     + ") for request found. Use GET /test/allPaymentConfirmations or /test/allPaymentReversals or /test/allRefundConfirmations or /test/allRefundReversals to see all advices");
         errorDetail.setDetailMessage(advice);
      } else {
         errorDetail.setErrorMessage(
               "Preceding advice for request found. Use GET /test/allPaymentConfirmations or /test/allPaymentReversals or /test/allRefundConfirmations or /test/allRefundReversals to see all advices");
      }
      return Response.status(Response.Status.BAD_REQUEST).entity(errorDetail).build();
   }

   public static Response getAccountAddErrorErrorDetail(String error, BillPayAccount account) {
      ErrorDetail errorDetail = new ErrorDetail();
      errorDetail.setErrorType(ErrorDetail.ErrorType.FORMAT_ERROR);
      errorDetail.setErrorMessage(error);
      errorDetail.setDetailMessage(account);
      return Response.status(Response.Status.BAD_REQUEST).entity(errorDetail).build();
   }

}
