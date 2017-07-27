package com.electrum.billpaytestserver.handler;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.core.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.electrum.billpaytestserver.account.BillPayAccount;
import com.electrum.billpaytestserver.engine.ErrorDetailFactory;
import com.electrum.billpaytestserver.engine.MockBillPayBackend;
import com.electrum.billpaytestserver.validation.BillpayMessageValidator;
import com.electrum.billpaytestserver.validation.ValidationResult;

import io.electrum.billpay.api.IAccountLookupsResource;
import io.electrum.billpay.model.*;
import io.electrum.vas.model.Amounts;

/**
 *
 */
public class AccountLookResourceHandler extends AnotherBaseRequestHandler implements IAccountLookupsResource {
   private static final Logger log = LoggerFactory.getLogger(AccountLookResourceHandler.class);

   @Override
   public void requestAccountInfo(
         String id,
         AccountLookupRequest accountLookupRequest,
         SecurityContext securityContext,
         AsyncResponse asyncResponse,
         Request request,
         HttpServletRequest httpServletRequest,
         HttpHeaders httpHeaders,
         UriInfo uriInfo) {
      log.info("Handling account lookup request");
      try {
         handleMessage(accountLookupRequest, asyncResponse);
      } catch (Exception e) {
         log.error("Error handling message", e);
         asyncResponse.resume(
               ErrorDetailFactory.getServerErrorErrorDetail(
                     e,
                     ErrorDetail.RequestType.ACCOUNT_LOOKUP_REQUEST,
                     accountLookupRequest.getId(),
                     null));
      }
   }

   @Override
   public void requestTrafficFineInfo(
         String id,
         TrafficFineLookupRequest trafficFineLookupRequest,
         SecurityContext securityContext,
         AsyncResponse asyncResponse,
         Request request,
         HttpServletRequest httpServletRequest,
         HttpHeaders httpHeaders,
         UriInfo uriInfo) {
      log.info("Handling traffic fine info request");
      try {
         handleMessage(trafficFineLookupRequest, asyncResponse);
      } catch (Exception e) {
         log.error("Error handling message", e);
         asyncResponse.resume(ErrorDetailFactory.getServerErrorErrorDetail(
               e,
               ErrorDetail.RequestType.TRAFFIC_FINE_LOOKUP_REQUEST,
               trafficFineLookupRequest.getId(),
               null));
      }
   }

   @Override
   public void requestPolicyInfo(
         String id,
         PolicyLookupRequest policyLookupRequest,
         SecurityContext securityContext,
         AsyncResponse asyncResponse,
         Request request,
         HttpServletRequest httpServletRequest,
         HttpHeaders httpHeaders,
         UriInfo uriInfo) {
      log.info("Handling traffic fine info request");
      try {
         handleMessage(policyLookupRequest, asyncResponse);
      } catch (Exception e) {
         log.error("Error handling message", e);
         asyncResponse.resume(
               ErrorDetailFactory.getServerErrorErrorDetail(
                     e,
                     ErrorDetail.RequestType.TRAFFIC_FINE_LOOKUP_REQUEST,
                     policyLookupRequest.getId(),
                     null));
      }
   }

   protected AccountLookupResponse getResponse(AccountLookupRequest request, BillPayAccount account) {
      log.info("Constructing response");
      AccountLookupResponse response = new AccountLookupResponse();

      setBasicResponseFields(request, response);

      response.setAccount(getAccount(account));
      response.setAmounts(new Amounts().balanceAmount(account.getBalance()));
      response.setCustomer(account.getCustomer());

      logRequestOrResponse(response, log);

      return response;
   }

   protected TrafficFineLookupResponse getResponse(TrafficFineLookupRequest request, BillPayAccount account) {
      log.info("Constructing response");
      TrafficFineLookupResponse response = new TrafficFineLookupResponse();

      setBasicResponseFields(request, response);

      response.setTrafficFine(getTrafficFine(account));
      response.setAmounts(new Amounts().balanceAmount(account.getBalance()));
      response.setCustomer(account.getCustomer());

      logRequestOrResponse(response, log);

      return response;
   }

   protected PolicyLookupResponse getResponse(PolicyLookupRequest request, BillPayAccount account) {
      log.info("Constructing response");
      PolicyLookupResponse response = new PolicyLookupResponse();

      setBasicResponseFields(request, response);

      response.setPolicy(getPolicy(account));
      response.setAmounts(new Amounts().balanceAmount(account.getBalance()));
      response.setCustomer(account.getCustomer());

      logRequestOrResponse(response, log);

      return response;
   }

