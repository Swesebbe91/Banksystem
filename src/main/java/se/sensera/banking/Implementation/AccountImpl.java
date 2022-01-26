package se.sensera.banking.Implementation;

import se.sensera.banking.Account;
import se.sensera.banking.User;

import java.util.stream.Stream;

public class AccountImpl implements Account {
    private User owner;
    private String name;

    public AccountImpl(User owner, String name){
        this.owner = owner;
         this.name = name;
    }

    @Override
    public String getId() {
        return null;
    }

    @Override
    public User getOwner() {
        return this.owner;
    }

    @Override
    public String getName() {return this.name;
    }

    @Override
    public void setName(String name) {

    }

    @Override
    public boolean isActive() {
        return owner.isActive();
    }

    @Override
    public void setActive(boolean active) {

    }

    @Override
    public Stream<User> getUsers() {
        return Stream.<User>builder().build();
    }

    @Override
    public void addUser(User user) {

    }

    @Override
    public void removeUser(User user) {

    }
}
