package se.sensera.banking.Implementation;

import se.sensera.banking.*;
import se.sensera.banking.exceptions.Activity;
import se.sensera.banking.exceptions.UseException;
import se.sensera.banking.exceptions.UseExceptionType;
import se.sensera.banking.utils.ListUtils;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class AccountServiceImpl implements AccountService {

    final UsersRepository usersRepository;
    final AccountsRepository accountsRepository;
    Factory factory = new Factory();

    public AccountServiceImpl(UsersRepository usersRepository, AccountsRepository accountsRepository) {
        this.usersRepository = usersRepository;
        this.accountsRepository = accountsRepository;
    }

    @Override
    public Account createAccount(String userId, String accountName) throws UseException {
        verifyBeforeCreateAccount(userId, accountName);
        return accountsRepository.save(factory.createAccountObject(usersRepository.getEntityById(userId).get(), accountName, userId, true)); // Factory pattern
    }

    @Override
    public Account changeAccount(String userId, String accountId, Consumer<ChangeAccount> changeAccountConsumer) throws UseException {
        Account account = accountsRepository.getEntityById(accountId).get();
        verifyBeforeChanging(userId, account);
        changeAccountConsumer.accept(name -> {
            if (name.equals(account.getName())) {
            } else {
                if (accountsRepository.all().anyMatch(x -> x.getName().contains(name))) {
                    throw new UseException(Activity.UPDATE_ACCOUNT, UseExceptionType.ACCOUNT_NAME_NOT_UNIQUE, "Account name not unique");
                }
                account.setName(name);
                accountsRepository.save(account);
            }
        });
        return account;
    }
    
    @Override
    public Account addUserToAccount(String userId, String accountId, String userIdToBeAssigned) throws UseException {
        verifyAccountExist(accountId);
        User newUser = usersRepository.getEntityById(userIdToBeAssigned).get();
        Account account = accountsRepository.getEntityById(accountId).get();
        verifyBeforeAdding(userId, userIdToBeAssigned, account);
        usersRepository.save(newUser);
        account.addUser(newUser);
        return accountsRepository.save(account);
    }

    @Override
    public Account removeUserFromAccount(String userId, String accountId, String userIdToBeAssigned) throws UseException {
        Account account = accountsRepository.getEntityById(accountId).get();
        User currentUser = usersRepository.getEntityById(userIdToBeAssigned).get();
        verifyBeforeRemoving(userId, userIdToBeAssigned, account);
        account.removeUser(currentUser);
        return accountsRepository.save(account);
    }

    @Override
    public Account inactivateAccount(String userId, String accountId) throws UseException {
        checkIfAccountAndUserEmpty(userId, accountId);
        Account account = accountsRepository.getEntityById(accountId).get();
        verifyBeforeInactivate(userId, account);
        account.setActive(false);
        return accountsRepository.save(account);
    }

    @Override
    public Stream<Account> findAccounts(String searchValue, String userId, Integer pageNumber, Integer pageSize, SortOrder sortOrder) throws UseException {
        Stream<Account> accountStream = accountsRepository.all();
            accountStream = getStreamBasedOnSearchValue(searchValue, accountStream);
            accountStream = getStreamAllAccountsToUser(userId, accountStream, () -> usersRepository.getEntityById(userId).get());
            accountStream = getStreamBasedOnAccountName(accountStream);
        return ListUtils.applyPage(accountStream, pageNumber, pageSize);
    }
    private void verifyBeforeInactivate(String userId, Account account) throws UseException {
        checkIfAccountOwner(account, userId, Activity.INACTIVATE_ACCOUNT, UseExceptionType.NOT_OWNER);
        checkIfAccountIsActive(account, Activity.INACTIVATE_ACCOUNT, UseExceptionType.NOT_ACTIVE);
    }

    private void verifyBeforeRemoving(String userId, String userIdToBeAssigned, Account account) throws UseException {
        checkIfAccountOwner(account, userId, Activity.UPDATE_ACCOUNT, UseExceptionType.NOT_OWNER);
        checkIfNotAssignedToAccount(account, userIdToBeAssigned, Activity.UPDATE_ACCOUNT, UseExceptionType.USER_NOT_ASSIGNED_TO_THIS_ACCOUNT);
    }

    private void verifyAccountExist(String accountId) throws UseException {
        if (accountsRepository.getEntityById(accountId).isEmpty())
            throw new UseException(Activity.UPDATE_ACCOUNT, UseExceptionType.NOT_FOUND, "Account does not exist");
    }

    private void verifyBeforeCreateAccount(String userId, String accountName) throws UseException {
        if (usersRepository.getEntityById(userId).isEmpty()) {
            throw new UseException(Activity.CREATE_ACCOUNT, UseExceptionType.USER_NOT_FOUND, "User not found");
        } else if (accountsRepository.all().anyMatch(x -> x.getName().equals(accountName))) {
            throw new UseException(Activity.CREATE_ACCOUNT, UseExceptionType.ACCOUNT_NAME_NOT_UNIQUE, "Not unique name");
        }
    }

    private void verifyBeforeChanging(String userId, Account account) throws UseException {
        checkIfAccountOwner(account, userId, Activity.UPDATE_ACCOUNT, UseExceptionType.NOT_OWNER);
        checkIfAccountIsActive(account, Activity.UPDATE_ACCOUNT, UseExceptionType.NOT_ACTIVE);
    }

    private void verifyBeforeAdding(String userId, String userIdToBeAssigned, Account account) throws UseException {
        checkIfAccountOwner(account, userId, Activity.UPDATE_ACCOUNT, UseExceptionType.NOT_OWNER);
        checkIfAccountIsActive(account, Activity.UPDATE_ACCOUNT, UseExceptionType.ACCOUNT_NOT_ACTIVE);
        checkOwnerIsNotUser(userIdToBeAssigned, account);
        checkIfAlreadyAssigned(account, userIdToBeAssigned, Activity.UPDATE_ACCOUNT, UseExceptionType.USER_ALREADY_ASSIGNED_TO_THIS_ACCOUNT);
    }

    private void checkIfAccountOwner(Account account, String userId, Activity activity, UseExceptionType type) throws UseException {
        if (!account.getOwner().getId().equals(userId))
            throw new UseException(Activity.valueOf(activity.name()), UseExceptionType.valueOf(type.name()), "Not owner of account");
    }

    private void checkIfAccountIsActive(Account account, Activity activity, UseExceptionType type) throws UseException {
        if (!account.isActive())
            throw new UseException(Activity.valueOf(activity.name()), UseExceptionType.valueOf(type.name()), "Account is not active");
    }

    private void checkOwnerIsNotUser(String userIdToBeAssigned, Account account) throws UseException {
        if (account.getOwner().getId().equals(userIdToBeAssigned))
            throw new UseException(Activity.UPDATE_ACCOUNT, UseExceptionType.CANNOT_ADD_OWNER_AS_USER, "Cannot add owner as user");
    }

    private void checkIfAlreadyAssigned(Account account, String userIdToBeAssigned, Activity activity, UseExceptionType type) throws UseException {
        if (account.getUsers().anyMatch(x -> x.getId().equals(userIdToBeAssigned)))
            throw new UseException(Activity.valueOf(activity.name()), UseExceptionType.valueOf(type.name()), "Already assigned");
    }

    private void checkIfNotAssignedToAccount(Account account, String userIdToBeAssigned, Activity activity, UseExceptionType type) throws UseException {
        if (account.getUsers().noneMatch(x -> x.getId().equals(userIdToBeAssigned)))
            throw new UseException(Activity.valueOf(activity.name()), UseExceptionType.valueOf(type.name()), "ARE YOU LOOOOST?");
    }

    private void checkIfAccountAndUserEmpty(String userId, String accountId) throws UseException {
        if (accountsRepository.getEntityById(accountId).isEmpty()) {
            throw new UseException(Activity.INACTIVATE_ACCOUNT, UseExceptionType.NOT_FOUND, "Account not found");
        } else if (usersRepository.getEntityById(userId).isEmpty()) {
            throw new UseException(Activity.INACTIVATE_ACCOUNT, UseExceptionType.USER_NOT_FOUND, "User not found");
        }
    }

    private Stream<Account> getStreamBasedOnAccountName(Stream<Account> accountStream) {
        return accountStream.sorted(Comparator.comparing(Account::getName));
    }

    private Stream<Account> getStreamAllAccountsToUser(String userId, Stream<Account> accountStream, Supplier<User> userSupplier) {
        if (userId != null) {
            User currentUser = userSupplier.get();
            accountStream = accountStream.filter(account -> account.getUsers().anyMatch(user -> user.getId().equals(userId)) || account.getOwner().equals(currentUser));
        }
        return accountStream;
    }

    private Stream<Account> getStreamBasedOnSearchValue(String searchValue, Stream<Account> accountStream) {
        return accountStream.filter(account -> account.getName().toLowerCase().contains(searchValue.toLowerCase()));
    }
}