   protected void handleMessage(AccountLookupRequest request, AsyncResponse asyncResponse) throws Exception {

      if (!validateAndPersist(request, asyncResponse)) {
         return;
      }

      BillPayAccount account;

      account = MockBillPayBackend.getAccount((request).getAccountRef());

      if (account == null) {
         asyncResponse.resume(
               ErrorDetailFactory.getNoAccountFoundErrorDetail(
                     (request).getAccountRef(),
                     ErrorDetail.RequestType.ACCOUNT_LOOKUP_REQUEST,
                     request.getId(),
                     null));
         return;
      }

      asyncResponse.resume(Response.status(Response.Status.OK).entity(getResponse(request, account)).build());
   }

   protected void handleMessage(TrafficFineLookupRequest request, AsyncResponse asyncResponse) throws Exception {

      if (!validateAndPersist(request, asyncResponse)) {
         return;
      }

      BillPayAccount account;

      account = MockBillPayBackend.getAccount((request).getNoticeNumber());

      if (account == null) {
         asyncResponse.resume(
               ErrorDetailFactory.getNoAccountFoundErrorDetail(
                     (request).getNoticeNumber(),
                     ErrorDetail.RequestType.TRAFFIC_FINE_LOOKUP_REQUEST,
                     request.getId(),
                     null));
         return;
      }

      asyncResponse.resume(Response.status(Response.Status.OK).entity(getResponse(request, account)).build());
   }

   protected void handleMessage(PolicyLookupRequest request, AsyncResponse asyncResponse) throws Exception {

      if (!validateAndPersist(request, asyncResponse)) {
         return;
      }

      BillPayAccount account = null;

      account = MockBillPayBackend.getAccount((request).getPolicyNumber());

      if (account == null) {
         asyncResponse.resume(
               ErrorDetailFactory.getNoAccountFoundErrorDetail(
                     (request).getPolicyNumber(),
                     ErrorDetail.RequestType.POLICY_LOOKUP_REQUEST,
                     request.getId(),
                     null));
         return;
      }

      asyncResponse.resume(Response.status(Response.Status.OK).entity(getResponse(request, account)).build());
   }

   protected boolean validateAndPersist(AccountLookupRequest request, AsyncResponse asyncResponse) {
      ValidationResult validation = BillpayMessageValidator.validate(request);

      ErrorDetail.RequestType requestType = ErrorDetail.RequestType.ACCOUNT_LOOKUP_REQUEST;

      if (!validation.isValid()) {
         log.info("Request format invalid");
         asyncResponse.resume(
               ErrorDetailFactory.getIllFormattedMessageErrorDetail(validation, requestType, request.getId(), null));
         return false;
      }

      logRequestOrResponse(request, log);

      boolean wasAdded = MockBillPayBackend.add(request);

      if (!wasAdded) {
         asyncResponse.resume(
               ErrorDetailFactory.getNotUniqueUuidErrorDetail(request.getId(), requestType, request.getId(), null));
         return false;
      }
      return true;
   }

   protected boolean validateAndPersist(TrafficFineLookupRequest request, AsyncResponse asyncResponse) {
      ValidationResult validation = BillpayMessageValidator.validate(request);

      ErrorDetail.RequestType requestType = ErrorDetail.RequestType.TRAFFIC_FINE_LOOKUP_REQUEST;

      if (!validation.isValid()) {
         log.info("Request format invalid");
         asyncResponse.resume(
               ErrorDetailFactory.getIllFormattedMessageErrorDetail(validation, requestType, request.getId(), null));
         return false;
      }

      logRequestOrResponse(request, log);

      boolean wasAdded = MockBillPayBackend.add(request);

      if (!wasAdded) {
         asyncResponse.resume(
               ErrorDetailFactory.getNotUniqueUuidErrorDetail(request.getId(), requestType, request.getId(), null));
         return false;
      }
      return true;
   }

   protected boolean validateAndPersist(PolicyLookupRequest request, AsyncResponse asyncResponse) {
      ValidationResult validation = BillpayMessageValidator.validate(request);

      ErrorDetail.RequestType requestType = ErrorDetail.RequestType.POLICY_LOOKUP_REQUEST;

      if (!validation.isValid()) {
         log.info("Request format invalid");
         asyncResponse.resume(
               ErrorDetailFactory.getIllFormattedMessageErrorDetail(validation, requestType, request.getId(), null));
         return false;
      }

      logRequestOrResponse(request, log);

      boolean wasAdded = MockBillPayBackend.add(request);

      if (!wasAdded) {
         asyncResponse.resume(
               ErrorDetailFactory.getNotUniqueUuidErrorDetail(request.getId(), requestType, request.getId(), null));
         return false;
      }
      return true;
   }
}
