package se.sensera.banking.Implementation;

import lombok.AllArgsConstructor;
import se.sensera.banking.Account;
import se.sensera.banking.AccountsRepository;
import se.sensera.banking.UsersRepository;
import se.sensera.banking.exceptions.Activity;
import se.sensera.banking.exceptions.UseException;
import se.sensera.banking.exceptions.UseExceptionType;

@AllArgsConstructor
public class AccountCheckHelper {
    
    UsersRepository usersRepository;
    AccountsRepository accountsRepository;

    /**************** CHECKS ******************/
     void checkIfAccountOwner(Account account, String userId, Activity activity, UseExceptionType type) throws UseException {
        if (!account.getOwner().getId().equals(userId))
            throw new UseException(Activity.valueOf(activity.name()), UseExceptionType.valueOf(type.name()), "Person is not owner of account");
    }

     void checkIfUserFound(String userId) throws UseException {
        if (usersRepository.getEntityById(userId).isEmpty())
            throw new UseException(Activity.CREATE_ACCOUNT, UseExceptionType.USER_NOT_FOUND, "User not found");
    }

     void checkIfAccountNameUnique(String accountName) throws UseException {
        if (accountsRepository.all().anyMatch(x -> x.getName().equals(accountName)))
            throw new UseException(Activity.CREATE_ACCOUNT, UseExceptionType.ACCOUNT_NAME_NOT_UNIQUE, "Not unique account name");
    }

     void checkIfAccountIsActive(Account account, Activity activity, UseExceptionType type) throws UseException {
        if (!account.isActive())
            throw new UseException(Activity.valueOf(activity.name()), UseExceptionType.valueOf(type.name()), "Account is not active");
    }

    public void checkOwnerIsNotUser(String userIdToBeAssigned, Account account) throws UseException {
        if (account.getOwner().getId().equals(userIdToBeAssigned))
            throw new UseException(Activity.UPDATE_ACCOUNT, UseExceptionType.CANNOT_ADD_OWNER_AS_USER, "Cannot add owner as user");
    }

     void checkIfAlreadyAssigned(Account account, String userIdToBeAssigned, Activity activity, UseExceptionType type) throws UseException {
        if (account.getUsers().anyMatch(x -> x.getId().equals(userIdToBeAssigned)))
            throw new UseException(Activity.valueOf(activity.name()), UseExceptionType.valueOf(type.name()), "User is already assigned to account");
    }

     void checkIfNotAssignedToAccount(Account account, String userIdToBeAssigned, Activity activity, UseExceptionType type) throws UseException {
        if (account.getUsers().noneMatch(x -> x.getId().equals(userIdToBeAssigned)))
            throw new UseException(Activity.valueOf(activity.name()), UseExceptionType.valueOf(type.name()), "Person has no authorized access to account");
    }

     void checkIfAccountFoundToInactivateAccount(String accountId) throws UseException {
        if (accountsRepository.getEntityById(accountId).isEmpty())
            throw new UseException(Activity.INACTIVATE_ACCOUNT, UseExceptionType.NOT_FOUND, "Account not found");
    }

     void checkIfUserFoundToInactivateAccount(String userId) throws UseException {
        if (usersRepository.getEntityById(userId).isEmpty())
            throw new UseException(Activity.INACTIVATE_ACCOUNT, UseExceptionType.USER_NOT_FOUND, "User not found");
    }
}
