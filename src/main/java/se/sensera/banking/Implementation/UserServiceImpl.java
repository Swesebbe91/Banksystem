package se.sensera.banking.Implementation;

import se.sensera.banking.*;
import se.sensera.banking.exceptions.Activity;
import se.sensera.banking.exceptions.UseException;
import se.sensera.banking.exceptions.UseExceptionType;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Scanner;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class UserServiceImpl implements UserService {

    final UsersRepository usersRepository;

    public UserServiceImpl(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }

    @Override
    public User createUser(String name, String personalIdentificationNumber) throws UseException {
        try {
            if (!checkPID(personalIdentificationNumber)) {
                boolean active = true;
                String id = String.valueOf(generateID());
                UserImpl newUser = new UserImpl(id, name, personalIdentificationNumber, active);
                return usersRepository.save(newUser);
            }
            throw new UseException(Activity.CREATE_USER, UseExceptionType.USER_PERSONAL_ID_NOT_UNIQUE, "Hejsan");
        } catch (UseException e) {
            throw e;
        }
    }

    public boolean checkPID(String pid) {
        boolean bool = usersRepository.all()
                .anyMatch(x -> x.getPersonalIdentificationNumber().equals(pid));
        return bool;
    }

    public UUID generateID() {
        return UUID.fromString(UUID.randomUUID().toString());
    }

    @Override
    public User changeUser(String userId, Consumer<ChangeUser> changeUser) throws UseException {
        User updatedUser;
        if(usersRepository.getEntityById(userId).isEmpty()) //Tittar först om det vi hämtar är tomt.
            throw new UseException(Activity.UPDATE_USER, UseExceptionType.NOT_FOUND, "empty");
        else {
        updatedUser = usersRepository.getEntityById(userId).get(); //OM där finns något, sätt värdet!
        }

             ChangeUser test = new ChangeUser() {
                @Override
                public void setName(String name) {
                    updatedUser.setName(name);
                }
                @Override
                public void setPersonalIdentificationNumber(String personalIdentificationNumber) throws UseException {
                    updatedUser.setPersonalIdentificationNumber(personalIdentificationNumber);
                }
            };
            //System.out.println(test.toString());
            changeUser.accept(test);
            //test.setName(String.valueOf(changeUser));
            return usersRepository.save(updatedUser);


}
    @Override
    public User inactivateUser(String userId) throws UseException {
        return null;
    }

    @Override
    public Optional<User> getUser(String userId) {
        return usersRepository.getEntityById(userId); //Returnerar ett värde på true eller false



    }

    @Override
    public Stream<User> find(String searchString, Integer pageNumber, Integer pageSize, SortOrder sortOrder) {
        return null;
    }


}
