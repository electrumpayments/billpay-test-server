package com.electrum.billpaytestserver.handler;

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
import com.electrum.billpaytestserver.engine.ErrorDetailFactory;
import com.electrum.billpaytestserver.engine.MockBillPayBackend;
import com.fasterxml.jackson.core.JsonProcessingException;

import io.electrum.billpay.api.IPaymentsResource;
import io.electrum.billpay.model.PaymentRequest;
import io.electrum.billpay.model.PaymentResponse;
import io.electrum.vas.model.Amounts;
import io.electrum.vas.model.BasicReversal;
import io.electrum.vas.model.LedgerAmount;
import io.electrum.vas.model.TenderAdvice;

/**
 *
 */
public class PaymentResourceHandler extends BaseRequestHandler<PaymentRequest, PaymentResponse>
      implements IPaymentsResource {
   private static final Logger log = LoggerFactory.getLogger(PaymentResourceHandler.class);

   @Override
   public void confirmPayment(
         UUID adviceId,
         UUID paymentId,
         TenderAdvice tenderAdvice,
         SecurityContext securityContext,
         AsyncResponse asyncResponse,
         Request request,
         HttpServletRequest httpServletRequest,
         HttpHeaders httpHeaders,
         UriInfo uriInfo) {
      log.info("Handling payment confirm");
      try {
         handleConfirm(
               adviceId,
               paymentId,
               tenderAdvice,
               securityContext,
               asyncResponse,
               request,
               httpServletRequest,
               httpHeaders,
               uriInfo,
               true);
      } catch (Exception e) {
         log.error("Error handling message", e);
         asyncResponse.resume(ErrorDetailFactory.getServerErrorErrorDetail(e));
      }
   }

   @Override
   public void createPayment(
         UUID uuid,
         PaymentRequest paymentRequest,
         SecurityContext securityContext,
         AsyncResponse asyncResponse,
         Request request,
         HttpServletRequest httpServletRequest,
         HttpHeaders httpHeaders,
         UriInfo uriInfo) {
      log.info("Handling payment request");
      try {
         handleMessage(
               uuid,
               paymentRequest,
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

   @Override
   public void reversePayment(
         UUID adviceId,
         UUID paymentId,
         BasicReversal paymentReversal,
         SecurityContext securityContext,
         AsyncResponse asyncResponse,
         Request request,
         HttpServletRequest httpServletRequest,
         HttpHeaders httpHeaders,
         UriInfo uriInfo) {
      log.info("Handling payment reversal");
      try {
         handleReversal(
               adviceId,
               paymentId,
               paymentReversal,
               securityContext,
               asyncResponse,
               request,
               httpServletRequest,
               httpHeaders,
               uriInfo,
               true);
      } catch (Exception e) {
         log.error("Error handling message", e);
         asyncResponse.resume(ErrorDetailFactory.getServerErrorErrorDetail(e));
      }
   }

   protected void doConfirm(PaymentRequest request) {
      BillPayAccount account = MockBillPayBackend.getAccount(request.getAccountRef());

      LedgerAmount ledgerAmount = account.getBalance();

      long amount = ledgerAmount.getAmount();

      amount -= request.getAmounts().getRequestAmount().getAmount();

      ledgerAmount.setAmount(amount);
   }

   protected void doReversal(PaymentRequest request) {
   }

   protected PaymentResponse getResponse(PaymentRequest request, BillPayAccount account) {
      log.info("Constructing response");
      PaymentResponse response = new PaymentResponse();

      response.setId(request.getId());
      response.setTime(new DateTime());
      response.setOriginator(request.getOriginator());
      response.setClient(getClient());
      response.setSettlementEntity(getSettlementEntity());
      response.setReceiver(getReceiver());
      response.setAccount(getAccount(account));
      response.setCustomer(account.getCustomer());
      response.setSlipData(getSlipData());
      Amounts reqAmounts = request.getAmounts();
      LedgerAmount requestedAmount = reqAmounts.getRequestAmount();
      Amounts rspAmounts =
            new Amounts().requestAmount(requestedAmount)
                  .approvedAmount(requestedAmount)
                  .balanceAmount(account.getBalance());
      response.setAmounts(rspAmounts);
      response.setThirdPartyIdentifiers(getThirdPartyIdentifiers(request.getThirdPartyIdentifiers()));

      try {
         log.debug(Utils.objectToPrettyPrintedJson(response));
      } catch (JsonProcessingException e) {
         log.error("Could not print response");
      }

      MockBillPayBackend.add(response);

      return response;
   }

}
