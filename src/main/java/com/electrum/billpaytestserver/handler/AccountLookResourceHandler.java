package com.electrum.billpaytestserver.handler;

import io.electrum.billpay.api.IAccountLookupsResource;
import io.electrum.billpay.model.AccountLookupRequest;
import io.electrum.billpay.model.AccountLookupResponse;

import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Request;
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
public class AccountLookResourceHandler extends BaseRequestHandler<AccountLookupRequest, AccountLookupResponse>
      implements IAccountLookupsResource {
   private static final Logger log = LoggerFactory.getLogger(AccountLookResourceHandler.class);

   @Override
   public void requestAccountInfo(
         UUID id,
         AccountLookupRequest accountLookupRequest,
         SecurityContext securityContext,
         AsyncResponse asyncResponse,
         Request request,
         HttpServletRequest httpServletRequest,
         HttpHeaders httpHeaders,
         UriInfo uriInfo) {
      log.info("Handling account lookup request");
      
       handleMessage(id, accountLookupRequest, securityContext, asyncResponse, request, httpServletRequest, httpHeaders, uriInfo);
   }

   protected AccountLookupResponse getResponse(AccountLookupRequest request, BillPayAccount account) {
      log.info("Constructing response");
      AccountLookupResponse response = new AccountLookupResponse();

      response.setId(request.getId());
      response.setTime(new DateTime());
      response.setSender(request.getSender());
      // response.setLinkData();
      response.setProcessor(getProcessor());
      response.setReceiver(getReceiver());
      response.setAccount(getAccount(account));
      response.setCustomer(account.getCustomer());
      response.setSlipData(getSlipData());

      try {
         log.debug(Utils.objectToPrettyPrintedJson(response));
      } catch (JsonProcessingException e) {
         log.error("Could not print response");
      }

      return response;
   }

}
