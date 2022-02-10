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
    AccountsRepository accountsRepository;
    Factory factory = new Factory();
    AccountCheckHelper accountCheckHelper;

    public AccountServiceImpl(UsersRepository usersRepository, AccountsRepository accountsRepository, AccountCheckHelper accountCheckHelper) {
        this.usersRepository = usersRepository;
        this.accountsRepository = accountsRepository;
        this.accountCheckHelper = accountCheckHelper;
    }

    @Override
    public Account createAccount(String userId, String accountName) throws UseException {
        verifyBeforeCreatingAccount(userId, accountName);
        return accountsRepository.save(factory.createAccountObject(userId, getUser(userId), accountName, true)); // Factory pattern
    }

    @Override
    public Account changeAccount(String userId, String accountId, Consumer<ChangeAccount> changeAccountConsumer) throws UseException {
        Account account = getAccount(accountId);
        verifyBeforeChanging(userId, account);
        changeAccountConsumer.accept(name -> {
            if (name.equals(account.getName())) {
            } else {
                verifyUniqueAccountName(name);
                account.setName(name);
                accountsRepository.save(account);
            }
        });
        return account;
    }

    @Override
    public Account addUserToAccount(String userId, String accountId, String userIdToBeAssigned) throws UseException {
        verifyAccountExist(accountId);
        Account account = getAccount(accountId);
        verifyBeforeAdding(userId, userIdToBeAssigned, account);
        User newUser = getUser(userIdToBeAssigned);
        account.addUser(newUser);
        usersRepository.save(newUser);
        return accountsRepository.save(account);
    }

    @Override
    public Account removeUserFromAccount(String userId, String accountId, String userIdToBeAssigned) throws UseException {
        Account account = getAccount(accountId);
        verifyBeforeRemoving(userId, userIdToBeAssigned, account);
        User currentUser = getUser(userIdToBeAssigned);
        account.removeUser(currentUser);
        return accountsRepository.save(account);
    }

    @Override
    public Account inactivateAccount(String userId, String accountId) throws UseException {
        verifyAccountAndUserEmpty(userId, accountId);
        Account account = getAccount(accountId);
        verifyBeforeInactivating(userId, account);
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
    
    /****** FETCH DATA FROM REPOSITORIES ******/
    public Account getAccount(String accountId) {
        return accountsRepository.getEntityById(accountId).get();
    }

    public User getUser(String userId) {
        return usersRepository.getEntityById(userId).get();
    }

    /************* VERIFICATIONS **************/
    private void verifyUniqueAccountName(String name) throws UseException {
        if (accountsRepository.all().anyMatch(x -> x.getName().contains(name)))
            throw new UseException(Activity.UPDATE_ACCOUNT, UseExceptionType.ACCOUNT_NAME_NOT_UNIQUE, "Account name not unique");
    }

    private void verifyAccountExist(String accountId) throws UseException {
        if (accountsRepository.all().noneMatch(account -> account.getId().equals(accountId)))
            throw new UseException(Activity.UPDATE_ACCOUNT, UseExceptionType.NOT_FOUND, "Account does not exist");
    }

    private void verifyAccountAndUserEmpty(String userId, String accountId) throws UseException {
        accountCheckHelper.checkIfAccountFoundToInactivateAccount(accountId);
        accountCheckHelper.checkIfUserFoundToInactivateAccount(userId);
    }

    private void verifyBeforeCreatingAccount(String userId, String accountName) throws UseException {
        accountCheckHelper.checkIfUserFound(userId);
        accountCheckHelper.checkIfAccountNameUnique(accountName);
    }

    private void verifyBeforeChanging(String userId, Account account) throws UseException {
        accountCheckHelper.checkIfAccountOwner(account, userId, Activity.UPDATE_ACCOUNT, UseExceptionType.NOT_OWNER);
        accountCheckHelper.checkIfAccountIsActive(account, Activity.UPDATE_ACCOUNT, UseExceptionType.NOT_ACTIVE);
    }

    private void verifyBeforeAdding(String userId, String userIdToBeAssigned, Account account) throws UseException {
        accountCheckHelper.checkIfAccountOwner(account, userId, Activity.UPDATE_ACCOUNT, UseExceptionType.NOT_OWNER);
        accountCheckHelper.checkIfAccountIsActive(account, Activity.UPDATE_ACCOUNT, UseExceptionType.ACCOUNT_NOT_ACTIVE);
        accountCheckHelper.checkOwnerIsNotUser(userIdToBeAssigned, account);
        accountCheckHelper.checkIfAlreadyAssigned(account, userIdToBeAssigned, Activity.UPDATE_ACCOUNT, UseExceptionType.USER_ALREADY_ASSIGNED_TO_THIS_ACCOUNT);
    }

    private void verifyBeforeRemoving(String userId, String userIdToBeAssigned, Account account) throws UseException {
        accountCheckHelper.checkIfAccountOwner(account, userId, Activity.UPDATE_ACCOUNT, UseExceptionType.NOT_OWNER);
        accountCheckHelper.checkIfNotAssignedToAccount(account, userIdToBeAssigned, Activity.UPDATE_ACCOUNT, UseExceptionType.USER_NOT_ASSIGNED_TO_THIS_ACCOUNT);
    }

    private void verifyBeforeInactivating(String userId, Account account) throws UseException {
        accountCheckHelper.checkIfAccountOwner(account, userId, Activity.INACTIVATE_ACCOUNT, UseExceptionType.NOT_OWNER);
        accountCheckHelper.checkIfAccountIsActive(account, Activity.INACTIVATE_ACCOUNT, UseExceptionType.NOT_ACTIVE);
    }

    /**************** CHECKS ******************//*
    static private void checkIfAccountOwner(Account account, String userId, Activity activity, UseExceptionType type) throws UseException {
        if (!account.getOwner().getId().equals(userId))
            throw new UseException(Activity.valueOf(activity.name()), UseExceptionType.valueOf(type.name()), "Person is not owner of account");
    }

    private void checkIfUserFound(String userId) throws UseException {
        if (usersRepository.getEntityById(userId).isEmpty())
            throw new UseException(Activity.CREATE_ACCOUNT, UseExceptionType.USER_NOT_FOUND, "User not found");
    }

    private void checkIfAccountNameUnique(String accountName) throws UseException {
        if (accountsRepository.all().anyMatch(x -> x.getName().equals(accountName)))
            throw new UseException(Activity.CREATE_ACCOUNT, UseExceptionType.ACCOUNT_NAME_NOT_UNIQUE, "Not unique account name");
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
            throw new UseException(Activity.valueOf(activity.name()), UseExceptionType.valueOf(type.name()), "User is already assigned to account");
    }

    private void checkIfNotAssignedToAccount(Account account, String userIdToBeAssigned, Activity activity, UseExceptionType type) throws UseException {
        if (account.getUsers().noneMatch(x -> x.getId().equals(userIdToBeAssigned)))
            throw new UseException(Activity.valueOf(activity.name()), UseExceptionType.valueOf(type.name()), "Person has no authorized access to account");
    }

    private void checkIfAccountFoundToInactivateAccount(String accountId) throws UseException {
        if (accountsRepository.getEntityById(accountId).isEmpty())
            throw new UseException(Activity.INACTIVATE_ACCOUNT, UseExceptionType.NOT_FOUND, "Account not found");
    }

    private void checkIfUserFoundToInactivateAccount(String userId) throws UseException {
        if (usersRepository.getEntityById(userId).isEmpty())
            throw new UseException(Activity.INACTIVATE_ACCOUNT, UseExceptionType.USER_NOT_FOUND, "User not found");
    }
*/
    /**************** FETCH STREAMS *****************/
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

