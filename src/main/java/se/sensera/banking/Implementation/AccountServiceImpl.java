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
        AccountImpl account = new AccountImpl(user, accountName, userId);
        return accountsRepository.save(account);
    }

    @Override
    public Account changeAccount(String userId, String accountId, Consumer<ChangeAccount> changeAccountConsumer) throws UseException {
        User updatedUser = usersRepository.getEntityById(userId).get();
        Account accountUser = accountsRepository.getEntityById(accountId).get();

        changeAccountConsumer.accept(new ChangeAccount() {
            @Override
            public void setName(String name) throws UseException {
                // TODO: En användare får inte byta namn ifall hen inte är ägare till kontot.
                // TODO: En användare får inte byta namn för att kontot är inaktivt.
                if (accountUser.getId().isEmpty()) { // Om det inte finns ett kontoid.
                } else if (name.equals(accountUser.getName())) { // Om namnet är samma som namnet på kontot
                    updatedUser.setName(name); // Uppdatera namnet på användaren.
                    usersRepository.save(updatedUser); //Sparar användarens nya namn
                } else { // Om namnet som skickats inte finns alls i kontorepot kastas ett undantag
                    if (accountsRepository.all().anyMatch(x -> x.getName().equals(name))) {
                        throw new UseException(Activity.UPDATE_ACCOUNT, UseExceptionType.ACCOUNT_NAME_NOT_UNIQUE, "Account name not unique");
                    } // I alla andra fall ska användaren kunna ändra sitt namn
                    accountUser.setName(name);
                    accountsRepository.save(accountUser); // spara nya kontonamnet.
                }
            }
        });
        return accountUser; // Returnera kontots ägare.
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
        User user;
        if (!accountId.equals(userId)) { // Såhär kollar vi om ägarskap över ett konto
            throw new UseException(Activity.INACTIVATE_ACCOUNT, UseExceptionType.NOT_OWNER, "Account not found");
        }
        else if (accountsRepository.getEntityById(accountId).isEmpty()) {
            throw new UseException(Activity.INACTIVATE_ACCOUNT, UseExceptionType.NOT_FOUND, "Account not found");
        }
        else if (usersRepository.getEntityById(userId).isEmpty()) {
            throw new UseException(Activity.INACTIVATE_ACCOUNT, UseExceptionType.USER_NOT_FOUND, "User not found");
        } else {
             account = accountsRepository.getEntityById(accountId).get();
             account.setActive(false);
             accountsRepository.save(account);
        }
        return account;
    }

    @Override
    public Stream<Account> findAccounts(String searchValue, String userId, Integer pageNumber, Integer pageSize, SortOrder sortOrder) throws UseException {
        return accountsRepository.all();
    }
}
