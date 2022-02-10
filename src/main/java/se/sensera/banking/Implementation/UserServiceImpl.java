package se.sensera.banking.Implementation;

import se.sensera.banking.*;
import se.sensera.banking.exceptions.Activity;
import se.sensera.banking.exceptions.UseException;
import se.sensera.banking.exceptions.UseExceptionType;
import se.sensera.banking.utils.ListUtils;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class UserServiceImpl implements UserService {

    final UsersRepository usersRepository;
    Factory factory = new Factory();

    public UserServiceImpl(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }

    @Override
    public User createUser(String name, String personalIdentificationNumber) throws UseException {
        verifyBeforeCreatingUser(personalIdentificationNumber);
        return usersRepository.save(factory.createUserObject(UUID.randomUUID().toString(), name, personalIdentificationNumber, true)); // factory pattern
    }

    @Override
    public User changeUser(String userId, Consumer<ChangeUser> changeUser) throws UseException {
        User user = checkIfUserExist(userId);
        ChangeUser updatedUser = new ChangeUser() {
            @Override
            public void setName(String name) {
                user.setName(name);
                usersRepository.save(user);
            }
            @Override
            public void setPersonalIdentificationNumber(String personalIdentificationNumber) throws UseException {
                if (verifyBeforeChangingUser(personalIdentificationNumber)) {
                    user.setPersonalIdentificationNumber(personalIdentificationNumber);
                    usersRepository.save(user);
                } else {
                    throw new UseException(Activity.UPDATE_USER, UseExceptionType.USER_PERSONAL_ID_NOT_UNIQUE, "Not unique personal identification number");
                }
            }
        };
        changeUser.accept(updatedUser);
        return user;
    }

    @Override
    public User inactivateUser(String userId) throws UseException {
        return getUser(userId)
                .map(user -> {
                    user.setActive(false);
                    return user;
                })
                .map(usersRepository::save)
                .orElseThrow(() -> new UseException(Activity.UPDATE_USER, UseExceptionType.NOT_FOUND, "Couldn't find userID"));
    }

    @Override
    public Optional<User> getUser(String userId) {
        return usersRepository.getEntityById(userId);
    }

    @Override
    public Stream<User> find(String searchString, Integer pageNumber, Integer pageSize, SortOrder sortOrder) {
        Stream<User> userStream = usersRepository.all();
        userStream = getUserStreamBasedOnString(searchString, userStream);
        userStream = getUserStreamBasedOnValue(sortOrder, userStream);
        return ListUtils.applyPage(userStream, pageNumber, pageSize);
    }

    private void verifyBeforeCreatingUser(String personalIdentificationNumber) throws UseException {
        if (usersRepository.all().anyMatch(user -> user.getPersonalIdentificationNumber().equals(personalIdentificationNumber)))
            throw new UseException(Activity.CREATE_USER, UseExceptionType.USER_PERSONAL_ID_NOT_UNIQUE, "Cannot create account");
    }

    private boolean verifyBeforeChangingUser(String pid) {
        return usersRepository.all().noneMatch(user -> user.getPersonalIdentificationNumber().equals(pid));
    }

    private User checkIfUserExist(String userId) throws UseException {
        return getUser(userId).orElseThrow(() -> new UseException(Activity.UPDATE_USER, UseExceptionType.NOT_FOUND, "empty"));
    }

    private Stream<User> getUserStreamBasedOnString(String searchString, Stream<User> userStream) {
        return userStream.filter(x -> x.getName().toLowerCase().contains(searchString));
    }

    private Stream<User> getUserStreamBasedOnValue(SortOrder sortOrder, Stream<User> streamOfUsers) {
        return switch (sortOrder) {
            case Name -> streamOfUsers.sorted(Comparator.comparing(User::getName));
            case PersonalId -> streamOfUsers.sorted(Comparator.comparing(User::getPersonalIdentificationNumber));
            default -> streamOfUsers.filter(User::isActive);
        };
    }
}