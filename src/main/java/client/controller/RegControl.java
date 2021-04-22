package client.controller;

import client.model.configuration.elementsfactory.ValidationListenerBuilder;
import client.model.entity.Department;
import client.model.entity.Position;
import client.model.entity.User;
import client.model.entity.UserProfile;
import client.model.utils.Support;
import client.model.ClientApp;
import client.model.Instruction;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class RegControl {

    /** Поле ввода электронной почты */
    @FXML protected TextField email;
    /** Поле ввода пароля */
    @FXML protected PasswordField passwordReg;
    /** Поле повторного ввода пароля */
    @FXML protected PasswordField passwordValidateReg;
    /** Поле ввода имени */
    @FXML protected TextField firstNameReg;
    /** Поле ввода фамилии */
    @FXML protected TextField lastNameReg;
    /** Поле ввода отчества */
    @FXML protected TextField otherNameReg;
    /** Поле ввода номера телефона */
    @FXML protected TextField phoneNumberReg;
    /** Кнопка завершения регистрации */
    @FXML protected Button registrationFinal;
    /** Поле выбора подразделения */
    @FXML protected ComboBox<String> departmentBox;
    /** Поле выбора должности */
    @FXML protected ComboBox<String> positionBox;

    /**
     * Паттерн, разрешающий вводить только буквенные символы.
     * Применяется для полей:
     * @See firstNameReg
     * @See lastNameReg
     * @See otherNameReg
     */
    private final String patternName = "\\b\\D*\\b";
    /**
     * Паттерн, разрешающий вводить только цифры.
     * Применяется для полей:
     * @See phoneNumberReg
     */
    private final String patternNumber = "\\b\\d*\\b";
    /**
     * Паттерн, проверяющий корректность введенной электронной почты
     * @See registrationFinal()
     * @See email
     */
    private final String patternValidEmail = "^([a-z0-9_-]+\\.)*[a-z0-9_-]+@[a-z0-9_-]+(\\.[a-z0-9_-]+)*\\.[a-z]{2,6}$";
    /**
     * Паттерн, проверяющий корректность введенного номера телефона
     * @See registrationFinal()
     */
    private final String patternValidNumber = "\\b\\d{10}\\b";
    /**
     * символы, запрещенные для написания
     * \n - отправляет сообщение
     * / - является разделителем при парсинге сообщения
     */
    private final String forbiddenCharacters = "[^\\n/]*";

    private List<Department>departments;
    private List<Position>positions;
    private Gson gson;

    @FXML
    private void initialize(){
        log.info("Initialize registration stage...");
        departmentBox.setValue("<Выберите подразделение>");
        positionBox.setValue("<Выберите должность>");
        gson=ClientApp.getGson();
        initTextFieldValidationListeners();
        log.info("Complete");
    }

    /**
     *
     */
    private void initTextFieldValidationListeners(){
        log.info("Initialize validation listeners...");
        passwordReg.textProperty().addListener(ValidationListenerBuilder.listenerFor(passwordReg)
                                                            .byPattern(forbiddenCharacters).get());
        passwordValidateReg.textProperty().addListener(ValidationListenerBuilder.listenerFor(passwordValidateReg)
                                                            .byPattern(forbiddenCharacters).get());
        firstNameReg.textProperty().addListener(ValidationListenerBuilder.listenerFor(firstNameReg)
                                                            .byPattern(patternName).get());
        lastNameReg.textProperty().addListener(ValidationListenerBuilder.listenerFor(lastNameReg)
                                                            .byPattern(patternName).get());
        otherNameReg.textProperty().addListener(ValidationListenerBuilder.listenerFor(otherNameReg)
                                                            .byPattern(patternName).get());
        phoneNumberReg.textProperty().addListener(ValidationListenerBuilder.listenerFor(phoneNumberReg)
                                                            .byPattern(patternNumber).get());
        log.debug("Complete");
    }

    /**
     * Кнопка "Зарегистрироваться" на панели регистрации приложения
     * Проверяет корректность введенной электронной почты
     * Проверяет корректность пароля (совпадение введенного пароля и подтверждения)
     * Проверяет введенный номер телефона
     * Проверяет наличие выбранных подразделения и должности
     * @throws IOException
     */
    @FXML
    private void doRegistration(){
        verifyRegistrationData();

        User user = createUserForRegistration();
        UserProfile profile = createUserProfileForRegistration();

        String userJson = gson.toJson(user);
        String profileJson = gson.toJson(profile);
        String newUser = String.format("%s%s/%s/%s",
                Instruction.NEW_USER.getCommand(), userJson, profileJson, passwordReg.getText());

        log.info("Sending registration user info...");
        ClientApp.outWrite(newUser);
    }

    /**
     * Проверяет регистрационные данные
     */
    private void verifyRegistrationData(){
        checkEmptyFields();
        checkEmail();
        checkPassword();
        checkPhoneNumber();
        checkDepartmentAndPosition();
    }

    /**
     * Проверяет, все ли обязательные поля заполнены
     */
    private void checkEmptyFields(){
        if (email.getText().equals("")||
                passwordReg.getText().equals("")||
                passwordValidateReg.getText().equals("")||
                firstNameReg.getText().equals("")||
                lastNameReg.getText().equals("")||
                otherNameReg.getText().equals("")||
                phoneNumberReg.getText().equals("")){
            Support.alertIncompleteData("Заполните все поле");
            log.error("Registration error: Empty field");
            throw new IllegalArgumentException("Empty field");
        }
    }

    /**
     * Проверяет корректность введенного емайла
     */
    private void checkEmail(){
        if (!email.getText().toLowerCase().matches(patternValidEmail)) {
            Support.alertIncompleteData("Некорректный адрес электронной почты");
            log.error("Registration error: Incorrect email");
            throw new IllegalArgumentException("Incorrect email");
        }
    }

    /**
     * Проверяет, совпадает ли введеный пароль с подтверждением
     */
    private void checkPassword(){
        if (!passwordReg.getText().equals(passwordValidateReg.getText())) {
            Support.alertIncompleteData("Введенные пароли не совпадают");
            log.error("Registration error: Password validation error");
            throw new IllegalArgumentException("Password validation error");
        }
    }

    /**
     * Проверяет правильность введенность телефонного номера
     */
    private void checkPhoneNumber(){
        if (!phoneNumberReg.getText().matches(patternValidNumber)){
            Support.alertIncompleteData("Введите 10 цифр номера телефона (без 8)");
            log.error("Registration error: Incorrect phone number");
            throw new IllegalArgumentException("Incorrect phone number");
        }
    }

    /**
     * Проверяет, выбрано ли подразделение и должность
     */
    private void checkDepartmentAndPosition(){
        if (departmentBox.getValue().equals("<Выберите подразделение>")|| positionBox.getValue().equals("<Выберите должность>")){
            Support.alertIncompleteData("Выберите подразделение и должность");
            log.error("Registration error: Department or position is empty");
            throw new IllegalArgumentException("Department or position is empty");
        }
    }

    /**
     * оздает пользователя для регистрации
     * @return
     */
    private User createUserForRegistration(){
        User user = User.getBuilder()
                .withLogin(email.getText().toLowerCase())
                .withPassword(passwordReg.getText())
                .build();
        return user;
    }

    /**
     * Создает профиль пользователя для регистрации
     * @return
     */
    private UserProfile createUserProfileForRegistration(){
        UserProfile profile = UserProfile.getBuilder()
                .withFirstName(firstNameReg.getText())
                .withLastName(lastNameReg.getText())
                .withOtherName(otherNameReg.getText())
                .withDepartment(departments.stream()
                        .filter(dep->dep.getName().equals(departmentBox.getValue()))
                        .findFirst()
                        .get())
                .withPosition(positions.stream()
                        .filter(pos->pos.getName().equals(positionBox.getValue()))
                        .findFirst()
                        .get())
                .withEmail(email.getText())
                .withPhoneNumber(Support.formatPhoneNumber("7"+phoneNumberReg.getText()))
                .build();
        return profile;
    }

    /**
     * Устанавливает список подразделений
     * @See departmentBox
     * @param departmentList
     */
    public void setDepartmentBox(String[] departmentList){
        log.info("Set department list.");
        Type depList = new TypeToken<List<Department>>(){}.getType();
        departments = gson.fromJson(departmentList[0], depList);
        this.departmentBox.getItems().setAll(departments.stream()
                                                    .map(Department::getName)
                                                                .collect(Collectors.toList()));
        log.info("Complete");
    }

    /**
     * Устанавливает список должностей
     * @See positionBox
     * @param positionList
     */
    public void setPositionBox(String[] positionList){
        log.info("Set position list.");
        Type posList = new TypeToken<List<Position>>(){}.getType();
        positions = gson.fromJson(positionList[0], posList);
        this.positionBox.getItems().setAll(positions.stream()
                                                    .map(Position::getName)
                                                                .collect(Collectors.toList()));
        log.info("Complete");
    }

}
