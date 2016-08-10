package com.electrum.billpaytestserver.handler;

import io.electrum.vas.model.BasicAdvice;
import io.electrum.vas.model.BasicRequest;
import io.electrum.vas.model.BasicResponse;
import io.electrum.vas.model.BasicReversal;

import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.electrum.billpaytestserver.Utils;
import com.electrum.billpaytestserver.engine.ErrorDetailFactory;
import com.electrum.billpaytestserver.engine.MockBillPayBackend;
import com.electrum.billpaytestserver.validation.BillpayMessageValidator;
import com.electrum.billpaytestserver.validation.ValidationResult;
import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * T is request type U is response type
 */
public abstract class BaseDualRequestHandler<W extends BasicRequest, X extends BasicResponse, Y extends BasicAdvice, Z extends BasicReversal>
      extends BaseRequestHandler<W, X> {
   private static final Logger log = LoggerFactory.getLogger(BaseDualRequestHandler.class);

   protected void handleConfirm(
         UUID adviceId,
         UUID requestId,
         Y advice,
         SecurityContext securityContext,
         AsyncResponse asyncResponse,
         Request request,
         HttpServletRequest httpServletRequest,
         HttpHeaders httpHeaders,
         UriInfo uriInfo) {

      if (!validateAndPersist(advice, asyncResponse)) {
         return;
      }

      W origRequest = (W) MockBillPayBackend.getRequest(requestId);

      if (origRequest == null) {
         asyncResponse.resume(ErrorDetailFactory.getNoPrecedingRequestFoundErrorDetail(requestId));
         return;
      }

      doConfirm (origRequest);
      
      asyncResponse.resume(Response.status(Response.Status.ACCEPTED).build());
   }

   protected void handleReversal(
         UUID adviceId,
         UUID requestId,
         Z reversal,
         SecurityContext securityContext,
         AsyncResponse asyncResponse,
         Request request,
         HttpServletRequest httpServletRequest,
         HttpHeaders httpHeaders,
         UriInfo uriInfo) {

      if (!validateAndPersist(reversal, asyncResponse)) {
         return;
      }

      if (!validateAndPersist(reversal, asyncResponse)) {
         return;
      }

      W origRequest = (W) MockBillPayBackend.getRequest(requestId);

      if (origRequest == null) {
         asyncResponse.resume(ErrorDetailFactory.getNoPrecedingRequestFoundErrorDetail(requestId));
         return;
      }

      doReversal(origRequest);

      asyncResponse.resume(Response.status(Response.Status.ACCEPTED).build());
   }

   protected abstract void doReversal(W origRequest);
   
   protected abstract void doConfirm(W origRequest);

   protected boolean validateAndPersist(Y advice, AsyncResponse asyncResponse) {
      ValidationResult validation = BillpayMessageValidator.validate(advice);

      if (!validation.isValid()) {
         log.info("Request format invalid");
         asyncResponse.resume(ErrorDetailFactory.getIllFormattedMessageErrorDetail(validation));
         return false;
      }

      try {
         log.debug(Utils.objectToPrettyPrintedJson(advice));
      } catch (JsonProcessingException e) {
         log.error("Could not print advice");
      }

      boolean wasAdded = MockBillPayBackend.add(advice);

      if (!wasAdded) {
         asyncResponse.resume(ErrorDetailFactory.getNotUniqueUuidErrorDetail());
         return false;
      }
      return true;
   }

   protected boolean validateAndPersist(Z reversal, AsyncResponse asyncResponse) {
      ValidationResult validation = BillpayMessageValidator.validate(reversal);

      if (!validation.isValid()) {
         log.info("Request format invalid");
         asyncResponse.resume(ErrorDetailFactory.getIllFormattedMessageErrorDetail(validation));
         return false;
      }

      try {
         log.debug(Utils.objectToPrettyPrintedJson(reversal));
      } catch (JsonProcessingException e) {
         log.error("Could not print advice");
      }

      boolean wasAdded = MockBillPayBackend.add(reversal);

      if (!wasAdded) {
         asyncResponse.resume(ErrorDetailFactory.getNotUniqueUuidErrorDetail());
         return false;
      }
      return true;
   }
}
