package com.electrum.billpaytestserver.account;

import io.electrum.billpay.model.Customer;
import io.electrum.vas.model.LedgerAmount;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.csvreader.CsvReader;
import com.electrum.billpaytestserver.BillpayTestServer;

/**
 *
 */
public class AccountLoader {
   private static final Logger log = LoggerFactory.getLogger(BillpayTestServer.class);
   private final String accountsFileName = "accounts.csv";
   private HashMap<String, BillPayAccount> mapToLoad;

   public AccountLoader(HashMap<String, BillPayAccount> mapToLoad) {
      this.mapToLoad = mapToLoad;
   }

   public HashMap<String, BillPayAccount> getMapToLoad() {
      return mapToLoad;
   }

   public void setMapToLoad(HashMap<String, BillPayAccount> mapToLoad) {
      this.mapToLoad = mapToLoad;
   }

   public void loadAccounts() throws IOException {
      log.info("Loading accounts map from file: {}", accountsFileName);

      ClassLoader classloader = Thread.currentThread().getContextClassLoader();
      InputStream is = classloader.getResourceAsStream(accountsFileName);

      CsvReader csv = new CsvReader(is, Charset.forName("UTF-8"));

      BillPayAccount account;

      try {
         while (csv.readRecord()) {
            account = new BillPayAccount();
            Customer customer = new Customer();

            account.setAccountRef(csv.get(0).replaceAll("\\s+", ""));

            customer.setFirstName(csv.get(1).replaceAll("\\s+", ""));
            customer.setLastName(csv.get(2).replaceAll("\\s+", ""));
            customer.setIdNumber(csv.get(3).replaceAll("\\s+", ""));
            customer.setContactNumber(csv.get(4).replaceAll("\\s+", ""));
            customer.setAddress(csv.get(5));

            LedgerAmount amount = new LedgerAmount();
            amount.setAmount(Long.parseLong(csv.get(6).replaceAll("\\s+", "")));
            amount.setCurrency("ZAR");

            account.setBalance(amount);
            account.setCustomer(customer);

            mapToLoad.put(account.getAccountRef(), account);

         }
      } catch (Exception e) {
         log.error("Error parsing accounts file. Possibly incorrect format", e);
         throw e;
      }

      try {
         is.close();
         csv.close();
      } catch (IOException e) {
         log.error("Error closing the accounts file.", e);
      }

      log.info("Accounts loaded successfully from file. Entries: {}", mapToLoad.size());
   }

}
