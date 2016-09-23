package io.electrum.billpaytestserver.handler;

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

import com.fasterxml.jackson.core.JsonProcessingException;

import io.electrum.billpay.api.IAccountLookupsResource;
import io.electrum.billpay.model.AccountLookupRequest;
import io.electrum.billpay.model.AccountLookupResponse;
import io.electrum.billpaytestserver.Utils;
import io.electrum.billpaytestserver.account.BillPayAccount;
import io.electrum.billpaytestserver.engine.ErrorDetailFactory;
import io.electrum.vas.model.Amounts;

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
      try {
         handleMessage(
               id,
               accountLookupRequest,
               securityContext,
               asyncResponse,
               request,
               httpServletRequest,
               httpHeaders,
               uriInfo);
      } catch (Exception e) {
         log.error("Error handling message", e);
         asyncResponse.resume(ErrorDetailFactory.getServerErrorErrorDetail(e));
      }
   }

   protected AccountLookupResponse getResponse(AccountLookupRequest request, BillPayAccount account) {
      log.info("Constructing response");
      AccountLookupResponse response = new AccountLookupResponse();

      response.setId(request.getId());
      response.setTime(new DateTime());
      response.setOriginator(request.getOriginator());
      response.setClient(getClient());
      response.setSettlementEntity(getSettlementEntity());
      response.setReceiver(getReceiver());
      response.setAccount(getAccount(account));
      response.setAmounts(new Amounts().balanceAmount(account.getBalance()));
      response.setCustomer(account.getCustomer());
      response.setSlipData(getSlipData());
      response.setThirdPartyIdentifiers(getThirdPartyIdentifiers(request.getThirdPartyIdentifiers()));

      try {
         log.debug(Utils.objectToPrettyPrintedJson(response));
      } catch (JsonProcessingException e) {
         log.error("Could not print response");
      }

      return response;
   }

   @Override
   protected void doReversal(AccountLookupRequest origRequest) {

   }

   @Override
   protected void doConfirm(AccountLookupRequest origRequest) {

   }

}
