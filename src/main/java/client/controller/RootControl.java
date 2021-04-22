package client.controller;

import client.UserPreferences;
import client.model.ClientApp;
import client.model.Instruction;
import client.model.utils.StageShower;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

/** Контроллер основного меню приложения */
@Slf4j
public class RootControl  implements ControllerWithMain{

    /** Кнопка минимизации приложения */
    @FXML protected Button minimizeButton;
    /** Поле с названием приложения */
    @FXML protected Label applicationName;
    /** Поле с номером версии приложения */
    @FXML protected Label version;
    /** Кнопка минимизации приложения в главном меню (Опции) */
    @FXML protected MenuItem minimize;
    /** Кнопка "Настройки" в главном меню (Опции) */
    @FXML protected MenuItem options;
    /** Кнопка фиксации приложения поверх остальных окон в главном меню (Опции) */
    @FXML protected MenuItem fixTop;
    /** Кнопка проверки актуальности версии в главном меню (Помощь) */
    @FXML protected MenuItem needUpdate;
    /** Кнопка выхода из приложения в главном меню (Файл) */
    @FXML protected MenuItem close;
    /** Поле со статистикой задач */
    @FXML protected Label taskStats;
    /** Контроллер основного окна приложения */
    private MainControl mainControl;
    /** Количество непрочитанных задач */
    private AtomicInteger countNotReadTasks = new AtomicInteger(-1);
    /** Количество невыполненных задач */
    private AtomicInteger countNotPerformTasks = new AtomicInteger(-1);

    @FXML
    private void initialize(){
        log.info("Initialize root layer...");
        initButtons();
        applicationName.setText(ClientApp.getTITLE());
        version.setText(ClientApp.getVERSION());
        log.info("Complete");
    }

    private void initButtons(){
        log.info("Initialize buttons for root layout...");
        minimize.setOnAction(event->setMinimize(true));
        minimizeButton.setOnAction(event->setMinimize(true));
        close.setOnAction(event->{
            Platform.exit();
            System.exit(0);
        });
        fixTop.setOnAction(event->ClientApp.primaryStageChangeFixTop());
        needUpdate.setOnAction(event-> ClientApp.isNeedUpdate());
        log.debug("Complete");
    }

    /**
     * Открывает меню "Опции"
     * @See options
     */
    @FXML
    private void openOptions(){
        log.info("Open options...");
        Label serverIp = new Label("IP-адрес сервера");
        TextField serverIpField = new TextField();
        UserPreferences.loadPrefIpArdess();
        serverIpField.setText(ClientApp.getIp());
        Button accept = new Button("Применить");
        accept.setOnAction(event->{
            log.info("Edit options..");
            ClientApp.setIp(serverIpField.getText());
            Stage stage = (Stage) accept.getScene().getWindow();
            stage.close();
            log.info("Complete");
        });

        GridPane field = new GridPane();
        field.add(serverIp,0,0);
        field.add(serverIpField,1,0);
        field.add(accept,0,1);

        Scene scene = new Scene(field);
        Stage stage = new Stage();
        stage.setTitle("Настройки");
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.showAndWait();
        log.info("Complete");
    }

    /**
     * Изменяет название кнопки @see fixTop в меню @see options
     * @param b
     */
    public void fixTopSetText(Boolean b){
        if (b==true){
            fixTop.setText("Открепить приложение");
        }
        else {
            fixTop.setText("Закрепить поверх окон");
        }
    }

    /**
     * Функция минимизации приложения
     * @See minimizeButton
     * @param minimize
     */
    private void setMinimize(Boolean minimize){
        log.info("Setting minimize...");
        if (!ClientApp.getIsConnectActive()){
            log.error("Connection false! Minimize return");
            return;
        }
        else if (minimize ==true) {
            log.info("Minimized application...");
            if (mainControl.getTaskList().size() == 0) {
                ClientApp.outWrite(String.format("%s%s/%s", Instruction.GET_TASKS.getCommand(), ClientApp.getCurrentUser().getKey().getLogin(), 0));
            }
            try {
                log.info("Open task table for current user...");
                StageShower shower = StageShower.getShower("/TaskTable.fxml")
                        .withOnCloseRequest(event -> {
                            log.info("Application quit");
                            Platform.exit();
                            System.exit(0);
                        })
                        .alwaysTop();
                TaskTableControl control = shower.getController();
                control.presets(mainControl.getTaskList(), ClientApp.getCurrentUser().getKey(),3);
                ClientApp.hidePrimaryStage(true);
                shower.show();
                log.info("Complete");
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
            log.info("Complete");
        }
        else{
            ClientApp.hidePrimaryStage(false);
        }
        log.info("Complete");
    }

    /**
     * Метод обновления статистики задач
     * @See taskStats
     */
    public void updateTaskStats(){
        log.info("Updating task stats...");
        Platform.runLater(() -> {
            taskStats.setText(String.format("Непрочитанных задач: %d, невыполненных задач: %d", countNotReadTasks.get(), countNotPerformTasks.get()));
        });
        log.info("Complete");
    }

    public int incrementNotPerformTaskCount(){
        countNotPerformTasks.incrementAndGet();
        updateTaskStats();
        return countNotPerformTasks.get();
    }

    public int decrementNotPerformTaskCount(){
        countNotPerformTasks.decrementAndGet();
        updateTaskStats();
        return countNotPerformTasks.get();
    }

    public int incrementNotReadTaskCount(){
        countNotReadTasks.incrementAndGet();
        updateTaskStats();
        return countNotReadTasks.get();
    }

    public int decrementNotReadTaskCount(){
        countNotReadTasks.decrementAndGet();
        updateTaskStats();
        return countNotReadTasks.get();
    }

    public void setCountNotReadTasks(int countNotReadTasks) {
        this.countNotReadTasks = new AtomicInteger(countNotReadTasks);
    }

    public void setCountNotPerformTasks(int countNotPerformTasks) {
        this.countNotPerformTasks = new AtomicInteger(countNotPerformTasks);
    }

    public AtomicInteger getCountNotReadTasks() {
        return countNotReadTasks;
    }

    public void setMainControl(MainControl mainControl) {
        this.mainControl = mainControl;
    }
}
