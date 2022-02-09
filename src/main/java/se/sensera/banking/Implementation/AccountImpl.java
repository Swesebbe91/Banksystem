package se.sensera.banking.Implementation;

import lombok.*;
import se.sensera.banking.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountImpl implements Account {
    String id;
    User owner;
    String name;
    boolean active;
    final List<User> userList = new ArrayList<>();

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
