package se.sensera.banking.Implementation;

import se.sensera.banking.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class AccountImpl implements Account {
    private final String id;
    private final User owner;
    private String name;
    private boolean active;
    private List<User> userList = new ArrayList<>();

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
        userList.add(user);
    }

    @Override
    public void removeUser(User user) {
        userList.remove(user);
    }
}
