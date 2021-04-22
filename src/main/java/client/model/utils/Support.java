package client.model.utils;

import client.controller.*;
import client.model.ClientApp;
import client.model.entity.UserProfile;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * Вспомогательный класс
 */
@Slf4j
public class Support {

    private Support(){}

    private static final String forbiddenCharacters = "[^\\n/]*";
    private static ComboBox<String>taskCategoryBox;

    /**
     * Проверяет, является ли текущий пользователь руководителем
     * пользователя, переданного в параметре
     * @param user
     * @return
     */
    public static boolean checkHead(UserProfile user){
        if (ClientApp.getCurrentUser().getValue().getEmail().equals(user.getEmail())){
            return false;
        }
        else if (ClientApp.getCurrentUser().getValue().getPosition().getName().equals("Глава администрации муниципального образования")){
            return true;
        }
        else if (ClientApp.getCurrentUser().getValue().getPosition().getName().equals("Заместитель главы администрации")||
                ClientApp.getCurrentUser().getValue().getPosition().getName().equals("Первый заместитель главы администрации")||
                ClientApp.getCurrentUser().getValue().getPosition().getName().equals("Руководитель аппарата администрации")){
            return true;
        }
        else if ((ClientApp.getCurrentUser().getValue().getPosition().getName().equals("Руководитель подразделения")||
                ClientApp.getCurrentUser().getValue().getPosition().getName().equals("Заместитель руководителя подразделения"))&&
                ClientApp.getCurrentUser().getValue().getDepartment().getName().equals(user.getDepartment().getName())){
            return true;
        }
        else if ((ClientApp.getCurrentUser().getValue().getPosition().getName().equals("Руководитель подразделения")||
                ClientApp.getCurrentUser().getValue().getPosition().getName().equals("Заместитель руководителя подразделения"))&&
                ClientApp.getCurrentUser().getValue().getDepartment().getName().equals("Комитет имущественных и земельных отношений")&&
                (user.getDepartment().getName().equals("Отдел жилищных правоотношений")||
                user.getDepartment().getName().equals("Отдел земельных отношений")||
                user.getDepartment().getName().equals("Отдел имущественных отношений"))){
            return true;
        }
        else if ((ClientApp.getCurrentUser().getValue().getPosition().getName().equals("Руководитель подразделения")||
                ClientApp.getCurrentUser().getValue().getPosition().getName().equals("Заместитель руководителя подразделения"))&&
                ClientApp.getCurrentUser().getValue().getDepartment().getName().equals("Управление по бюджету и финансам")&&
                (user.getDepartment().getName().equals("Отдел казначейского исполнения бюджета")||
                user.getDepartment().getName().equals("Отдел по бюджету")||
                user.getDepartment().getName().equals("Отдел доходов и финансирования отдельных отраслей")||
                user.getDepartment().getName().equals("Контрольно-ревизионный сектор"))){
            return true;
        }
        else if ((ClientApp.getCurrentUser().getValue().getPosition().getName().equals("Руководитель подразделения")||
                ClientApp.getCurrentUser().getValue().getPosition().getName().equals("Заместитель руководителя подразделения"))&&
                ClientApp.getCurrentUser().getValue().getDepartment().getName().equals("Управление по организационной, кадровой работе и информационному обеспечению")&&
                (user.getDepartment().getName().equals("Отдел муниципальной службы и кадров")||
                user.getDepartment().getName().equals("Отдел по организационной работе")||
                user.getDepartment().getName().equals("Сектор информационного обеспечения"))){
            return true;
        }
        else return false;
    }

    /**
     * Вызывает уведомление о поступившем сообщении чата
     * @param userName
     */
    public static void callAlertAboutMessage(String userName){
        Platform.runLater(()-> {
            Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
            Support.alertShower(Alert.AlertType.INFORMATION, Modality.NONE)
                    .withTitle("Новое сообщение")
                    .withHeaderText("Новое сообщение от: "+userName)
                    .withDelay(Duration.seconds(7))
                    .withPosition(bounds.getMaxX()-400, bounds.getMaxY()-200)
                    .show();
        });
    }

    /**
     * Форматирует номер телефона
     * @param phoneNumber
     * @return
     */
    public static String formatPhoneNumber(String phoneNumber){
        String phoneMask = "0 000 000-00-00";
        char[]result = new char[16];
        result[0]='+';
        int idx = 0;
        for (int i=0;i<phoneMask.length();i++){
            if (phoneMask.charAt(i)=='0'){
                result[i+1]=phoneNumber.charAt(idx++);
            }
            else {
                result[i+1]=phoneMask.charAt(i);
            }
        }
        return String.valueOf(result);
    }

    /**
     * Вызывает уведомление о поступлении новой задачи
     */
    public static void alertNewTask(){
        Platform.runLater(()-> Support.alertShower(Alert.AlertType.WARNING, Modality.APPLICATION_MODAL)
                                            .withTitle("Новая задача")
                                            .withHeaderText("Новая задача")
                                            .alwaysTop()
                                            .show());
    }

