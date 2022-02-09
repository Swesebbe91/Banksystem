package se.sensera.banking.Implementation;

import lombok.*;
import se.sensera.banking.User;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserImpl implements User {

    String id;
    String name;
    String personalIdentificationNumber;
    boolean active;

}
