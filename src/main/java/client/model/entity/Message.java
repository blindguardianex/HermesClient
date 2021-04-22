package client.model.entity;

import com.google.gson.annotations.Expose;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Message {

    @Expose
    private int id;
    @Expose
    private Dialog dialog;
    @Expose
    private User sender;
    @Expose
    private Timestamp timeOfSending;
    @Expose
    private String textMessage;
    @Expose
    private int isRead;

    void init(){
        isRead=0;
        java.util.Date datesend = new java.util.Date();
        timeOfSending = new Timestamp(datesend.getTime());
    }

    public static Builder getBuilder(){
        return new Builder();
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Builder{

        private Dialog dialog;
        private User sender;
        private String textMessage;

        public Builder withDialog(Dialog dialog){
            this.dialog=dialog;
            return this;
        }

        public Builder withSender(User sender){
            this.sender = sender;
            return this;
        }

        public Builder withTextMessage(String textMessage) {
            this.textMessage=textMessage;
            return this;
        }

        public Message build(){
            Message message = new Message();
            message.init();
            message.setDialog(dialog);
            message.setSender(sender);
            message.setTextMessage(textMessage);
            return message;
        }
    }
}
