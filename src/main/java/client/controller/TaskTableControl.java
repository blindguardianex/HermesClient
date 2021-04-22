package client.controller;

import client.model.ClientApp;
import client.model.Instruction;
import client.model.entity.Task;
import client.model.entity.User;
import client.model.utils.StageShower;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;

import static java.util.stream.Collectors.toList;

@Slf4j
public class TaskTableControl  implements ControllerWithPresets{

    @FXML private TextField filterField;

    /** Устанавливает сортировку строк с задачами по степени выполнения */
    @FXML private Button sortButton;
    @FXML private TableView<Task> taskTable;
    @FXML private TableColumn<Task, String> taskNameColumn;
    @FXML private TableColumn<Task, String> taskSenderColumn;
    @FXML private TableColumn<Task, String> timeOfCreatedTaskColumn;
    @FXML private TextArea previewArea;
    @FXML private Button getMoreTasksButton;
    @FXML private Button uploadAgainButton;
    @FXML private Button getFullVersionButton;
    /**
     * Тип окна таблицы.
     * 0 - таблица содержаит задачи, направленные пользователю
     * 1 - таблица содержаит задачи, направленные пользователем
     * 2 - таблица содержаит задачи, направленные подчиненному сотруднику
     * 3 - минимизированная версия приложения
     */
    private int taskTableType;

    /** Список задач для taskTableView */
    private static ObservableList<Task>taskList;

    /**
     * Отфильтрованный список задач
     * @See updateFilteredUserListData
     * @See filterForUserListTable
     * @See reapplyTableSortOrder
     */
    private static ObservableList<Task> filteredTaskList = FXCollections.observableArrayList();

    @FXML
    private void initialize(){
        log.info("Initialize task table stage...");
        initTaskTable();
        initButtons();
        filterField.textProperty().addListener((((observableValue, s, t1) -> {
            updateFilteredTaskListData();
        })));
        log.info("Complete");
    }

