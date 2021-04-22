package client.controller;

import client.model.ClientApp;
import client.model.Instruction;
import client.model.configuration.elementsfactory.ActionFactory;
import client.model.entity.Task;
import client.model.entity.User;
import client.model.entity.UserProfile;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

import java.io.*;

/** Окно контроллера окна задачи */
@Slf4j
public class TaskControl  implements ControllerWithRoot, ControllerWithMain, ControllerWithPresets{

    /** Контроллер основного окна приложения */
    private MainControl mainControl;
    /** Контроллер основного меню приложения */
    private RootControl rootControl;
    /** Открытая задача */
    private Task task;

    @FXML private Label taskName;
    @FXML private Label category;
    @FXML private Label timeOfCreated;
    @FXML private Label timeOfComplete;
    @FXML private Label infoTitle;
    @FXML private TextField senderFIO;
    @FXML private TextField senderDepartment;
    @FXML private TextField senderPosition;
    @FXML private TextField senderMail;
    @FXML private TextField senderPhoneNumber;
    @FXML private TextArea taskBodyArea;
    @FXML private TextArea correspondenceArea;
    @FXML private TextArea answerArea;
    @FXML private Button sendButton;
    @FXML private Button chooseFileButton;
    @FXML private Button downloadFileButton;
    @FXML private TextField selectedFile;
    @FXML private ComboBox<String> filesForDownload;

    private final FileChooser fileChooser = new FileChooser();
    private final DirectoryChooser dirChooser = new DirectoryChooser();
    private File uploadFile;

    @FXML
    private void initialize(){
        log.info("Initialize task stage...");
        chooseFileButton.setOnAction(event->{
            uploadFile = fileChooser.showOpenDialog(chooseFileButton.getScene().getWindow());
            if (uploadFile!=null){
                selectedFile.setText(String.valueOf(uploadFile));
            }
        });

        downloadFileButton.setOnAction(event->{
            if (filesForDownload.getValue().equals("<Прикрепленные файлы>")||filesForDownload.getValue().equals("Нет прикрепленных файлов")){
                return;
            }
            File downloadDir = dirChooser.showDialog(downloadFileButton.getScene().getWindow());
            if (downloadDir!=null){
                String downloadPath = downloadDir.getAbsolutePath()+"\\";
                String downloadFile = downloadPath+filesForDownload.getValue();
                String[]files = task.getLinks().split(",");
                String neededFile = null;
                for (String file:files){
                    if (file.endsWith("\\"+filesForDownload.getValue())){
                        neededFile=file;
                        break;
                    }
                }
                if (neededFile==null){
                    log.error("Файл для скачивания не найден в файлах задачи.");
                    return;
                }
                ClientApp.outWrite(String.format("%s%s/%s/%s",Instruction.DOWNLOAD_FILE.getCommand(),task.getId(),neededFile,downloadFile));
            }
        });
        log.info("Complete");
    }

    /**
     * Инициализирует окно задачи.
     * @param task
     * @param taskType
     *      принимает 3 типа:
     *      0 - задача направлена пользователю
     *      1 - задача направлена пользователем
     *      2 - задача принадлежит подчиненному сотруднику
     *      3 - минимизированная версия приложения
     */
    public void presets(Task task, int taskType){
        log.info("Initialize task...");
        this.task=task;
        checkTaskForRead(taskType);
        initTextLabels();
        setFilesForDownloads();
        switch (taskType) {
            case 0:
            case 3: {
                log.info("Task for current user");
                initSetCompleteButton();
                break;
            }
            case 1: {
                log.info("Appointed task from current user");
                infoTitle.setText("Информация о получателе");
                initRefundedTaskButton();
                break;
            }
            case 2: {
                log.info("Open task for other user");
                sendButton.setDisable(true);
                chooseFileButton.setDisable(true);
                break;
            }
            default: {
                log.error("Unknown task type: " + taskType);
                throw new IllegalArgumentException("Unknown task type: " + taskType);
            }
        }

        answerArea.setOnKeyPressed(ke->{
            if (ke.getCode().equals(KeyCode.ENTER)){
                sendButton.fire();
            }
        });
        log.info("Complete");
    }

    /**
     * Если это задача для авторизованного пользователя и открывается в первый раз,
     * тогда устанавливает статус задачи "Прочитана"
     * @param taskType
     */
    private void checkTaskForRead(int taskType){
        if (taskType==0 && task.getIsRead()==0){
            task.setIsRead((short) 1);
            rootControl.decrementNotReadTaskCount();
            ClientApp.outWrite(String.format("%s%s",Instruction.TASK_READ.getCommand(),task.getId()));
        }
    }

