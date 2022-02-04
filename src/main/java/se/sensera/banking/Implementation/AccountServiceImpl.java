package se.sensera.banking.Implementation;

import se.sensera.banking.*;
import se.sensera.banking.exceptions.Activity;
import se.sensera.banking.exceptions.UseException;
import se.sensera.banking.exceptions.UseExceptionType;
import se.sensera.banking.utils.ListUtils;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
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
        if (usersRepository.getEntityById(userId).isEmpty()) {
            throw new UseException(Activity.CREATE_ACCOUNT, UseExceptionType.USER_NOT_FOUND, "User not found");
        } else if (accountsRepository.all().anyMatch(x -> x.getName().equals(accountName))) {
            throw new UseException(Activity.CREATE_ACCOUNT, UseExceptionType.ACCOUNT_NAME_NOT_UNIQUE, "Not unique name");
        }
        User user = usersRepository.getEntityById(userId).get();

        return accountsRepository.save(factory.createAccountObject(user, accountName, userId, true)); // Factory pattern
    }

    @Override
    public Account changeAccount(String userId, String accountId, Consumer<ChangeAccount> changeAccountConsumer) throws UseException {
        Account account = accountsRepository.getEntityById(accountId).get();
        User userAccount = usersRepository.getEntityById(userId).get();
        if (!account.getOwner().equals(userAccount)) {
            throw new UseException(Activity.UPDATE_ACCOUNT, UseExceptionType.NOT_OWNER, "Not owner of account");
        }
        if (!account.isActive()) {
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
        if (accountsRepository.getEntityById(accountId).isEmpty()) {
            throw new UseException(Activity.UPDATE_ACCOUNT, UseExceptionType.NOT_FOUND, "Account does not exist");
        }
        Account accountUser = accountsRepository.getEntityById(accountId).get(); //Nuvarande account
        User currentUser = usersRepository.getEntityById(userId).get(); //Nuvarande user
        User newUser = usersRepository.getEntityById(userIdToBeAssigned).get(); //ny user
        Account account = accountsRepository.getEntityById(accountId).get();
        if (!accountUser.getOwner().getId().equals(userId)) {//Test rad 94. Ska inte kunna lägga till användare för att man inte är ägare
            throw new UseException(Activity.UPDATE_ACCOUNT, UseExceptionType.NOT_OWNER, "Not owner");
        }
        if (!account.isActive()) {
            throw new UseException(Activity.UPDATE_ACCOUNT, UseExceptionType.ACCOUNT_NOT_ACTIVE, "Account not active"); //account inte aktivt
        }
        if (account.getOwner().getId().equals(userIdToBeAssigned)) {//Test rad 80, kan inte lägga till ägare som användare
            throw new UseException(Activity.UPDATE_ACCOUNT, UseExceptionType.CANNOT_ADD_OWNER_AS_USER, "Cannot add owner as user");
        }
        if (account.getUsers().anyMatch(x -> x.getId().equals(userIdToBeAssigned))) {
            throw new UseException(Activity.UPDATE_ACCOUNT, UseExceptionType.USER_ALREADY_ASSIGNED_TO_THIS_ACCOUNT, "Already assigned");
        }
        usersRepository.save(newUser);
        accountsRepository.save(accountUser);
        accountUser.addUser(newUser);
        return accountUser;
    }

    @Override
    public Account removeUserFromAccount(String userId, String accountId, String userIdToBeAssigned) throws UseException {
        Account account = accountsRepository.getEntityById(accountId).get();
        User currUser = usersRepository.getEntityById(userIdToBeAssigned).get();
        if (!account.getOwner().getId().equals(userId)) {
            throw new UseException(Activity.UPDATE_ACCOUNT, UseExceptionType.NOT_OWNER, "Not owner");
        }
        if (account.getUsers().noneMatch(x -> x.getId().equals(userIdToBeAssigned))) {
            throw new UseException(Activity.UPDATE_ACCOUNT, UseExceptionType.USER_NOT_ASSIGNED_TO_THIS_ACCOUNT, "Are you lost?");
        }
        account.removeUser(currUser);
        accountsRepository.save(account);
        return account;
    }

    @Override
    public Account inactivateAccount(String userId, String accountId) throws UseException {
        if (!accountsRepository.getEntityById(accountId).isPresent()) {
            throw new UseException(Activity.INACTIVATE_ACCOUNT, UseExceptionType.NOT_FOUND, "Account not found");
        } else if (!usersRepository.getEntityById(userId).isPresent()) {
            throw new UseException(Activity.INACTIVATE_ACCOUNT, UseExceptionType.USER_NOT_FOUND, "User not found");
        }
        Account account = accountsRepository.getEntityById(accountId).get();
        User userAccount = usersRepository.getEntityById(userId).get();

        if (!account.getOwner().equals(userAccount)) {
            throw new UseException(Activity.INACTIVATE_ACCOUNT, UseExceptionType.NOT_OWNER, "Not Owner");
        }
        if (!account.isActive()) {
            throw new UseException(Activity.INACTIVATE_ACCOUNT, UseExceptionType.NOT_ACTIVE, "Not active");
        }
        account.setActive(false);
        accountsRepository.save(account);
        return account;
    }

    @Override
    public Stream<Account> findAccounts(String searchValue, String userId, Integer pageNumber, Integer pageSize, SortOrder sortOrder) throws UseException {
        Stream<Account> accountStream = accountsRepository.all();
        if (searchValue != null && !searchValue.isEmpty()) {
            accountStream = accountStream.filter(account -> account.getName().toLowerCase().contains(searchValue.toLowerCase()));
        }
        if (userId != null) {
            User my = usersRepository.getEntityById(userId).get();
            accountStream = accountStream.filter(account -> account.getUsers().anyMatch(user -> user.getId().equals(userId)) || account.getOwner().equals(my));
        }
        if (SortOrder.AccountName.equals(sortOrder)) {
            accountStream = accountStream.sorted(Comparator.comparing(Account::getName));
        }
        return ListUtils.applyPage(accountStream, pageNumber, pageSize);
    }
}