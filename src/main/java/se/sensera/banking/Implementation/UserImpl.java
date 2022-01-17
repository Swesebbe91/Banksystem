package se.sensera.banking.Implementation;

import lombok.Data;
import se.sensera.banking.User;

@Data
public class UserImpl implements User {

    String id;
    String name;
    String personalIdentificationNumber;
    boolean active;

    public UserImpl() {
        this.id = "unknown";
        this.name = "unknown";
        this.personalIdentificationNumber = "unknown";
        this.active = false;
    }

    public UserImpl(String id, String name, String personalIdentificationNumber, boolean active) {
        this.id = id;
        this.name = name;
        this.personalIdentificationNumber = personalIdentificationNumber;
        this.active = active;
    }

    public UserImpl(String name, String personalIdentificationNumber) {
        this.name = name;
        this.personalIdentificationNumber = personalIdentificationNumber;
    }

    @Override
    public String getId() {
        return this.id;
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
    public String getPersonalIdentificationNumber() {
        return this.personalIdentificationNumber;
    }

    @Override
    public void setPersonalIdentificationNumber(String pid) {
        this.personalIdentificationNumber = pid;
    }

    @Override
    public boolean isActive() {
        return this.active;
    }

    @Override
    public void setActive(boolean active) {
        this.active = active;
    }
}
