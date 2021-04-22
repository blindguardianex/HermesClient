package client.controller;

import client.model.configuration.elementsfactory.ActionFactory;
import client.model.configuration.elementsfactory.ValidationListenerBuilder;
import client.model.entity.Message;
import client.model.entity.Task;
import client.model.entity.User;
import client.model.utils.Pair;
import client.model.utils.StageShower;
import client.model.utils.Support;
import client.model.ClientApp;
import client.model.Instruction;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import java.util.ArrayList;

import static java.util.stream.Collectors.toList;

/**
 * Контроллер основного окна программы
 */
@Slf4j
public class MainControl implements ControllerWithRoot{
    /** Кнопка отправки сообщения в основной чат */
    @FXML protected Button sendMessage;
    /** Поле с именем авторизованного пользователя */
    @FXML protected Label userName;
    /** Область основного чата */
    @FXML protected volatile TextArea chatArea;
    /** Область ввода текста сообщения для основного чата */
    @FXML protected TextArea messageArea;
    /** Таблица пользователей */
    @FXML protected TableView<User> onlineUserTable;
    /** Колонка в таблице пользователей - имя пользователя */
    @FXML protected TableColumn<User, String> onlineUsersName;
    /** Кнопка открытия профиля */
    @FXML protected Button profileOptions;
    /** Поле фильтра */
    @FXML protected TextField filterField;
    /**
     * символы, запрещенные для написания
     * \n - отправляет сообщение
     * / - является разделителем при парсинге сообщения
     */
    private final String forbiddenCharacters = "[^\\n/]*";
    /** id основного чата */
    private int mainDialogId = 1;
    /** Список пользователей */
    private static ObservableList<User>userList= FXCollections.observableArrayList();
    /**
     * Отфильтрованный список пользователей
     * @See updateFilteredUserListData
     * @See filterForUserListTable
     * @See reapplyTableSortOrder
     */
    private static ObservableList<User>filteredUserList = FXCollections.observableArrayList();
    /** Список задач, установленных для текущего (авторизованного) пользователя */
    private static ObservableList<Task>taskList = FXCollections.observableArrayList();
    /** Список задач, установленных текущим (авторизованным) пользователем */
    private static ObservableList<Task>myTaskList = FXCollections.observableArrayList();
    /**
     * Список задач для пользователя, являющегося подчиненным сотрудником текущего
     * (авторизованного) пользователя
     */
    private static ObservableList<Task>tempTaskList = FXCollections.observableArrayList();
    /** Контроллер Root окна (основного меню программы) */
    private RootControl rootControl;
    private Gson gson;

    @FXML
    private void initialize(){
        log.info("Initializing main application stage...");

        initButtons();
        initUserTable();
        initRemainingProps();

        ClientApp.outWrite(Instruction.TASK_STATS.getCommand());
        log.info("Complete");
    }

    /**
     * Инициализирует свойства таблицы пользователей
     */
    private void initUserTable(){
        log.info("Initializing table with user list...");
        filteredUserList.addAll(userList);
        userList.addListener((ListChangeListener<? super User>) change->{
            updateFilteredUserListData();
        });

        onlineUserTable.setRowFactory((param) -> new IsOnlineUserRow());
        userName.setText(ClientApp.getCurrentUser().getValue().getUserFIO());
        onlineUsersName.setCellValueFactory(cellData -> cellData.getValue().shortNameProperty());
        onlineUserTable.setItems(filteredUserList);
        log.debug("Complete");
    }

    /**
     * Инициализирует свойства кнопок
     */
    private void initButtons(){
        log.info("Initialize buttons for main stage...");
        ActionFactory factory = new ActionFactory();
        profileOptions.setOnAction(event->openUserProfileWindow());
        messageArea.textProperty().addListener(ValidationListenerBuilder.listenerFor(messageArea)
                                                        .byPattern(forbiddenCharacters).get());
        messageArea.setOnKeyPressed(ke->{
            if(ke.getCode().equals(KeyCode.ENTER)) {
                sendMessage.fire();
            }
        });
        log.debug("Complete");
    }

    /**
     * Инициализирует оставшиеся свойства окна
     */
    private void initRemainingProps(){
        log.info("Initialize remaining properties for main stage...");
        chatArea.setEditable(false);
        filterField.textProperty().addListener((((observableValue, s, t1) -> {
            updateFilteredUserListData();
        })));
        gson=ClientApp.getGson();
        log.debug("Complete");
    }

    /**
     * Функция фильтрации пользователей.
     * Добавляет в filteredUserList отфильтрованных пользоватей
     * из userList.
     * @See filterForUserListTable
     * @See reapplyTableSortOrder
     */
    private void updateFilteredUserListData(){
        filteredUserList.clear();
        filteredUserList.addAll(userList.stream()
                .filter(user->filterForUserListTable(user))
                .collect(toList()));
        reapplyTableSortOrder();
    }

