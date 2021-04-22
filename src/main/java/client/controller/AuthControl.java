package client.controller;

import client.model.ClientApp;
import client.model.Instruction;
import client.model.configuration.elementsfactory.ValidationListenerBuilder;
import client.model.utils.Support;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * Контроллер окна аутентификации
 */
@Slf4j
public class AuthControl {
    /** Поле ввода логина */
    @FXML protected TextField username;
    /** Поле ввода пароля */
    @FXML protected PasswordField password;
    /** Кнопка отправки информации для аутентификации */
    @FXML protected Button logIn;
    /** Кнопка открытия окна регистрации */
    @FXML protected Button registration;

    /**
     * Поле, показывающее успешность/неуспешность регистрации.
     * Изначально пустое
     */
    @FXML protected Label registrationMessage;

    /** Паттерн, проверяющий корректность ввода электронной почты */
    private final String patternEmail = "^([a-z0-9_-]+\\.)*[a-z0-9_-]+@[a-z0-9_-]+(\\.[a-z0-9_-]+)*\\.[a-z]{2,6}$";

    /**
     * символы, запрещенные для написания
     * \n - отправляет сообщение
     * / - является разделителем при парсинге сообщения
     */
    private final String forbiddenCharacters = "[^\\n/]*";

    @FXML
    private void initialize(){
        log.info("Initializing authorization stage...");
        logIn.disableProperty().bind(Bindings.or(username.textProperty().isEmpty(), password.textProperty().isEmpty()));
        initButtons();
        log.info("Complete");
    }

    /**
     * Инициализирует свойства кнопок
     */
    private void initButtons(){
        log.info("Initializing buttons for authorization stage...");
        password.textProperty().addListener(ValidationListenerBuilder.listenerFor(password)
                                            .byPattern(forbiddenCharacters).get());
        password.setOnKeyPressed(ke->{
            if (ke.getCode().equals(KeyCode.ENTER)){
                logIn.fire();
            }
        });
        log.debug("Complete");
    }

    /**
     * Метод для входа в приложение
     * Вызывает уведомление в случае некорректно введенного логина (электронной почты)
     * @throws IOException
     */
    @FXML
    private void login() throws IOException {
        log.info("Login in application...");
        if (!username.getText().matches(patternEmail)){
            Support.alertIncompleteData("Введите в поле \"Логин\" e-mail адрес.");
            log.error("Incorrect login: "+username.getText());
        }
        else {
            ClientApp.connect();
            String name = username.getText().toLowerCase();
            String pass = password.getText();
            log.info("Getting authorization...");
            ClientApp.outWrite(String.format("%s%s/%s", Instruction.AUTHORIZATION.getCommand(), name, pass));
        }
    }

    /**
     * Метод вызова окна регистрации
     * @throws IOException
     */
    @FXML
    private void registration() throws IOException {
        ClientApp.connect();
        log.info("Getting registration...");
        ClientApp.outWrite(Instruction.REGISTRATION.getCommand());
        ClientApp.registrationScene();
    }

    /**
     * Устанавливает сообщение об успешной/неуспешной регистрации или авторизации
     * @param successful
     */
    public void setRegistrationMessage(int successful) {
        switch (successful){
            case 0:{
                registrationMessage.setText("Успешная регистрация.");
                registrationMessage.setTextFill(Color.GREEN);
                break;
            }
            case 1:{
                registrationMessage.setText("Ошибка регистрации: пользователь с данной электронной почтой зарегистрирован в системе.");
                registrationMessage.setTextFill(Color.RED);
                break;
            }
            case 2:{
                registrationMessage.setText("Ошибка входа: неверное имя пользователя или пароль.");
                registrationMessage.setTextFill(Color.RED);
                break;
            }
        }
    }

    public void setUsername(String login) {
        username.setText(login);
    }
}
