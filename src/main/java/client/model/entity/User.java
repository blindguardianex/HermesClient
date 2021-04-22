package client.model.entity;

import client.model.ClientApp;
import com.google.gson.annotations.Expose;
import javafx.beans.property.SimpleStringProperty;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class User {

    @Expose
    private int id;
    @Expose
    private String login;
    @Expose
    private String password;
    @Expose
    private Role role;
    @Expose
    private short status;
    private Set<Dialog>dialogs;
    private List<Task> tasks;

    private transient int notReadMessages=0;
    private transient SimpleStringProperty shortName;

    public String getShortName() {
        if (shortName==null){
            UserProfile profile = ClientApp.getClientList().get(login).getValue();
            shortName=new SimpleStringProperty(profile.getUserFIO());
        }
        return shortName.get();
    }

    public SimpleStringProperty shortNameProperty() {
        if (shortName==null){
            UserProfile profile = ClientApp.getClientList().get(login).getValue();
            shortName=new SimpleStringProperty(profile.getUserFIO());
        }
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName.set(shortName);
    }

    void init(){
        status = 0;
        dialogs = new HashSet<>();
        tasks = new ArrayList<>();
    }

    public static Builder getBuilder(){
        return new Builder();
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Builder{

        private String login;
        private String password;
        private Role role;

        public User build(){
            User user = new User();
            user.setLogin(login);
            user.setPassword(password);
            user.setRole(role);
            return user;
        }

        public Builder withLogin(String login) {
            this.login = login;
            return this;
        }

        public Builder withPassword(String password) {
            this.password = password;
            return this;
        }

        public Builder withRole(Role role) {
            this.role = role;
            return this;
        }
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", login='" + login + '\'' +
                ", role=" + role +
                ", status=" + status +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id == user.id &&
                Objects.equals(login, user.login);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, login);
    }
}
