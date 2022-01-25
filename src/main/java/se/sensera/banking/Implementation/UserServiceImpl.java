package se.sensera.banking.Implementation;

import se.sensera.banking.*;
import se.sensera.banking.exceptions.Activity;
import se.sensera.banking.exceptions.UseException;
import se.sensera.banking.exceptions.UseExceptionType;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class UserServiceImpl implements UserService {

    final UsersRepository usersRepository;

    public UserServiceImpl(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }

    @Override
    public User createUser(String name, String personalIdentificationNumber) throws UseException {
        if (usersRepository.all()
                .anyMatch(user -> user.getPersonalIdentificationNumber().equals(personalIdentificationNumber)))
            throw new UseException(Activity.CREATE_USER, UseExceptionType.USER_PERSONAL_ID_NOT_UNIQUE, "Cannot create account");
        UserImpl newUser = new UserImpl(UUID.randomUUID().toString(), name, personalIdentificationNumber, true);
        return usersRepository.save(newUser);
    }

    @Override
    public User changeUser(String userId, Consumer<ChangeUser> changeUser) throws UseException {
        User updatedUser;
        if (usersRepository.getEntityById(userId).isEmpty()) //Tittar först om det vi hämtar är tomt.
            throw new UseException(Activity.UPDATE_USER, UseExceptionType.NOT_FOUND, "empty");
        else {
            updatedUser = usersRepository.getEntityById(userId).get(); //OM där finns något, sätt värdet!
        }
        ChangeUser test = new ChangeUser() {
            @Override
            public void setName(String name) {
                updatedUser.setName(name);
                usersRepository.save(updatedUser);
            }

            @Override
            public void setPersonalIdentificationNumber(String personalIdentificationNumber) throws UseException {

                if (usersRepository.all().anyMatch(x -> x.getPersonalIdentificationNumber().equals(personalIdentificationNumber))) {
                    throw new UseException(Activity.UPDATE_USER, UseExceptionType.USER_PERSONAL_ID_NOT_UNIQUE, "Not unique personalnr");
                } else {
                    updatedUser.setPersonalIdentificationNumber(personalIdentificationNumber);
                    usersRepository.save(updatedUser);
                }
            }
        };
        changeUser.accept(test);
        return updatedUser;
    }

    @Override
    public User inactivateUser(String userId) throws UseException {
        User inactiveUser;
        if (usersRepository.getEntityById(userId).isEmpty()) {
            throw new UseException(Activity.UPDATE_USER, UseExceptionType.NOT_FOUND, "Couldn't find userID");
        }
        inactiveUser = usersRepository.getEntityById(userId).get();
        inactiveUser.setActive(false);
        return usersRepository.save(inactiveUser);
    }

    @Override
    public Optional<User> getUser(String userId) {
        return usersRepository.getEntityById(userId); //Returnerar ett värde på true eller false
    }

    @Override
    public Stream<User> find(String searchString, Integer pageNumber, Integer pageSize, SortOrder sortOrder) {
        List<User> listOfUsers = usersRepository.all()
                .filter(x -> x.getName().toLowerCase().contains(searchString))
                .collect(Collectors.toList());
        return listOfUsers.stream();
    }
}
