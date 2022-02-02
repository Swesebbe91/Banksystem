package se.sensera.banking.Implementation;

import se.sensera.banking.*;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AccountImpl implements Account {
    private String id;
    private User owner;
    private String name;
    private boolean active;

    public AccountImpl(User owner, String name, String id, boolean active){
         this.id = id;
         this.owner = owner;
         this.name = name;
         this.active = active;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public User getOwner() {
        return this.owner;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean isActive() {
        return this.active;
    }

    @Override
    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public Stream<User> getUsers() {
        return Stream.<User>builder().build();
    }

    @Override
    public void addUser(User user) {

        //accountsRepository.save(user);;
        //users.peek(System.out::println).collect(Collectors.toList());

    }

    @Override
    public void removeUser(User user) {

    }
}
