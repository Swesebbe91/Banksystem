package se.sensera.banking.Implementation;

import lombok.Data;
import se.sensera.banking.User;

@Data
public class UserImpl implements User {

    final String id;
    String name;
    String personalIdentificationNumber;
    boolean active;

    public UserImpl(String id, String name, String personalIdentificationNumber, boolean active) {
        this.id = id;
        this.name = name;
        this.personalIdentificationNumber = personalIdentificationNumber;
        this.active = active;
    }
}
