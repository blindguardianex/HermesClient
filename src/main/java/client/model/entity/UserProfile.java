package client.model.entity;

import com.google.gson.annotations.Expose;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;

@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserProfile {

    @Expose
    private int id;
    @Expose
    private transient User user;
    @Expose
    private Date registeredDate;
    @Expose
    private String email;
    @Expose
    private String lastName;
    @Expose
    private String firstName;
    @Expose
    private String otherName;
    @Expose
    private String phoneNumber;
    @Expose
    private Department department;
    @Expose
    private Position position;

    void init(){
        java.util.Date utilDate = new java.util.Date();
        registeredDate=new Date(utilDate.getTime());
    }

    public static Builder getBuilder(){
        return new Builder();
    }

    /**
     * Формирует представление ФИО пользователя
     * @return
     */
    public String getUserFIO(){
        return String.format("%s %c. %c.", lastName,firstName.charAt(0),otherName.charAt(0));
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Builder{

        private User user;
        private String email;
        private String lastName;
        private String firstName;
        private String otherName;
        private String phoneNumber;
        private Department department;
        private Position position;

        public UserProfile build(){
            UserProfile userProfile = new UserProfile();
            userProfile.init();
            userProfile.setUser(user);
            userProfile.setEmail(email);
            userProfile.setLastName(lastName);
            userProfile.setFirstName(firstName);
            userProfile.setOtherName(otherName);
            userProfile.setPhoneNumber(phoneNumber);
            userProfile.setDepartment(department);
            userProfile.setPosition(position);
            return userProfile;
        }

        public Builder withUser(User user) {
            this.user = user;
            return this;
        }

        public Builder withEmail(String email) {
            this.email = email;
            return this;
        }

        public Builder withLastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public Builder withFirstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public Builder withOtherName(String otherName) {
            this.otherName = otherName;
            return this;
        }

        public Builder withPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
            return this;
        }

        public Builder withDepartment(Department department) {
            this.department = department;
            return this;
        }

        public Builder withPosition(Position position) {
            this.position = position;
            return this;
        }
    }
}