    /**
     * Устанавливает соответствие filterField содержанию в поле onlineUsersName.
     * Если строка onlineUsersName сотержит значение из filterField возвращает true.
     * @param user
     * @return
     */
    private boolean filterForUserListTable(User user){
        String filter = filterField.getText();
        if (filter==null || filter.isEmpty()){
            return true;
        }
        String lowerCaseFilter = filter.toLowerCase();
        if (user.getShortName().toLowerCase().indexOf(lowerCaseFilter)!=-1){
            return true;
        }
        return false;
    }

    /**
     * Устанавливает порядок сортировки таблицы onlineUserTable
     * после замены filteredUserList в ней
     */
    private void reapplyTableSortOrder(){
        ArrayList<TableColumn<User,?>>sortOrder= new ArrayList<>(onlineUserTable.getSortOrder());
        onlineUserTable.getSortOrder().clear();
        onlineUserTable.getSortOrder().addAll(sortOrder);
    }

    /**
     * Отправляет сообщение в основной чат программы
     */
    @FXML
    private void sendMainDialogMessage(){
        log.debug("Sending message in main dialog...");
        Message message = Message.getBuilder()
                .withSender(ClientApp.getCurrentUser().getKey())
                .withTextMessage(messageArea.getText())
                .build();
        String messageJson = gson.toJson(message);
        ClientApp.outWrite(String.format("%s%d/%s", Instruction.DIALOG.getCommand(), mainDialogId, messageJson));
        messageArea.clear();
        log.debug("Complete");
    }

    @FXML
    /**
     * Открывает профиль пользователя из таблицы
     */
    public void openUserProfileFromTab(MouseEvent mouseEvent){
        if (mouseEvent.getClickCount()==2){
            User user =  onlineUserTable.getSelectionModel().getSelectedItem();
            try {
                log.info("Open user profile: "+user.getLogin());
                StageShower shower = StageShower.getShower("/Profile.fxml")
                        .presetMainControl();
                ProfileControl control = shower.getController();
                control.presets(new Pair<>(user, ClientApp.getClientList().get(user.getLogin()).getValue()),true);
                shower.show();
                log.info("Complete");
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    /**
     * Открывает профиль пользователя
     */
    public void openUserProfileWindow(){
        try {
            log.info("Open current user profile: "+ClientApp.getCurrentUser().getKey().getLogin());
            StageShower shower = StageShower.getShower("/Profile.fxml")
                    .presetMainControl();
            ProfileControl control = shower.getController();
            control.presets(ClientApp.getCurrentUser(), false);
            shower.show();
            log.info("Complete");
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * Изменяет решенную задачу в списке задач
     * @param task
     * @return
     */
    public Task taskComplete(Task task){
        taskList.remove(task);
        task.setComplete((short) 1);
        taskList.add(task);
        rootControl.decrementNotPerformTaskCount();
        return task;
    }

    /**
     * Изменяет задачу, возвращенную на доработку, в списке задач
     * @param task
     * @return
     */
    public Task taskRefunded(Task task){
        taskList.remove(task);
        task.setComplete((short) 0);
        task.setIsRead((short) 0);
        taskList.add(task);
        return task;
    }

    /**
     * Обновляет поле чата
     * @param message
     */
    public void addMessageMainChat(Message message){
        log.debug("Adding message in main chat...");
        synchronized (chatArea) {
            chatArea.appendText(String.format("[%s]%s: %s\n",
                    message.getTimeOfSending(),
                    message.getSender().getLogin(),
                    message.getTextMessage()));
        }
        if (ClientApp.isIconifiedPrimaryStage()){
            Support.callAlertAboutMessage(message.getSender().getLogin());
        }
        log.debug("Complete");
    }

    public void addUserInList(User user){
        if (userList.contains(user)){
            userList.remove(user);
        }
        userList.add(user);
    }

    /**
     * Класс для окрашивания строк в таблице пользователей
     * offline пользователи: lightcoral
     * online ользователи: green
     */
    private class IsOnlineUserRow extends TableRow<User> {
        @Override
        protected void updateItem(User user, boolean b) {
            super.updateItem(user, b);
            if (b || user == null) {
                this.setStyle("");
            }
            if (!isEmpty()){
                if (user.getStatus()==0) this.setStyle("-fx-background-color:BurlyWood");
                else this.setStyle("-fx-background-color:Honeydew");
            }
        }
    }

    public ObservableList<Task> getTaskList() {
        return taskList;
    }

    public ObservableList<Task> getTempTaskList() {
        return tempTaskList;
    }

    public ObservableList<Task> getMyTaskList() {
        return myTaskList;
    }

    public void setRootControl(RootControl rootControl) {
        this.rootControl = rootControl;
    }
}
