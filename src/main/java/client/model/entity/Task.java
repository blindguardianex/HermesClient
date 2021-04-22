package client.model.entity;

import com.google.gson.annotations.Expose;
import javafx.beans.property.SimpleStringProperty;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Task {

    @Expose
    private int id;
    @Expose
    private String name;
    @Expose
    private User recipient;
    @Expose
    private String body;
    @Expose
    private User sender;
    @Expose
    private String category;
    @Expose
    private short complete;
    @Expose
    private Timestamp timeOfCreated;
    @Expose
    private String answerAnnotation;
    @Expose
    private Timestamp timeOfComplete;
    @Expose
    private short isRead;
    @Expose
    private String links;

    private SimpleStringProperty timeOfCreatedProperty;
    private SimpleStringProperty recipientNameProperty;
    private SimpleStringProperty senderNameProperty;
    private SimpleStringProperty nameProperty;

    void init(){
        isRead=0;
        complete=0;
        java.util.Date datesend = new java.util.Date();
        timeOfCreated = new Timestamp(datesend.getTime());
    }

    public void initProperty(){
        timeOfCreatedProperty=new SimpleStringProperty(timeOfCreated.toString());
        recipientNameProperty=new SimpleStringProperty(recipient.getShortName());
        senderNameProperty=new SimpleStringProperty(sender.getShortName());
        nameProperty=new SimpleStringProperty(name);
    }

    public static Builder getBuilder(){
        return new Builder();
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Builder{

        private String taskName;
        private User recipient;
        private String taskBody;
        private User sender;
        private String category;

        public Task build(){
            Task task = new Task();
            task.setName(taskName);
            task.setRecipient(recipient);
            task.setBody(taskBody);
            task.setSender(sender);
            task.setCategory(category);
            return task;
        }

        public Builder withTaskName(String taskName) {
            this.taskName = taskName;
            return this;
        }

        public Builder withRecipient(User recipient) {
            this.recipient = recipient;
            return this;
        }

        public Builder withTaskBody(String taskBody) {
            this.taskBody = taskBody;
            return this;
        }

        public Builder withSender(User sender) {
            this.sender = sender;
            return this;
        }

        public Builder withCategory(String category) {
            this.category = category;
            return this;
        }
    }

    public String getTimeOfCreatedProperty() {
        return timeOfCreatedProperty.get();
    }

    public SimpleStringProperty timeOfCreatedProperty() {
        return timeOfCreatedProperty;
    }

    public void setTimeOfCreatedProperty(String timeOfCreatedProperty) {
        this.timeOfCreatedProperty.set(timeOfCreatedProperty);
    }

    public String getSenderNameProperty() {
        return senderNameProperty.get();
    }

    public SimpleStringProperty senderNameProperty() {
        return senderNameProperty;
    }

    public void setSenderNameProperty(String senderNameProperty) {
        this.senderNameProperty.set(senderNameProperty);
    }

    public String getNameProperty() {
        return nameProperty.get();
    }

    public SimpleStringProperty nameProperty() {
        return nameProperty;
    }

    public void setNameProperty(String nameProperty) {
        this.nameProperty.set(nameProperty);
    }
}
