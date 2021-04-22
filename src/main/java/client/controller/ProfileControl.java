package client.controller;

import client.model.ClientApp;
import client.model.configuration.elementsfactory.ActionFactory;
import client.model.entity.User;
import client.model.entity.UserProfile;
import client.model.utils.Pair;
import client.model.utils.Support;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import lombok.extern.slf4j.Slf4j;

import java.io.File;

/** Контроллер окна профиля пользователя */
@Slf4j
public class ProfileControl implements ControllerWithMain, ControllerWithPresets{

    /** Имя пользователя */
    @FXML protected Label profileName;
    /** Электронная почта */
    @FXML protected TextField email;
    /** Подразделение */
    @FXML protected TextField department;
    /** Должность */
    @FXML protected TextField position;
    /** Номер телефона */
    @FXML protected TextField phoneNumber;
    /** Дата регистрации */
    @FXML protected TextField registrationDate;
    /**
     * Если профиль открыт через кнопку "Профиль авторизованного пользователя":
     * открывает задачи, назначенные пользователю.
     * Если профиль открыт из таблицы пользователей:
     * открывает окно новой задачи.
     * @See class - MainControl
     * @See field - MainControl.profileOptions
     * @See field - MainControl.userTable
     */
    @FXML protected Button myTasks;
    /**
     * Если профиль открыт через кнопку "Профиль авторизованного пользователя":
     * открывает задачи, назначенные пользователем.
     * Если профиль открыт из таблицы пользователей:
     * открывает задачи, назначенные пользователю (если
     * авторизованный пользователь является руководителем).
     * @See class - MainControl
     * @See field - MainControl.profileOptions
     * @See field - MainControl.userTable
     */
    @FXML protected Button myAppointedTasks;
    /**
     * Кнопка изменения профиля пользователя.
     * НЕ РЕАЛИЗОВАНА
     */
    @FXML protected Button editProfile;
    /** Контроллер главного окна приложения */
    private MainControl mainControl;
    private ActionFactory actionFactory;

    @FXML
    private void initialize(){
        log.info("Initialize profile stage...");
        actionFactory = new ActionFactory();
        editProfile.setDisable(true);
        editProfile.setVisible(false);
        log.info("Complete");
    }

    /**
     * Инициализирует профиль пользователя.
     * Если открывается профиль текущего (авторизованного) пользователя,
     * кнопка myTasks открывает задачи, назначенные пользователю,
     * а кнопка myAppointedTasks открывает задачи, назначенные пользователем.
     * Если открывается профиль другого пользователя,
     * кнопка myTasks открывает окно создания новой задачи,
     * а кнопка myAppointedTasks - открывает таблицу задач, назаначенных выбранному пользователю
     * (в случае, если текущий пользователь не является руководителем выбранного пользователя,
     * данная кнопка скрывается)
     * @param user
     * @param fromTable - указывает, открывается пользователь из таблицы, или нет
     */
        public void presets(Pair<User, UserProfile> user, boolean fromTable){
        log.info("Initialize user profile: "+user.getKey().getLogin());
        initTextFields(user.getValue());
        if (!fromTable){
            initCurrentUserProfile();
        }
        else {
            initUserProfileFromTable(user);
        }
        log.info("Complete");
    }

    /**
     * Инициализирует текстовые поля профиля
     * @param user
     */
    private void initTextFields(UserProfile user){
        log.info("Initialize text fields...");
        profileName.setText(String.format("Профиль пользователя :%s %s %s",
                user.getLastName(),
                user.getFirstName(),
                user.getOtherName()));
        email.setText(user.getEmail());
        department.setText(user.getDepartment().getName());
        position.setText(user.getPosition().getName());
        phoneNumber.setText(user.getPhoneNumber());
        registrationDate.setText(user.getRegisteredDate().toString());
        log.debug("Complete");
    }

    /**
     * Инициализирует профиль текущего пользователя
     */
    private void initCurrentUserProfile(){
        log.info("Profile for current user");
        myTasks.setText("Поставленные мне задачи");
        myTasks.setOnAction(actionFactory.openTasksAction(ClientApp.getCurrentUser().getKey(),mainControl));
        myAppointedTasks.setText("Поставленные мной задачи");
        myAppointedTasks.setOnAction(actionFactory.openAppointedTasksAction(ClientApp.getCurrentUser().getKey(),mainControl));
    }

    /**
     * Инициализирует профиль пользователя из таблицы пользователей
     * @param user
     */
    private void initUserProfileFromTable(Pair<User, UserProfile>user){
        log.info("Profile for other user");
        myTasks.setText("Новая задача");
        myTasks.setOnAction(actionFactory.createTaskAction(user.getKey()));
        myAppointedTasks.setText("Посмотреть задачи");
        boolean isHead = Support.checkHead(ClientApp.getClientList().get(user.getKey().getLogin()).getValue());
        myAppointedTasks.setDisable(!isHead);
        if(isHead){
            myAppointedTasks.setOnAction(actionFactory.openEmployeeTasksAction(user.getKey(), mainControl));
        }
    }

    public void setMainControl(MainControl mainControl) {
        this.mainControl = mainControl;
    }
}
