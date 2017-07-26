package com.electrum.billpaytestserver.handler;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.slf4j.Logger;

import com.electrum.billpaytestserver.Utils;
import com.electrum.billpaytestserver.account.BillPayAccount;
import com.fasterxml.jackson.core.JsonProcessingException;

import io.electrum.billpay.model.Account;
import io.electrum.billpay.model.BillSlipData;
import io.electrum.billpay.model.Policy;
import io.electrum.billpay.model.TrafficFine;
import io.electrum.vas.model.Institution;
import io.electrum.vas.model.SlipLine;
import io.electrum.vas.model.ThirdPartyIdentifier;
import io.electrum.vas.model.Transaction;

/**
 * This exists because the API was updated to include new operations in AccountLookupsResource and PaymentsResource.
 * These methods are not compatible with the parameterised types in BaseRequestHandler, so a new base class was
 * required. RefundResourceHandler still works with the old base class, but has not yet been updated due to time
 * constraints at time of writing.
 */
public class AnotherBaseRequestHandler {

   protected Institution getClient() {
      Institution institution = new Institution();
      institution.setId("ClientId");
      institution.setName("Client Name");

      return institution;
   }

   protected Institution getSettlementEntity() {
      Institution institution = new Institution();
      institution.setId("SettlementEntityId");
      institution.setName("Settlement Entity");

      return institution;
   }

   protected Institution getReceiver() {
      Institution institution = new Institution();
      institution.setId("ReceiverId");
      institution.setName("Receiver Name");

      return institution;
   }

   protected List<ThirdPartyIdentifier> getThirdPartyIdentifiers(
         List<ThirdPartyIdentifier> thirdPartyIdentifiersFromRequest) {
      List<ThirdPartyIdentifier> thirdPartyIdentifiers = new ArrayList<>();

      for (ThirdPartyIdentifier thirdPartyIdentifier : thirdPartyIdentifiersFromRequest) {
         thirdPartyIdentifiers.add(thirdPartyIdentifier);
      }

      ThirdPartyIdentifier settlement = new ThirdPartyIdentifier();
      settlement.setInstitutionId("234652");
      settlement.setTransactionIdentifier("settlementEntityRef");

      ThirdPartyIdentifier receiver = new ThirdPartyIdentifier();
      receiver.setInstitutionId("803485");
      receiver.setTransactionIdentifier("receiverRef");

      thirdPartyIdentifiers.add(settlement);
      thirdPartyIdentifiers.add(receiver);

      return thirdPartyIdentifiers;
   }

   protected Account getAccount(BillPayAccount bpAccount) {
      Account account = new Account();

      account.setAccountRef(bpAccount.getAccountRef());
      // account.setDueDate();
      return account;
   }

   protected TrafficFine getTrafficFine(BillPayAccount bpAccount) {
      TrafficFine trafficFine = new TrafficFine();

      trafficFine.setNoticeNumber(bpAccount.getAccountRef());

      return trafficFine;
   }

   protected Policy getPolicy(BillPayAccount bpAccount) {
      Policy policy = new Policy();

      policy.setPolicyNumber(bpAccount.getAccountRef());

      return policy;
   }

   protected BillSlipData getSlipData() {
      BillSlipData slipData = new BillSlipData();
      slipData.setIssuerReference(Utils.generateIssuerReferenceNumber());
      slipData.setPhoneNumber("PhoneNumber");
      List<SlipLine> lines = new ArrayList<SlipLine>();
      lines.add(new SlipLine().fontHeightScaleFactor(2).text("Double height"));
      lines.add(new SlipLine().fontWidthScaleFactor(2).text("Double width"));
      lines.add(new SlipLine().line(true));
      lines.add(new SlipLine().text("Some text goes here"));
      lines.add(new SlipLine().cut(true));
      slipData.setMessageLines(lines);

      return slipData;
   }

   protected void setBasicResponseFields(Transaction request, Transaction response) {
      response.setId(request.getId());
      response.setTime(new DateTime());
      response.setOriginator(request.getOriginator());
      response.setClient(getClient());
      response.setSettlementEntity(getSettlementEntity());
      response.setReceiver(getReceiver());
      response.setSlipData(getSlipData());
      response.setThirdPartyIdentifiers(getThirdPartyIdentifiers(request.getThirdPartyIdentifiers()));
   }

   protected <T extends Transaction> void logRequestOrResponse(T requestOrResponse, Logger log) {
      try {
         log.debug(Utils.objectToPrettyPrintedJson(requestOrResponse));
      } catch (JsonProcessingException e) {
         log.error("Could not print request or response message");
      }
   }
}
