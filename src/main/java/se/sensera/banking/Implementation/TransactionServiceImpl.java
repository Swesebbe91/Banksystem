package se.sensera.banking.Implementation;

import se.sensera.banking.*;
import se.sensera.banking.exceptions.Activity;
import se.sensera.banking.exceptions.UseException;
import se.sensera.banking.exceptions.UseExceptionType;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;


public class TransactionServiceImpl implements TransactionService {

    UsersRepository usersRepository;
    AccountsRepository accountsRepository;
    TransactionsRepository transactionsRepository;
    Factory factory = new Factory();
    UserServiceImpl userService;
    AccountServiceImpl accountService;

    public TransactionServiceImpl(UsersRepository usersRepository, AccountsRepository accountsRepository, TransactionsRepository transactionsRepository) {
        this.usersRepository = usersRepository;
        this.accountsRepository = accountsRepository;
        this.transactionsRepository = transactionsRepository;
    }

    @Override
    public Transaction createTransaction(String timeStamp, String userId, String accountId, double amount) throws UseException {
        Account account = getAccount(accountId);
        if (!account.getUsers().anyMatch(x -> x.getId().equals(userId)) && !account.getOwner().getId().equals(userId)) {
            throw new UseException(Activity.CREATE_TRANSACTION, UseExceptionType.NOT_ALLOWED);
        }
        return transactionsRepository.save(factory.createTransactionObject(userId, timeStamp, usersRepository.getEntityById(userId).get(), accountsRepository.getEntityById(accountId).get(), amount));
    }

    @Override
    public double sum (String created, String userId, String accountId) throws UseException {
        return 0;
    }

    @Override
    public void addMonitor (Consumer < Transaction > monitor) {

    }

    public Account getAccount(String accountId) {
        return accountsRepository.getEntityById(accountId).get();
    }
}