    /**
     * Вызывает уведомление об исполнении одной из задач
     * @param taskName
     */
    public static void alertTaskComplete(String taskName){
        Platform.runLater(()-> Support.alertShower(Alert.AlertType.INFORMATION, Modality.APPLICATION_MODAL)
                                            .withTitle("Задача выполнена")
                                            .withHeaderText("Ваша задача выполнена: "+taskName)
                                            .alwaysTop()
                                            .show());
    }

    /**
     * Вызывает уведомление о возвращении одной из задач на доработку
     * @param taskName
     */
    public static void alertTaskRefunded(String taskName){
        Platform.runLater(()-> Support.alertShower(Alert.AlertType.WARNING, Modality.APPLICATION_MODAL)
                                            .withTitle("Задача возвращена на доработку")
                                            .withHeaderText("Задача возвращена на доработку: "+taskName)
                                            .alwaysTop()
                                            .show());
    }

    /**
     * Вызывает уведомление о некорректно введенных данных
     * @param message
     */
    public static void alertIncompleteData(String message){
        Platform.runLater(()-> Support.alertShower(Alert.AlertType.WARNING, Modality.APPLICATION_MODAL)
                                            .withTitle("Некорректные данные")
                                            .withHeaderText(message)
                                            .show());
    }

    /**
     * Вызывает уведомление о попытке переподключения к серверу
     */
    public static void alertTryingConnect(){
        Platform.runLater(()-> Support.alertShower(Alert.AlertType.WARNING, Modality.APPLICATION_MODAL)
                                            .withTitle("Сервер недоступен")
                                            .withHeaderText("Повторная попытка подключения...")
                                            .show());
    }

    /**
     * Вызывает уведомление о прочтении одной из задач
     * @param taskName
     */
    public static void alertAboutTaskRead(String taskName){
        Platform.runLater(()-> Support.alertShower(Alert.AlertType.WARNING, Modality.APPLICATION_MODAL)
                                            .withTitle("Задача получена")
                                            .withHeaderText(String.format("Задача \"%s\" прочитана", taskName))
                                            .alwaysTop()
                                            .show());
    }

    /**
     * Вызывает уведомление о количестве непрочитанных/невыполненных сообщений
     * @param notRead
     * @param notPerform
     */
    public static void alertWithStats(int notPerform, int notRead){
        Platform.runLater(()-> Support.alertShower(Alert.AlertType.WARNING, Modality.APPLICATION_MODAL)
                                            .withTitle("У вас непрочитанные задачи!")
                                            .withHeaderText(String.format("Непрочитанных задач: %d\n" +
                                                    "Всего невыполненно: %d", notRead,notPerform))
                                            .show());
    }

    /**
     * Вызывает уведомление о необходимости обновления.
     * @param needUpdate
     */
    public static void alertIsNeedUpdate(boolean needUpdate){
        Platform.runLater(()-> Support.alertShower(needUpdate ? Alert.AlertType.WARNING : Alert.AlertType.INFORMATION, Modality.APPLICATION_MODAL)
                                            .withTitle(needUpdate ? "Требуется обновление" : "Обновление не требуется")
                                            .withHeaderText(needUpdate ? "Используется неактуальная версия приложения.\n" +
                                                    "Для корректной работы скачайте новую версию" :
                                                    "Используется актуальная версия приложения")
                                            .show());
    }

    /**
     * Вызывает уведомление о недоступности сервера
     */
    public static void stopServer(){
        Platform.runLater(()-> {
            Support.alertShower(Alert.AlertType.WARNING,Modality.APPLICATION_MODAL)
                    .withTitle("Критическая ошибка!")
                    .withHeaderText("Соединение потеряно! Необходимо перезапустить приложение.")
                    .alwaysTop()
                    .show();
        });
    }

    /**
     * Билдер, создающий уведомления
     * @param type
     * @param modality
     * @return
     */
    public static AlertShower alertShower(Alert.AlertType type, Modality modality){
        return new AlertShower(modality,type);
    }

    public static class AlertShower {

        private Alert alert;
        private PauseTransition delay;

        private AlertShower(Modality modality, Alert.AlertType type) {
            alert = new Alert(type);
            alert.initModality(modality);
        }

        public void show(){
            alert.show();
            if (delay!=null) delay.play();
        }

        public AlertShower withHeaderText(String head){
            alert.setHeaderText(head);
            return this;
        }

        public AlertShower withTitle(String title){
            alert.setTitle(title);
            return this;
        }

        public AlertShower alwaysTop(){;
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            stage.setAlwaysOnTop(true);
            return this;
        }

        public AlertShower withPosition (double x, double y){
            alert.setX(x);
            alert.setY(y);
            return this;
        }

        public AlertShower withDelay(Duration duration){
            delay = new PauseTransition(Duration.seconds(7));
            delay.setOnFinished(timeUp->alert.hide());
            return this;
        }
    }
}