    /**
     * Инициализирует текстовые лабели в карточке задачи
     */
    private void initTextLabels(){
        taskName.setText("Наименование задачи: "+task.getName());
        category.setText("Категория задачи: "+task.getCategory());
        timeOfCreated.setText("Дата создания задачи: "+task.getTimeOfCreated());
        timeOfComplete.setText(task.getTimeOfComplete()==null ?
                "Дата решения задачи: задача не решена" :
                "Дата решения задачи: "+task.getTimeOfComplete());
        User sender = task.getSender();
        UserProfile senderProfile = ClientApp.getClientList().get(sender.getLogin()).getValue();
        senderFIO.setText(senderProfile.getUserFIO());
        senderDepartment.setText(senderProfile.getDepartment().getName());
        senderPosition.setText(senderProfile.getPosition().getName());
        senderMail.setText(senderProfile.getEmail());
        senderPhoneNumber.setText(senderProfile.getPhoneNumber());
        taskBodyArea.setText(task.getBody());
        String annotation = task.getAnswerAnnotation();
        correspondenceArea.setText(annotation==null ? "" : annotation.replace('|','\n'));
    }

    /**
     * Инициализирует комбо бокс для скачивания файла
     */
    private void setFilesForDownloads(){
        String[]files = task.getLinks()==null ? new String [0] : task.getLinks().split(",");
        String[]fileNames=new String[files.length];
        for (int i=0;i<files.length;i++){
            String[]temp = files[i].split("\\\\");
            fileNames[i]= temp[temp.length-1];
        }
        filesForDownload.getItems().addAll(fileNames);
        filesForDownload.setValue("<Прикрепленные файлы>");
    }

    /**
     * Инициализирует sendButton как кнопку, устанавливающую задачу выполненой
     */
    private void initSetCompleteButton(){
        sendButton.setText("Установить решенной");
        if (task.getComplete()!=0){
            sendButton.setDisable(true);
        }
        sendButton.setOnAction(event->{
            log.info("Sending task");
            ClientApp.outWrite(String.format("%s%s/%s", Instruction.TASK_COMPLETE.getCommand(),task.getId(),getAnswer()));
            mainControl.taskComplete(task);
            if (uploadFile!=null){
                ClientApp.outWrite(String.format("%s%s/%s",Instruction.FILE_FOR_TASK.getCommand(),task.getId(),uploadFile.getName()));
                ClientApp.sendFile(uploadFile);
            }
            Stage stage = (Stage) sendButton.getScene().getWindow();
            stage.close();
            log.info("Complete");
        });
    }

    /**
     * Инициализирует sendButton как кнопку, возвращающую задачу на доработку
     */
    private void initRefundedTaskButton(){
        sendButton.setText("Вернуть на доработку");
        if (task.getComplete()==0){
            sendButton.setDisable(true);
        }
        sendButton.setOnAction(event->{
            ClientApp.outWrite(String.format("%s%s/%s",Instruction.TASK_REFUNDED.getCommand(),task.getId(),getRefundAnswer()));
            mainControl.taskRefunded(task);
            if (uploadFile!=null){
                ClientApp.outWrite(String.format("%s%s/%s",Instruction.FILE_FOR_TASK.getCommand(),task.getId(),uploadFile.getName()));
                ClientApp.sendFile(uploadFile);
            }
            Stage stage = (Stage) sendButton.getScene().getWindow();
            stage.close();
        });
    }

    /**
     * Формирует ответ для решенной задачи
     * @return
     */
    private String getAnswer(){
        String answer = answerArea.getText().equals("") ?
                correspondenceArea.getText().equals("") ?
                "Решена: без комментария" :
                correspondenceArea.getText().replace('\n','|')+"|Решена: без комментария" :
                correspondenceArea.getText().replace('\n','|')+"|Решена: "+answerArea.getText();
        return answer;
    }

    /**
     * Формирует ответ для задачи, возвращенной на доработку
     * @return
     */
    private String getRefundAnswer(){
        String answer = answerArea.getText().equals("") ?
                correspondenceArea.getText().replace('\n','|')+"|Возвращена: без комментария" :
                correspondenceArea.getText().replace('\n','|')+"|Возвращена: "+answerArea.getText();
        return answer;
    }

    public void setMainControl(MainControl mainControl) {
        this.mainControl = mainControl;
    }

    public void setRootControl(RootControl rootControl) {
        this.rootControl = rootControl;
    }
}
