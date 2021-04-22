package client.model.entity;

import com.google.gson.annotations.Expose;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Dialog {

    @Expose
    private int id;
    @Expose
    private User creator;
    @Expose
    private String name;
    @Expose
    private short active;
    private Set<User> users;
    private Set<Message> messages;

    void init(){
        users = new HashSet<>();
        messages = new HashSet<>();
        active = 1;
    }

    public void addUser(User user){
        users.add(user);
        user.getDialogs().add(this);
    }

    public void removeUser(User user){
        users.remove(user);
        user.getDialogs().remove(this);
    }

    @Override
    public String toString() {
        return "Dialog{" +
                "id=" + id +
                ", creator=" + creator +
                ", name='" + name + '\'' +
                ", active=" + active +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Dialog dialog = (Dialog) o;
        return id == dialog.id &&
                Objects.equals(name, dialog.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
}
