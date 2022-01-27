package se.sensera.banking.Implementation;

import se.sensera.banking.*;
import se.sensera.banking.exceptions.Activity;
import se.sensera.banking.exceptions.UseException;
import se.sensera.banking.exceptions.UseExceptionType;


import java.util.function.Consumer;

import java.util.stream.Stream;



public class AccountServiceImpl implements AccountService {

    final UsersRepository usersRepository;
    final AccountsRepository accountsRepository;

    public AccountServiceImpl(UsersRepository usersRepository, AccountsRepository accountsRepository) {
        this.usersRepository = usersRepository;
        this.accountsRepository = accountsRepository;
    }

    @Override
    public Account createAccount(String userId, String accountName) throws UseException {
        if (usersRepository.getEntityById(userId).isEmpty()) {
            throw new UseException(Activity.CREATE_ACCOUNT, UseExceptionType.USER_NOT_FOUND, "User not found");
        } else if (accountsRepository.all().anyMatch(x -> x.getName().equals(accountName))) {
            throw new UseException(Activity.CREATE_ACCOUNT, UseExceptionType.ACCOUNT_NAME_NOT_UNIQUE, "Not unique name");
        }
        User user = usersRepository.getEntityById(userId).get();
        AccountImpl account = new AccountImpl(user, accountName);
        return accountsRepository.save(account);
    }

    @Override
    public Account changeAccount(String userId, String accountId, Consumer<ChangeAccount> changeAccountConsumer) throws UseException {
        User updatedUser = usersRepository.getEntityById(userId).get();
        Account accountUser = accountsRepository.getEntityById(accountId).get();

        changeAccountConsumer.accept(new ChangeAccount() {
            @Override
            public void setName(String name) throws UseException {
                // En användare får inte byta namn ifall hen inte är ägare till kontot.

                if (accountUser.getId().isEmpty()) { // Det finns en användare.
                } else if (name.equals(accountUser.getName())) {
                    updatedUser.setName(name);
                    usersRepository.save(updatedUser); //Sparar användaren i user
                } else {
                    if (accountsRepository.all().anyMatch(x -> x.getName().equals(name))) {
                        throw new UseException(Activity.UPDATE_ACCOUNT, UseExceptionType.ACCOUNT_NAME_NOT_UNIQUE, "Account name not unique");
                    }
                    accountUser.setName(name);
                    accountsRepository.save(accountUser);
                }
                /*if (accountsRepository.all().noneMatch(x -> x.getOwner().equals(x.getUsers()))) {
                    throw new UseException(Activity.UPDATE_ACCOUNT, UseExceptionType.NOT_FOUND, "BAJS");
                }*/

                accountUser.setName(name);
            }
        });
        return accountUser;
    }

    @Override
    public Account addUserToAccount(String userId, String accountId, String userIdToBeAssigned) throws UseException {
        return null;
    }

    @Override
    public Account removeUserFromAccount(String userId, String accountId, String userIdToBeAssigned) throws UseException {
        return null;
    }

    @Override
    public Account inactivateAccount(String userId, String accountId) throws UseException {
        Account account;
        if(accountsRepository.getEntityById(accountId).isEmpty()) {
            throw new UseException(Activity.INACTIVATE_ACCOUNT, UseExceptionType.NOT_FOUND, "account not found");
        }else {
             account = accountsRepository.getEntityById(accountId).get();
             account.setActive(false);
             accountsRepository.save(account);
        }
            return account;
    }

    @Override
    public Stream<Account> findAccounts(String searchValue, String userId, Integer pageNumber, Integer pageSize, SortOrder sortOrder) throws UseException {
        return null;
    }
}
