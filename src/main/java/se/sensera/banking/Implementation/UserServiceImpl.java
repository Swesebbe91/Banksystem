package se.sensera.banking.Implementation;

import se.sensera.banking.User;
import se.sensera.banking.UserService;
import se.sensera.banking.UsersRepository;
import se.sensera.banking.exceptions.UseException;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class UserServiceImpl implements UserService {


    private UsersRepository usersRepository;
    private UserImpl test;

    public UserServiceImpl(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;

    }

    @Override
    public User createUser(String name, String personalIdentificationNumber) throws UseException {
        UserImpl newPerson = new UserImpl(name, personalIdentificationNumber);

         // här är implementationen med funktionen
        // felhantera när den ska kasta ett undantag;

        return newPerson;
    }

    @Override
    public User changeUser(String userId, Consumer<ChangeUser> changeUser) throws UseException {
        return null;
    }

    @Override
    public User inactivateUser(String userId) throws UseException {
        return null;
    }

    @Override
    public Optional<User> getUser(String userId) {
        return Optional.empty();
    }

    @Override
    public Stream<User> find(String searchString, Integer pageNumber, Integer pageSize, SortOrder sortOrder) {
        return null;
    }
}
