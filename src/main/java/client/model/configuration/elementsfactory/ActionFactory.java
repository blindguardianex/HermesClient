package client.model.configuration.elementsfactory;

import client.controller.MainControl;
import client.controller.TaskCreatingControl;
import client.controller.TaskTableControl;
import client.model.ClientApp;
import client.model.Instruction;
import client.model.entity.User;
import client.model.utils.StageShower;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import lombok.extern.slf4j.Slf4j;
import java.io.IOException;

@Slf4j
public class ActionFactory {

    /**
     * Возвращает действие, открывающее задачи подчиненного сотрудника
     * @param user
     * @param mainControl
     * @return
     */
    public EventHandler<ActionEvent> openEmployeeTasksAction(User user, MainControl mainControl) {
        EventHandler<ActionEvent> event = e -> {
            try {
                ClientApp.outWrite(String.format("%s%s/%s", Instruction.GET_TASKS.getCommand(),user.getLogin(),0));
                log.info("Open task table for user "+user.getLogin());
                StageShower shower = StageShower.getShower("/TaskTable.fxml");
                TaskTableControl control = shower.getController();
                control.presets(mainControl.getTempTaskList(), user,2);
                shower.show();
                log.info("Complete");
            } catch (IOException ex) {
                log.error(ex.getMessage(), ex);
            }
        };
        return event;
    }

    /**
     * Возвращает действие, создающее новую задачу
     * @param user
     * @return
     */
    public EventHandler<ActionEvent> createTaskAction(User user) {
        EventHandler<ActionEvent> event = e -> {
            try {
                StageShower shower = StageShower.getShower("/TaskCreating.fxml");
                TaskCreatingControl control = shower.getController();
                control.presets(user);
                shower.show();
            } catch (IOException ex) {
                log.error(ex.getMessage(), ex);
            }
        };
        return event;
    }

    /**
     * Возвращает действие, открывающее отправленные пользователем задачи
     * @param user
     * @param mainControl
     * @return
     */
    public EventHandler<ActionEvent> openAppointedTasksAction(User user, MainControl mainControl) {
        EventHandler<ActionEvent> event = e->{
            try {
                if (mainControl.getMyTaskList().size()==0){
                    ClientApp.outWrite(String.format("%s%s/%s", Instruction.GET_APPOINTED_TASKS.getCommand(),user.getLogin(),0));
                }
                log.info("Open task table from current user...");
                StageShower shower = StageShower.getShower("/TaskTable.fxml");
                TaskTableControl control = shower.getController();
                control.presets(mainControl.getMyTaskList(), user,1);
                shower.show();
                log.info("Complete");
            } catch (IOException ex) {
                log.error(ex.getMessage(), ex);
            }
        };
        return event;
    }

    /**
     * Возвращает действие, открывающее отправленные пользователю задачи
     * @param user
     * @param mainControl
     * @return
     */
    public EventHandler<ActionEvent> openTasksAction(User user, MainControl mainControl) {
        EventHandler<ActionEvent> event = e->{
            try {
                if (mainControl.getTaskList().size()==0){
                    ClientApp.outWrite(String.format("%s%s/%s", Instruction.GET_TASKS.getCommand(),user.getLogin(),0));
                }
                log.info("Open task table for current user...");
                StageShower shower = StageShower.getShower("/TaskTable.fxml");
                TaskTableControl control = shower.getController();
                control.presets(mainControl.getTaskList(), user,0);
                shower.show();
                log.info("Complete");
            } catch (IOException ex) {
                log.error(ex.getMessage(), ex);
            }
        };
        return event;
    }
}
