package client.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import lombok.extern.slf4j.Slf4j;

/**
 * НЕ ИСПОЛЬЗУЕТСЯ
 * НЕ ФУНКЦИОНИРУЕТ
 */
@Slf4j
public class DialogControl {
    private int dialogId;
    @FXML protected Label whoIs;
    @FXML protected Button send;
    @FXML protected TextArea messageDialog;
    @FXML protected TextArea privateChat;

    public void setWhoIs(String name){
        whoIs.setText(String.format("Диалог с пользователем %s", name));
    }

//    @FXML private void sendMessage() throws IOException {
//        ClientApp.getDialogList().get(dialogId).sendDialogMessage(messageDialog.getText());
//        messageDialog.clear();
//    }

    public void setDialogId(int dialogId) {
        this.dialogId = dialogId;
    }

    public void setPrivateChat(String message){
        String chat = null;
        if (privateChat.getText().equals("")) chat = message;
        else chat = privateChat.getText()+"\n"+message;
        privateChat.setText(chat);
    }
}
