package client.controller;

import client.model.ClientApp;
import client.model.Instruction;
import client.model.configuration.elementsfactory.ValidationListenerBuilder;
import client.model.entity.Task;
import client.model.entity.User;
import client.model.utils.Support;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

import java.io.File;

@Slf4j
public class TaskCreatingControl implements ControllerWithPresets{

    private static final String[]TASK_CATEGORIES = new String[]{"Поручение","Запрос","Заявка","Информационное сообщение","Прочее"};
    private static final String FORBIDDEN_CHARACTERS = "[^\\n/]*";

    @FXML protected TextField taskName;
    @FXML protected TextField selectedFile;
    @FXML protected TextArea taskBody;
    @FXML protected Button chooseFile;
    @FXML protected Button sendTask;
    @FXML protected ComboBox<String> categoryBox;

    private User recipient;
    private File uploadFile;
    private FileChooser fileChooser;
    private Gson gson;


    @FXML
    public void initialize(){
        log.info("Initialize stage for creating task...");
        gson=ClientApp.getGson();
        initFields();
        initButtons();
        log.info("Complete");

    }

    public void presets(User user){
        recipient = user;
    }

    private void initFields(){
        log.info("Initialize non-button fields...");
        fileChooser=new FileChooser();
        taskBody.textProperty().addListener(ValidationListenerBuilder.listenerFor(taskBody)
                .byPattern(FORBIDDEN_CHARACTERS).get());
        taskBody.setOnKeyPressed(ke->{
            if (ke.getCode().equals(KeyCode.ENTER)){
                sendTask.fire();
            }
        });
        categoryBox.getItems().addAll(TASK_CATEGORIES);
        log.debug("Complete");
    }

    private void initButtons(){
        log.info("Initialize buttons...");
        chooseFile.setOnAction(event->{
            uploadFile = fileChooser.showOpenDialog(chooseFile.getScene().getWindow());
            if (uploadFile!=null){
                selectedFile.setText(String.valueOf(uploadFile));
            }
        });

        sendTask.setOnAction(event->{
            log.info("Sending new task...");
            if (categoryBox.getValue().equals("<Выберите категорию>")) {
                Support.alertIncompleteData("Выберите категорию");
                return;
            }
            Task task = Task.getBuilder()
                    .withSender(ClientApp.getCurrentUser().getKey())
                    .withRecipient(recipient)
                    .withTaskName(taskName.getText())
                    .withCategory(categoryBox.getValue())
                    .withTaskBody(taskBody.getText())
                    .build();
            String taskJson = gson.toJson(task);
            ClientApp.outWrite(String.format("%s%s",
                    Instruction.CREATE_NEW_TASK.getCommand(),
                    taskJson));
            if (uploadFile!=null){
                ClientApp.outWrite(String.format("%s-1/%s",Instruction.FILE_FOR_TASK.getCommand(),uploadFile.getName()));
                ClientApp.sendFile(uploadFile);
            }
            log.info("Complete");
            Stage stage = (Stage) sendTask.getScene().getWindow();
            stage.close();
        });
        log.debug("Complete");
    }
}
