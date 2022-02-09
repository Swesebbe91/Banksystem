package se.sensera.banking.Implementation;

import se.sensera.banking.Account;
import se.sensera.banking.Transaction;
import se.sensera.banking.User;

import java.util.ArrayList;
import java.util.Date;

public class Factory {

     public User createUserObject(String id, String name, String personalIdentificationNumber, boolean active) {
         return new UserImpl(id, name, personalIdentificationNumber, active);
     }

     public Account createAccountObject(String id, User owner, String name,  boolean active) {
         return new AccountImpl(id, owner, name, active);
     }

     public Transaction createTransactionObject(String id, String created, User user, Account account, double amount) {
         return new TransactionImpl(id, created, user, account, amount);
     }
}
