package se.sensera.banking.Implementation;

import se.sensera.banking.*;
import se.sensera.banking.exceptions.UseException;
import java.util.function.Consumer;


public class TransactionServiceImpl implements TransactionService {

    UsersRepository usersRepository;
    AccountsRepository accountsRepository;
    TransactionsRepository transactionsRepository;
    Factory factory = new Factory();

    public TransactionServiceImpl(UsersRepository usersRepository, AccountsRepository accountsRepository, TransactionsRepository transactionsRepository) {
        this.usersRepository = usersRepository;
        this.accountsRepository = accountsRepository;
        this.transactionsRepository = transactionsRepository;
    }

    @Override
    public Transaction createTransaction(String timeStamp, String userId, String accountId, double amount) throws UseException {
        return transactionsRepository.save(factory.createTransactionObject(userId, timeStamp, usersRepository.getEntityById(userId).get(), accountsRepository.getEntityById(accountId).get(), amount));
    }

    @Override
    public double sum(String created, String userId, String accountId) throws UseException {
        return 0;
    }

    @Override
    public void addMonitor(Consumer<Transaction> monitor) {

    }
}
