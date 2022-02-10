package se.sensera.banking.Implementation;

import se.sensera.banking.*;
import se.sensera.banking.exceptions.UseException;
import java.util.UUID;
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

        Account account = getAccount(accountId);
        User user = getUser(userId);
        return transactionsRepository.save(factory.createTransactionObject(UUID.randomUUID().toString(), timeStamp, user, account, amount));
        /*if (!account.getUsers().anyMatch(x -> x.getId().equals(userId)) || !account.getOwner().getId().equals(userId)) { //TEST 2 && TEST 3
                throw new UseException(Activity.CREATE_TRANSACTION, UseExceptionType.NOT_ALLOWED);
        }

         /*Transaction oldTrans = transactionsRepository.all().filter(x -> x.getAccount().equals(accountId)).findFirst().get();
             if (oldTrans.getAmount() < amount) {
            throw new UseException(Activity.CREATE_TRANSACTION, UseExceptionType.NOT_FUNDED);
        }
        */

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

    public User getUser(String userId) {
        return usersRepository.getEntityById(userId).get();
    }
}

