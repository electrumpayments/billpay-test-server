package io.electrum.billpaytestserver.account;

import io.electrum.billpay.model.Customer;
import io.electrum.vas.model.LedgerAmount;

/**
 *
 */
public class BillPayAccount {

   private String accountRef;
   private LedgerAmount balance;
   private Customer customer;

   public BillPayAccount() {
   }

   public BillPayAccount(String accountRef, LedgerAmount balance, Customer customer) {
      this.accountRef = accountRef;
      this.balance = balance;
      this.customer = customer;
   }

   public String getAccountRef() {
      return accountRef;
   }

   public void setAccountRef(String accountRef) {
      this.accountRef = accountRef;
   }

   public LedgerAmount getBalance() {
      return balance;
   }

   public void setBalance(LedgerAmount balance) {
      this.balance = balance;
   }

   public Customer getCustomer() {
      return customer;
   }

   public void setCustomer(Customer customer) {
      this.customer = customer;
   }

   public void setCustomer(String firstName, String lastName, String address, String idNumber, String contactNumber) {
      Customer customer = new Customer();
      customer.setFirstName(firstName);
      customer.setLastName(lastName);
      customer.setAddress(address);
      customer.setIdNumber(idNumber);
      customer.setContactNumber(contactNumber);

      this.customer = customer;
   }
}