    private void initTaskTable(){
        log.info("Initialize task table...");
        taskNameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        taskSenderColumn.setCellValueFactory(cellData -> cellData.getValue().senderNameProperty());
        timeOfCreatedTaskColumn.setCellValueFactory(cellData -> cellData.getValue().timeOfCreatedProperty());
        taskTable.setRowFactory((param) -> new IsCompleteAndReadTaskRow());
        taskTable.setOnMouseClicked(mouseEvent->{
            if (taskTable.getSelectionModel().getSelectedItem()==null){
                return;
            }
            if (mouseEvent.getClickCount()==2){
                try {
                    log.info("Open task stage...");
                    StageShower shower = StageShower.getShower("/Task.fxml")
                            .presetMainControl()
                            .presetRootControl();
                    TaskControl control = shower.getController();
                    control.presets(taskTable.getSelectionModel().getSelectedItem(), taskTableType);
                    if(ClientApp.isHidePrimaryStage()){
                        shower.alwaysTop();
                    }
                    shower.show();
                    log.info("Complete");
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
            }
            else{
                previewArea.setText(taskTable.getSelectionModel().getSelectedItem().getBody());
            }
        });
        log.debug("Complete");
    }

    private void initButtons(){
        log.info("Initialize buttons...");
        sortButton.setOnAction(event->{
            taskTable.sortPolicyProperty().set(table->{
                Comparator<Task> comparator=(t1, t2)
                        -> t1.getIsRead()>t2.getIsRead()?1
                        :t1.getComplete()>t2.getComplete()?1
                        :table.getComparator()==null?0
                        :table.getComparator().compare(t1,t2);
                FXCollections.sort(taskTable.getItems(), comparator);
                return true;
            });
        });
        getFullVersionButton.setOnAction(event->{
            log.info("Application deploy...");
            Stage stage = (Stage) getFullVersionButton.getScene().getWindow();
            stage.close();
            ClientApp.hidePrimaryStage(false);
            log.info("Complete");
        });
        log.debug("Complete");
    }

    /**
     * Устанавливает порядок сортировки таблицы taskTable
     * после замены filteredTaskList в ней
     */
    private void reapplyTableSortOrder(){
        ArrayList<TableColumn<Task,?>> sortOrder= new ArrayList<>(taskTable.getSortOrder());
        taskTable.getSortOrder().clear();
        taskTable.getSortOrder().addAll(sortOrder);
    }

    /**
     * Инициализирует таблицу задач для пользователя user
//не сделано     * В APPOINTED TASK сервер вместо отправителя присылает получателя,
//не сделано     * поэтому не надо делать отдельного типа для них.
     * @param taskListForTable
     * @param user
     * @param tableType
     *      принимает 3 типа:
     *      0 - таблица содержаит задачи, направленные пользователю
     *      1 - таблица содержаит задачи, направленные пользователем
     *      2 - таблица содержаит задачи, направленные подчиненному сотруднику
     *      3 - минимизированная версия приложения
     */
    public void presets(ObservableList<Task>taskListForTable, User user, int tableType){
        log.info("Initialize task table...");
        taskList=taskListForTable;
        taskTableType=tableType;
        taskTable.setItems(taskList);
        if (tableType!=3){
            getFullVersionButton.setDisable(true);
            getFullVersionButton.setVisible(false);
        }
        switch (tableType){
            case 1:{
                initAppointedTaskTable(user);
                break;
            }
            case 0:
            case 2:
            case 3:{
                initReceivedTaskTable(user);
                break;
            }
            default:{
                log.error("Unknown task table type: " + tableType);
                throw new IllegalArgumentException("Unknown task table type: " + tableType);
            }
        }
        log.info("Complete");
    }

    private void initReceivedTaskTable(User user){
        log.info("Open task table for user: "+user.getLogin());
        getMoreTasksButton.setOnAction(event -> {
            if (taskList.size() % 20 == 0) {
                ClientApp.outWrite(String.format("%s%s/%s", Instruction.GET_TASKS.getCommand(), user.getLogin(), taskList.size() / 20));
                filterField.clear();
                taskTable.setItems(taskList);
            } else {
                getMoreTasksButton.setDisable(true);
                filterField.clear();
                taskTable.setItems(taskList);
            }
        });

        uploadAgainButton.setOnAction(event -> {
            taskList.clear();
            ClientApp.outWrite(String.format("%s%s/%s", Instruction.GET_TASKS.getCommand(), user.getLogin(), 0));
            getMoreTasksButton.setDisable(false);
            filterField.clear();
            taskTable.setItems(taskList);
        });
    }

    private void initAppointedTaskTable(User user){
        log.info("Open appointed task table for current user");

        taskSenderColumn.setText("Получатель");
        taskSenderColumn.setCellValueFactory(cellData -> cellData.getValue().getRecipientNameProperty());

        getMoreTasksButton.setOnAction(event -> {
            if (taskList.size() % 20 == 0) {
                ClientApp.outWrite(String.format("%s%s/%s", Instruction.GET_APPOINTED_TASKS.getCommand(), user.getLogin(), taskList.size() / 20));
                filterField.clear();
                taskTable.setItems(taskList);
            } else {
                getMoreTasksButton.setDisable(true);
                filterField.clear();
                taskTable.setItems(taskList);
            }
        });

        uploadAgainButton.setOnAction(event -> {
            taskList.clear();
            ClientApp.outWrite(String.format("%s%s/%s", Instruction.GET_APPOINTED_TASKS.getCommand(), user.getLogin(), 0));
            getMoreTasksButton.setDisable(false);
            filterField.clear();
            taskTable.setItems(taskList);
        });
    }

    /**
     * Класс, устанавливающий строки цветными.
     * Выполненные задачи - Honeydew
     * Невыполненные и прочитанные задачи - Moccasin
     * Непрочитанные задачи - BurlyWood
     */
    private static class IsCompleteAndReadTaskRow extends TableRow<Task> {
        @Override
        protected void updateItem(Task task, boolean b) {
            super.updateItem(task, b);
            if (b || task == null) {
                this.setStyle("");
            }
            if (!isEmpty()){
                if (task.getComplete()==0 && task.getIsRead()==0) this.setStyle("-fx-background-color:BurlyWood");
                else if (task.getComplete()==0) this.setStyle("-fx-background-color:Moccasin");
                else this.setStyle("-fx-background-color:Honeydew");
            }
        }
    }

    /**
     * Функция фильтрации пользователей.
     * Добавляет в filteredTaskList отфильтрованных пользоватей
     * из taskList.
     * Ставит в taskTable filteredTaskList.
     * @See filterForTaskListTable
     * @See reapplyTableSortOrder
     */
    private void updateFilteredTaskListData(){
        filteredTaskList.clear();
        filteredTaskList.addAll(taskList.stream()
                .filter(task->filterForTaskListTable(task))
                .collect(toList()));
        reapplyTableSortOrder();
        taskTable.setItems(filteredTaskList);
    }

    /**
     * Устанавливает соответствие filterField содержанию в колонках таблицы taskTable.
     * Если какая либо из колонок таблиц содержит значение из filterField возвращает true.
     * @param task
     * @return
     */
    private boolean filterForTaskListTable(Task task){
        String filter = filterField.getText();
        if (filter==null || filter.isEmpty()){
            return true;
        }
        String lowerCaseFilter = filter.toLowerCase();
        if (task.getName().toLowerCase().indexOf(lowerCaseFilter)!=-1){
            return true;
        }
        if (task.getSender().getShortName().toLowerCase().indexOf(lowerCaseFilter)!=-1){
            return true;
        }
        if (task.getTimeOfCreated().toString().toLowerCase().indexOf(lowerCaseFilter)!=-1){
            return true;
        }
        return false;
    }
}
