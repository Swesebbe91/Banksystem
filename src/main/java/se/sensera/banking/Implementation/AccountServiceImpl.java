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
        AccountImpl account = new AccountImpl(user, accountName, userId, true);
        return accountsRepository.save(account);
    }

    @Override
    public Account changeAccount(String userId, String accountId, Consumer<ChangeAccount> changeAccountConsumer) throws UseException {
        Account account = accountsRepository.getEntityById(accountId).get();
        User userAccount = usersRepository.getEntityById(userId).get();

        if (!account.getOwner().equals(userAccount)){
            throw new UseException(Activity.UPDATE_ACCOUNT, UseExceptionType.NOT_OWNER, "Not owner of account");
        }
        if (!account.isActive()){
            throw new UseException(Activity.UPDATE_ACCOUNT, UseExceptionType.NOT_ACTIVE, "Account is not active");
        }
        changeAccountConsumer.accept(new ChangeAccount() {
            @Override
            public void setName(String name) throws UseException {
                if (name.equals(account.getName())) { // Tar hand om samma namn - inget ska hända.
                } else {//Inga fel funna, byter namn.
                    if (accountsRepository.all().anyMatch(x -> x.getName().contains(name))) {
                        throw new UseException(Activity.UPDATE_ACCOUNT, UseExceptionType.ACCOUNT_NAME_NOT_UNIQUE, "Account name not unique");
                    }
                    account.setName(name);
                    accountsRepository.save(account);
                }
            }
        });
        return account;
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
        return null;
    }

    @Override
    public Stream<Account> findAccounts(String searchValue, String userId, Integer pageNumber, Integer pageSize, SortOrder sortOrder) throws UseException {
        return accountsRepository.all();
    }
}
