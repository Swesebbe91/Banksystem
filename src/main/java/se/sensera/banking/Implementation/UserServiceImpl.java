package se.sensera.banking.Implementation;

import se.sensera.banking.User;
import se.sensera.banking.UserService;
import se.sensera.banking.UsersRepository;
import se.sensera.banking.exceptions.UseException;

import java.util.Optional;
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
        boolean active = true;
        String id = String.valueOf(generateID());
        //If hämta report jämföra
        // OM inte gör detta under
        UserImpl newUser = new UserImpl(id, name, personalIdentificationNumber, active);
        return usersRepository.save(newUser);

        // Måste hantera ID med random
        // Måste sätta bool
        // try/catch
        // IF: Dubbelkolla att namn och pid inte redan finns i repo/list
        // Om det inte inte finns, go ahead
        // Annars, kasta ett undantag UseException
    }

    public UUID generateID() {
        return UUID.fromString(UUID.randomUUID().toString());
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
