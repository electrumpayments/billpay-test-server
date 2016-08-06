package com.electrum.billpaytestserver.handler;

import io.electrum.billpay.api.IRefundsResource;
import io.electrum.billpay.model.RefundRequest;
import io.electrum.billpay.model.RefundResponse;
import io.electrum.billpay.model.RefundReversal;
import io.electrum.vas.model.BasicAdvice;

import java.util.UUID;

import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.electrum.billpaytestserver.Utils;
import com.electrum.billpaytestserver.account.BillPayAccount;
import com.fasterxml.jackson.core.JsonProcessingException;

/**
 *
 */
public class RefundResourceHandler extends
      BaseDualRequestHandler<RefundRequest, RefundResponse, BasicAdvice, RefundReversal> implements IRefundsResource {
   private static final Logger log = LoggerFactory.getLogger(RefundResourceHandler.class);

   @Override
   public Response confirmRefund(
         UUID uuid,
         UUID uuid1,
         BasicAdvice basicAdvice,
         SecurityContext securityContext,
         AsyncResponse asyncResponse,
         HttpHeaders httpHeaders,
         UriInfo uriInfo) {
      return null;
   }

   @Override
   public Response createRefund(
         UUID uuid,
         RefundRequest refundRequest,
         SecurityContext securityContext,
         AsyncResponse asyncResponse,
         HttpHeaders httpHeaders,
         UriInfo uriInfo) {
      log.info("Handling refund request");
      return handleMessage(uuid.toString(), refundRequest, securityContext, asyncResponse, httpHeaders, uriInfo);
   }

   @Override
   public Response reverseRefund(
         UUID uuid,
         UUID uuid1,
         RefundReversal refundReversal,
         SecurityContext securityContext,
         AsyncResponse asyncResponse,
         HttpHeaders httpHeaders,
         UriInfo uriInfo) {
      return null;
   }

   protected RefundResponse getResponse(RefundRequest request, BillPayAccount account) {
      log.info("Constructing response");
      RefundResponse response = new RefundResponse();

      response.setId(request.getId());
      response.setTime(new DateTime());
      response.setSender(request.getSender());
      // response.setLinkData();
      response.setProcessor(getProcessor());
      response.setReceiver(getReceiver());
      response.setAccount(getAccount(account));
      response.setCustomer(account.getCustomer());

      // add response amount

      try {
         log.debug(Utils.objectToPrettyPrintedJson(response));
      } catch (JsonProcessingException e) {
         log.error("Could not print response");
      }

      return response;
   }

}
